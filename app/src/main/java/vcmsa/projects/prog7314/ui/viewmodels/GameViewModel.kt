package vcmsa.projects.prog7314.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.AppDatabase
import vcmsa.projects.prog7314.data.models.*
import vcmsa.projects.prog7314.data.repository.UserProfileRepository
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.data.sync.SyncManager
import kotlin.math.sqrt

/*
    Code Attribution for: Creating ViewModels
    ===================================================
    Android Developers, 2019b. ViewModel Overview | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/libraries/architecture/viewmodel>
    [Accessed 18 November 2025].
*/


/**
 * ViewModel that manages the adventure mode memory game.
 * Handles game logic, card matching, scoring, time tracking, and data persistence.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    // The current state of the game including all cards and progress
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    // Time elapsed since game started (in milliseconds)
    private val _timeElapsed = MutableStateFlow(0L)
    val timeElapsed: StateFlow<Long> = _timeElapsed.asStateFlow()

    // Number of card flips the player has made
    private val _moves = MutableStateFlow(0)
    val moves: StateFlow<Int> = _moves.asStateFlow()

    // Current score including bonuses
    private val _points = MutableStateFlow(0)
    val points: StateFlow<Int> = _points.asStateFlow()

    // Whether the game has finished
    private val _isGameComplete = MutableStateFlow(false)
    val isGameComplete: StateFlow<Boolean> = _isGameComplete.asStateFlow()

    // Final results shown when game completes
    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()

    // Coroutine job that runs the game timer
    private var timerJob: Job? = null

    // Track the two cards currently being compared for a match
    private var firstFlippedCard: GameCard? = null
    private var secondFlippedCard: GameCard? = null

    // Prevent rapid clicking while cards are being checked
    private var isProcessing = false

    // Store the current game configuration
    private lateinit var currentTheme: GameTheme
    private lateinit var currentGridSize: GridSize

    // Repository for managing user profile data
    private val userProfileRepository: UserProfileRepository

    // Manager for syncing data to cloud storage
    private val syncManager: SyncManager

    init {
        // Initialize database and repositories
        val database = AppDatabase.getDatabase(application)
        userProfileRepository = UserProfileRepository(database.userProfileDao())
        syncManager = SyncManager(application)
    }

    /**
     * Sets up a new game with the specified theme and grid size.
     * Generates card pairs, shuffles them, and starts the timer.
     */
    fun initializeGame(theme: GameTheme, gridSize: GridSize) {
        currentTheme = theme
        currentGridSize = gridSize

        // Create shuffled deck of card pairs
        val cards = generateCards(theme, gridSize)

        // Initialize game state with fresh values
        _gameState.value = GameState(
            cards = cards,
            moves = 0,
            matchedPairs = 0,
            points = 0,
            timeElapsed = 0,
            isComplete = false
        )

        // Reset all tracking values
        _timeElapsed.value = 0L
        _moves.value = 0
        _points.value = 0
        _isGameComplete.value = false
        _gameResult.value = null

        // Begin counting time
        startTimer()
    }

    /**
     * Generates a shuffled list of card pairs for the game.
     * Each card has a matching pair with the same image.
     */
    private fun generateCards(theme: GameTheme, gridSize: GridSize): List<GameCard> {
        val totalPairs = gridSize.totalCards / 2
        val availableImages = theme.cardImages

        // Select random images from the theme, repeating if needed
        val selectedImages = if (availableImages.size >= totalPairs) {
            availableImages.shuffled().take(totalPairs)
        } else {
            (availableImages + availableImages).shuffled().take(totalPairs)
        }

        // Create two cards for each image (the matching pair)
        val cards = mutableListOf<GameCard>()
        selectedImages.forEachIndexed { pairId, imageResId ->
            cards.add(
                GameCard(
                    id = cards.size,
                    pairId = pairId,
                    imageResId = imageResId,
                    isFlipped = false,
                    isMatched = false
                )
            )
            cards.add(
                GameCard(
                    id = cards.size,
                    pairId = pairId,
                    imageResId = imageResId,
                    isFlipped = false,
                    isMatched = false
                )
            )
        }

        // Shuffle cards so pairs are randomly distributed
        return cards.shuffled()
    }

    /**
     * Handles when a player clicks on a card.
     * Flips the card and checks for matches when two cards are flipped.
     */
    fun onCardClicked(card: GameCard) {
        // Ignore clicks if processing, card already flipped, or card already matched
        if (isProcessing || card.isFlipped || card.isMatched) return

        val currentState = _gameState.value ?: return
        val updatedCards = currentState.cards.toMutableList()
        val cardIndex = updatedCards.indexOfFirst { it.id == card.id }

        if (cardIndex == -1) return

        // Flip the clicked card
        updatedCards[cardIndex] = updatedCards[cardIndex].copy(isFlipped = true)
        _gameState.value = currentState.copy(cards = updatedCards)

        // Track which card was flipped (first or second)
        when {
            firstFlippedCard == null -> {
                // This is the first card in the pair
                firstFlippedCard = updatedCards[cardIndex]
            }
            secondFlippedCard == null -> {
                // This is the second card - now check for a match
                secondFlippedCard = updatedCards[cardIndex]
                _moves.value += 1
                checkForMatch()
            }
        }
    }

    /**
     * Compares the two flipped cards to see if they match.
     * Awards points for matches or flips cards back if they don't match.
     */
    private fun checkForMatch() {
        val first = firstFlippedCard ?: return
        val second = secondFlippedCard ?: return

        // Block further clicks while checking
        isProcessing = true

        viewModelScope.launch {
            // Wait briefly so player can see the second card
            delay(800)

            val currentState = _gameState.value ?: return@launch
            val updatedCards = currentState.cards.toMutableList()

            if (first.pairId == second.pairId) {
                // Cards match - mark them as matched
                updatedCards.replaceAll { card ->
                    if (card.id == first.id || card.id == second.id) {
                        card.copy(isMatched = true, isFlipped = true)
                    } else {
                        card
                    }
                }

                // Award points with time-based bonus
                val basePoints = 100
                val timeBonus = maxOf(0, 50 - (_timeElapsed.value.toInt() / 1000))
                _points.value += basePoints + timeBonus

                // Update match count
                val matchedPairs = currentState.matchedPairs + 1
                _gameState.value = currentState.copy(
                    cards = updatedCards,
                    matchedPairs = matchedPairs
                )

                // Check if all pairs have been matched
                if (matchedPairs == currentGridSize.totalCards / 2) {
                    completeGame()
                }
            } else {
                // Cards don't match - wait then flip them back
                delay(400)
                updatedCards.replaceAll { card ->
                    if (card.id == first.id || card.id == second.id) {
                        card.copy(isFlipped = false)
                    } else {
                        card
                    }
                }
                _gameState.value = currentState.copy(cards = updatedCards)
            }

            // Reset for next pair of flips
            firstFlippedCard = null
            secondFlippedCard = null
            isProcessing = false
        }
    }

    /**
     * Called when all pairs are matched.
     * Calculates final score with bonuses, awards stars, and saves results.
     */
    private fun completeGame() {
        stopTimer()
        _isGameComplete.value = true

        val timeInMilliseconds = _timeElapsed.value
        val timeInSeconds = (timeInMilliseconds / 1000).toLong()
        val moves = _moves.value
        val points = _points.value

        // Determine star rating based on performance
        val stars = when {
            moves <= currentGridSize.totalCards && timeInSeconds <= 30 -> 3  // Excellent
            moves <= currentGridSize.totalCards * 1.5 && timeInSeconds <= 60 -> 2  // Good
            else -> 1  // Completed
        }

        // Award completion bonus based on stars
        val bonus = if (stars == 3) 500 else if (stars == 2) 200 else 0

        // Package up the final results
        _gameResult.value = GameResult(
            theme = currentTheme,
            gridSize = currentGridSize,
            moves = moves,
            timeTaken = timeInMilliseconds,
            points = points + bonus,
            stars = stars,
            bonus = bonus
        )

        // Save everything to database and sync to cloud
        saveGameData(stars, points + bonus, timeInSeconds.toInt(), moves)
    }

    /**
     * Saves the completed game to the database and syncs to Firestore.
     * Updates user stats, awards XP, checks achievements, and maintains streak.
     */
    private fun saveGameData(stars: Int, finalScore: Int, timeTaken: Int, moves: Int) {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                Log.d("GameViewModel", "💾 Saving adventure game data...")

                // Save this game session to the game results table
                val gameResultRepo = RepositoryProvider.getGameResultRepository()
                val gameId = gameResultRepo.createGameResult(
                    userId = userId,
                    gameMode = "ADVENTURE",
                    theme = currentTheme.name,
                    gridSize = "${currentGridSize.rows}x${currentGridSize.columns}",
                    difficulty = "NORMAL",
                    score = finalScore,
                    timeTaken = timeTaken,
                    moves = moves,
                    accuracy = calculateAccuracy(moves),
                    isWin = stars > 0
                )
                Log.d("GameViewModel", "✅ Game result saved: $gameId")

                // Update the player's overall statistics
                val userProfile = userProfileRepository.getUserProfile(userId)
                if (userProfile != null) {
                    userProfileRepository.updateUserStats(
                        userId = userId,
                        totalGames = userProfile.totalGamesPlayed + 1,
                        gamesWon = userProfile.gamesWon + if (stars > 0) 1 else 0,
                        currentStreak = userProfile.currentStreak,
                        bestStreak = userProfile.bestStreak,
                        avgTime = ((userProfile.averageCompletionTime * userProfile.totalGamesPlayed) + timeTaken) / (userProfile.totalGamesPlayed + 1),
                        accuracy = ((userProfile.accuracyRate * userProfile.totalGamesPlayed) + calculateAccuracy(moves)) / (userProfile.totalGamesPlayed + 1)
                    )
                    Log.d("GameViewModel", "✅ User profile stats updated")

                    // Award experience points and check for level up
                    val earnedXP = calculateXP(stars, finalScore, timeTaken)
                    val newTotalXP = userProfile.totalXP + earnedXP
                    val newLevel = calculateLevel(newTotalXP)

                    userProfileRepository.updateXPAndLevel(
                        userId = userId,
                        xp = newTotalXP,
                        level = newLevel
                    )

                    if (newLevel > userProfile.level) {
                        Log.d("GameViewModel", "🎉 LEVEL UP! Now level $newLevel")
                    }
                    Log.d("GameViewModel", "✅ Earned $earnedXP XP (Total: $newTotalXP, Level: $newLevel)")
                }

                // Check if player maintained their daily play streak
                userProfileRepository.updateDailyStreak(userId)
                Log.d("GameViewModel", "🔥 Daily streak updated")

                // Check if player unlocked any achievements
                checkAchievements(userId, stars, timeTaken, moves, finalScore)

                // Upload all changes to Firestore
                syncManager.syncToFirestore()
                Log.d("GameViewModel", "🔄 Firestore sync initiated")

            } catch (e: Exception) {
                Log.e("GameViewModel", "❌ Error saving game data: ${e.message}", e)
            }
        }
    }

    /**
     * Checks if the player earned any achievements from this game.
     * Awards achievements for first win, perfect performance, speed, and accuracy.
     */
    private suspend fun checkAchievements(userId: String, stars: Int, timeTaken: Int, moves: Int, finalScore: Int) {
        try {
            val achievementRepo = RepositoryProvider.getAchievementRepository()

            // Check if this is the player's first win
            val firstWin = achievementRepo.checkFirstWinAchievement(userId, stars > 0)
            if (firstWin) Log.d("GameViewModel", "🏆 First Victory achievement!")

            // Award achievement for perfect 3-star performance
            if (stars == 3) {
                val perfect = achievementRepo.awardAchievement(
                    userId = userId,
                    achievementType = "PERFECT_PERFORMANCE",
                    name = "Perfect Performance",
                    description = "Complete a level with 3 stars",
                    iconName = "ic_star"
                )
                if (perfect) Log.d("GameViewModel", "🏆 Perfect Performance achievement!")
            }

            // Check if player completed game quickly enough
            val speedDemon = achievementRepo.checkSpeedDemonAchievement(userId, timeTaken, 30)
            if (speedDemon) Log.d("GameViewModel", "🏆 Speed Demon achievement!")

            // Check if player achieved high accuracy
            val accuracy = calculateAccuracy(moves)
            val memoryGuru = achievementRepo.checkMemoryGuruAchievement(userId, accuracy, 95f)
            if (memoryGuru) Log.d("GameViewModel", "🏆 Memory Guru achievement!")

            // Run comprehensive achievement check for all other possible achievements
            achievementRepo.checkAllAchievements(
                userId = userId,
                score = finalScore,
                moves = moves,
                perfectMoves = currentGridSize.totalCards / 2,  // Number of pairs equals perfect moves
                timeTaken = timeTaken,
                accuracy = accuracy,
                isWin = stars > 0
            )

            Log.d("GameViewModel", "✅ Achievements checked")
        } catch (e: Exception) {
            Log.e("GameViewModel", "❌ Error checking achievements: ${e.message}", e)
        }
    }

    /**
     * Calculates how much experience points to award based on performance.
     * Factors in score, star rating, and completion time.
     */
    private fun calculateXP(stars: Int, score: Int, timeTaken: Int): Int {
        var xp = 0

        // Base XP from score achieved
        xp += (score / 10)

        // Bonus XP based on star rating
        xp += when (stars) {
            3 -> 100  // Perfect performance
            2 -> 50   // Good performance
            1 -> 25   // Completed
            else -> 0
        }

        // Additional bonus for fast completion
        if (timeTaken < 30) xp += 50
        else if (timeTaken < 60) xp += 25

        return xp
    }

    /**
     * Determines the player's level based on total XP accumulated.
     * Uses a square root formula for progressive leveling.
     */
    private fun calculateLevel(totalXP: Int): Int {
        // Level calculation using square root ensures higher levels need more XP
        // Level 1 = 0-99 XP
        // Level 2 = 100-399 XP
        // Level 3 = 400-899 XP
        // Level 4 = 900-1599 XP, and so on
        return (sqrt(totalXP.toDouble() / 100.0).toInt() + 1).coerceAtLeast(1)
    }

    /**
     * Calculates accuracy percentage based on moves taken.
     * Compares actual moves to the minimum possible moves.
     */
    private fun calculateAccuracy(moves: Int): Float {
        val perfectMoves = currentGridSize.totalCards
        return if (moves > 0) {
            ((perfectMoves.toFloat() / moves.toFloat()) * 100).coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    /**
     * Starts the game timer that counts up every second.
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timeElapsed.value += 1000
            }
        }
    }

    /**
     * Stops the game timer.
     */
    private fun stopTimer() {
        timerJob?.cancel()
    }

    /**
     * Restarts the current game with the same theme and grid size.
     */
    fun resetGame() {
        initializeGame(currentTheme, currentGridSize)
    }

    /**
     * Clean up resources when ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}