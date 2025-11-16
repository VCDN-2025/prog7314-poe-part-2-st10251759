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

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _timeElapsed = MutableStateFlow(0L)
    val timeElapsed: StateFlow<Long> = _timeElapsed.asStateFlow()

    private val _moves = MutableStateFlow(0)
    val moves: StateFlow<Int> = _moves.asStateFlow()

    private val _points = MutableStateFlow(0)
    val points: StateFlow<Int> = _points.asStateFlow()

    private val _isGameComplete = MutableStateFlow(false)
    val isGameComplete: StateFlow<Boolean> = _isGameComplete.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()

    private var timerJob: Job? = null
    private var firstFlippedCard: GameCard? = null
    private var secondFlippedCard: GameCard? = null
    private var isProcessing = false

    private lateinit var currentTheme: GameTheme
    private lateinit var currentGridSize: GridSize

    private val userProfileRepository: UserProfileRepository
    private val syncManager: SyncManager

    init {
        val database = AppDatabase.getDatabase(application)
        userProfileRepository = UserProfileRepository(database.userProfileDao())
        syncManager = SyncManager(application)
    }

    fun initializeGame(theme: GameTheme, gridSize: GridSize) {
        currentTheme = theme
        currentGridSize = gridSize

        val cards = generateCards(theme, gridSize)

        _gameState.value = GameState(
            cards = cards,
            moves = 0,
            matchedPairs = 0,
            points = 0,
            timeElapsed = 0,
            isComplete = false
        )

        _timeElapsed.value = 0L
        _moves.value = 0
        _points.value = 0
        _isGameComplete.value = false
        _gameResult.value = null

        startTimer()
    }

    private fun generateCards(theme: GameTheme, gridSize: GridSize): List<GameCard> {
        val totalPairs = gridSize.totalCards / 2
        val availableImages = theme.cardImages

        val selectedImages = if (availableImages.size >= totalPairs) {
            availableImages.shuffled().take(totalPairs)
        } else {
            (availableImages + availableImages).shuffled().take(totalPairs)
        }

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

        return cards.shuffled()
    }

    fun onCardClicked(card: GameCard) {
        if (isProcessing || card.isFlipped || card.isMatched) return

        val currentState = _gameState.value ?: return
        val updatedCards = currentState.cards.toMutableList()
        val cardIndex = updatedCards.indexOfFirst { it.id == card.id }

        if (cardIndex == -1) return

        updatedCards[cardIndex] = updatedCards[cardIndex].copy(isFlipped = true)
        _gameState.value = currentState.copy(cards = updatedCards)

        when {
            firstFlippedCard == null -> {
                firstFlippedCard = updatedCards[cardIndex]
            }
            secondFlippedCard == null -> {
                secondFlippedCard = updatedCards[cardIndex]
                _moves.value += 1
                checkForMatch()
            }
        }
    }

    private fun checkForMatch() {
        val first = firstFlippedCard ?: return
        val second = secondFlippedCard ?: return

        isProcessing = true

        viewModelScope.launch {
            delay(800)

            val currentState = _gameState.value ?: return@launch
            val updatedCards = currentState.cards.toMutableList()

            if (first.pairId == second.pairId) {
                updatedCards.replaceAll { card ->
                    if (card.id == first.id || card.id == second.id) {
                        card.copy(isMatched = true, isFlipped = true)
                    } else {
                        card
                    }
                }

                val basePoints = 100
                val timeBonus = maxOf(0, 50 - (_timeElapsed.value.toInt() / 1000))
                _points.value += basePoints + timeBonus

                val matchedPairs = currentState.matchedPairs + 1
                _gameState.value = currentState.copy(
                    cards = updatedCards,
                    matchedPairs = matchedPairs
                )

                if (matchedPairs == currentGridSize.totalCards / 2) {
                    completeGame()
                }
            } else {
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

            firstFlippedCard = null
            secondFlippedCard = null
            isProcessing = false
        }
    }

    private fun completeGame() {
        stopTimer()
        _isGameComplete.value = true

        val timeInMilliseconds = _timeElapsed.value
        val timeInSeconds = (timeInMilliseconds / 1000).toLong()
        val moves = _moves.value
        val points = _points.value

        val stars = when {
            moves <= currentGridSize.totalCards && timeInSeconds <= 30 -> 3
            moves <= currentGridSize.totalCards * 1.5 && timeInSeconds <= 60 -> 2
            else -> 1
        }

        val bonus = if (stars == 3) 500 else if (stars == 2) 200 else 0

        _gameResult.value = GameResult(
            theme = currentTheme,
            gridSize = currentGridSize,
            moves = moves,
            timeTaken = timeInMilliseconds,
            points = points + bonus,
            stars = stars,
            bonus = bonus
        )

        // 🔥 SAVE ALL DATA
        saveGameData(stars, points + bonus, timeInSeconds.toInt(), moves)
    }

    /**
     * Save all game data to database and sync to cloud
     */
    private fun saveGameData(stars: Int, finalScore: Int, timeTaken: Int, moves: Int) {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                Log.d("GameViewModel", "💾 Saving adventure game data...")

                // 1. Save game result
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

                // 2. Update user profile stats
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

                    // 2.5 🔥 ADD XP AND CHECK LEVEL UP
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

                // 3. Update daily streak
                userProfileRepository.updateDailyStreak(userId)
                Log.d("GameViewModel", "🔥 Daily streak updated")

                // 4. Check achievements
                checkAchievements(userId, stars, timeTaken, moves, finalScore)

                // 5. 🔥 SYNC TO FIRESTORE
                syncManager.syncToFirestore()
                Log.d("GameViewModel", "🔄 Firestore sync initiated")

            } catch (e: Exception) {
                Log.e("GameViewModel", "❌ Error saving game data: ${e.message}", e)
            }
        }
    }

    /**
     * Check and award achievements
     */
    private suspend fun checkAchievements(userId: String, stars: Int, timeTaken: Int, moves: Int, finalScore: Int) {
        try {
            val achievementRepo = RepositoryProvider.getAchievementRepository()

            // First Win
            val firstWin = achievementRepo.checkFirstWinAchievement(userId, stars > 0)
            if (firstWin) Log.d("GameViewModel", "🏆 First Victory achievement!")

            // Perfect Performance (3 stars)
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

            // Speed Demon
            val speedDemon = achievementRepo.checkSpeedDemonAchievement(userId, timeTaken, 30)
            if (speedDemon) Log.d("GameViewModel", "🏆 Speed Demon achievement!")

            // Memory Guru (95%+ accuracy)
            val accuracy = calculateAccuracy(moves)
            val memoryGuru = achievementRepo.checkMemoryGuruAchievement(userId, accuracy, 95f)
            if (memoryGuru) Log.d("GameViewModel", "🏆 Memory Guru achievement!")

            // 🔥 CHECK ALL OTHER ACHIEVEMENTS
            achievementRepo.checkAllAchievements(
                userId = userId,
                score = finalScore,
                moves = moves,
                perfectMoves = currentGridSize.totalCards / 2,  // ✅ CORRECT (number of pairs)
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
     * Calculate XP earned from game performance
     */
    private fun calculateXP(stars: Int, score: Int, timeTaken: Int): Int {
        var xp = 0

        // Base XP from score
        xp += (score / 10) // 100 score = 10 XP

        // Star bonus
        xp += when (stars) {
            3 -> 100  // Perfect performance
            2 -> 50   // Good performance
            1 -> 25   // Completed
            else -> 0
        }

        // Time bonus (faster = more XP)
        if (timeTaken < 30) xp += 50
        else if (timeTaken < 60) xp += 25

        return xp
    }

    /**
     * Calculate level from total XP
     */
    private fun calculateLevel(totalXP: Int): Int {
        // Simple level formula: Level = sqrt(XP / 100)
        // Level 1 = 0-99 XP
        // Level 2 = 100-399 XP
        // Level 3 = 400-899 XP
        // Level 4 = 900-1599 XP, etc.
        return (sqrt(totalXP.toDouble() / 100.0).toInt() + 1).coerceAtLeast(1)
    }

    /**
     * Calculate accuracy percentage
     */
    private fun calculateAccuracy(moves: Int): Float {
        val perfectMoves = currentGridSize.totalCards
        return if (moves > 0) {
            ((perfectMoves.toFloat() / moves.toFloat()) * 100).coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timeElapsed.value += 1000
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun resetGame() {
        initializeGame(currentTheme, currentGridSize)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}