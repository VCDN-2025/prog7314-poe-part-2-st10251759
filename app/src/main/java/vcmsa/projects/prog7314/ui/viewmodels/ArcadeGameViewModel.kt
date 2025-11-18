package vcmsa.projects.prog7314.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.models.GameTheme
import vcmsa.projects.prog7314.data.models.GameProgress
import vcmsa.projects.prog7314.data.models.LevelData
import vcmsa.projects.prog7314.data.repository.ArcadeRepository
import vcmsa.projects.prog7314.data.repository.LevelRepository
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.data.repository.UserProfileRepository
import vcmsa.projects.prog7314.data.sync.FirestoreManager
import vcmsa.projects.prog7314.data.sync.SyncManager
import vcmsa.projects.prog7314.game.GameConfig
import vcmsa.projects.prog7314.game.GameEngine
import vcmsa.projects.prog7314.utils.AuthManager
import kotlin.math.sqrt

/*
    Code Attribution for: Creating ViewModels
    ===================================================
    Android Developers, 2019b. ViewModel Overview | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/libraries/architecture/viewmodel>
    [Accessed 18 November 2025].
*/

/**
 * ViewModel that manages the state and logic for the arcade memory game.
 * Handles game initialization, card flipping, scoring, and progress tracking.
 */
class ArcadeGameViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ArcadeGameViewModel"

    // Repository instances for accessing different data sources
    private val levelRepository: LevelRepository
    private val arcadeRepository: ArcadeRepository
    private val userProfileRepository: UserProfileRepository
    private val firestoreManager = FirestoreManager()
    private val syncManager: SyncManager

    // Core game components
    private var gameEngine: GameEngine? = null
    private var timerJob: Job? = null

    // Track current game settings
    private var currentLevelNumber: Int = 1
    private var isArcade: Boolean = false
    private var currentTheme: GameTheme? = null

    // Flag to prevent premature game completion before player makes first move
    private var hasGameStarted: Boolean = false

    // Game state that the UI observes - contains cards, moves, score, etc.
    private val _gameState = MutableStateFlow(GameEngine.GameState(
        cards = emptyList(),
        moves = 0,
        matchedPairs = 0,
        totalPairs = 0,
        score = 0,
        isComplete = false
    ))
    val gameState: StateFlow<GameEngine.GameState> = _gameState.asStateFlow()

    // Time tracking for the current game session
    private val _timeElapsed = MutableStateFlow(0)
    val timeElapsed: StateFlow<Int> = _timeElapsed.asStateFlow()

    // Countdown timer (used for timed game modes)
    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    // Current move count
    private val _moves = MutableStateFlow(0)
    val moves: StateFlow<Int> = _moves.asStateFlow()

    // Current score
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    // Whether the game has finished
    private val _isGameComplete = MutableStateFlow(false)
    val isGameComplete: StateFlow<Boolean> = _isGameComplete.asStateFlow()

    init {
        // Initialize the repository provider first before accessing any repositories
        RepositoryProvider.initialize(application)

        // Get repository instances from the provider
        levelRepository = RepositoryProvider.getLevelRepository()
        arcadeRepository = RepositoryProvider.getArcadeRepository()
        userProfileRepository = RepositoryProvider.getUserProfileRepository()
        syncManager = SyncManager(application)
    }

    /**
     * Sets up a new game with the specified level and mode.
     * Resets all game state and starts the timer.
     */
    fun initializeGame(levelNumber: Int, arcadeMode: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing game - Level: $levelNumber, Arcade: $arcadeMode")

                // Reset all game state variables to their starting values
                currentLevelNumber = levelNumber
                isArcade = arcadeMode
                hasGameStarted = false

                // Make sure completion flag is reset before starting
                _isGameComplete.value = false

                // Get configuration for this level (grid size, time limit, etc.)
                val config = GameConfig.getLevelConfig(levelNumber)

                // Pick a random visual theme for the cards
                val randomTheme = GameTheme.entries.random()
                currentTheme = randomTheme

                Log.d(TAG, "Theme: ${randomTheme.name}, Grid: ${config.gridRows}x${config.gridColumns}")

                // Create the game engine and set up the card grid
                gameEngine = GameEngine(randomTheme, config)
                gameEngine!!.initializeCards()

                // Update UI with initial game state
                _gameState.value = gameEngine!!.getGameState()
                _timeElapsed.value = 0
                _timeRemaining.value = config.timeLimit
                _moves.value = 0
                _score.value = 0

                // Start counting time
                startTimer(config.timeLimit)

                Log.d(TAG, "Game initialized with ${_gameState.value.cards.size} cards, isComplete: ${_isGameComplete.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing game: ${e.message}", e)
            }
        }
    }

    /**
     * Handles when a player clicks on a card.
     * Manages card flipping, matching logic, and game completion.
     */
    fun onCardClick(cardId: Int) {
        viewModelScope.launch {
            try {
                val engine = gameEngine ?: return@launch

                // Mark that the player has started playing (prevents instant completion)
                if (!hasGameStarted) {
                    hasGameStarted = true
                    Log.d(TAG, "Game started!")
                }

                // Attempt to flip the card and get the result
                val (success, result) = engine.flipCard(cardId)

                if (success) {
                    // Update the UI with new game state
                    updateGameState()

                    when (result) {
                        GameEngine.FlipResult.MATCH -> {
                            // Cards matched - wait briefly for animation, then clear them
                            delay(500)
                            engine.clearMatchedCards()
                            updateGameState()

                            // Check if all pairs are matched and game has actually started
                            if (engine.isGameComplete && hasGameStarted) {
                                onGameComplete()
                            }
                        }
                        GameEngine.FlipResult.NO_MATCH -> {
                            // Cards didn't match - wait for player to see them, then flip back
                            delay(1000)
                            engine.resetFlippedCards()
                            updateGameState()
                        }
                        else -> {
                            // Only one card flipped so far - waiting for second card
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling card click: ${e.message}", e)
            }
        }
    }

    /**
     * Updates all state flows with current values from the game engine.
     */
    private fun updateGameState() {
        val engine = gameEngine ?: return
        val state = engine.getGameState()

        _gameState.value = state
        _moves.value = state.moves
        _score.value = state.score
    }

    /**
     * Starts the game timer that counts up and optionally counts down.
     * Automatically completes the game if time runs out.
     */
    private fun startTimer(timeLimit: Int) {
        // Cancel any existing timer first
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            // Keep counting until game is complete
            while (!_isGameComplete.value) {
                delay(1000)
                _timeElapsed.value++

                // Handle countdown timer if this level has a time limit
                if (timeLimit > 0) {
                    val remaining = timeLimit - _timeElapsed.value
                    _timeRemaining.value = remaining.coerceAtLeast(0)

                    // Time's up - end the game if it has started
                    if (remaining <= 0 && hasGameStarted) {
                        onGameComplete()
                        break
                    }
                }
            }
        }
    }

    /**
     * Called when the game ends (either by completion or timeout).
     * Saves results, updates user stats, awards XP, and syncs to cloud.
     */
    private suspend fun onGameComplete() {
        try {
            // Prevent this from running multiple times
            if (_isGameComplete.value) {
                Log.d(TAG, "Game already complete, skipping...")
                return
            }

            Log.d(TAG, "Game complete!")

            // Stop the timer
            timerJob?.cancel()
            _isGameComplete.value = true

            // Get the current user and game details
            val userId = AuthManager.getCurrentUser()?.uid ?: return
            val finalScore = getFinalScore()
            val config = GameConfig.getLevelConfig(currentLevelNumber)
            val theme = currentTheme?.name ?: "Unknown"
            val gridSize = "${config.gridRows}x${config.gridColumns}"

            // Save this game session to the database for statistics tracking
            val gameResultRepo = RepositoryProvider.getGameResultRepository()
            gameResultRepo.createGameResult(
                userId = userId,
                gameMode = if (isArcade) "ARCADE" else "LEVEL",
                theme = theme,
                gridSize = gridSize,
                difficulty = config.difficulty.name,
                score = finalScore.finalScore,
                timeTaken = _timeElapsed.value,
                moves = finalScore.moves,
                accuracy = calculateAccuracy(finalScore.moves, config.totalPairs),
                isWin = finalScore.stars > 0
            )
            Log.d(TAG, "Game result saved for statistics")

            // Update player's overall stats and award experience points
            updateUserProfileStatsAndXP(userId, finalScore.stars > 0, finalScore.stars, finalScore.finalScore)

            // Check if player maintained their daily play streak
            updateDailyStreak()

            // Check if player earned any new achievements
            checkAchievements(userId, finalScore)

            if (isArcade) {
                // Save arcade-specific session data
                arcadeRepository.saveArcadeSession(
                    userId = userId,
                    theme = theme,
                    gridSize = gridSize,
                    difficulty = config.difficulty.displayName,
                    score = finalScore.finalScore,
                    timeTaken = _timeElapsed.value,
                    moves = finalScore.moves,
                    bonus = finalScore.timeBonus,
                    stars = finalScore.stars
                )
                Log.d(TAG, "Arcade session saved to RoomDB")
            } else {
                // Mark level as complete and unlock the next one
                levelRepository.completeLevelAndUnlockNext(
                    userId = userId,
                    levelNumber = currentLevelNumber,
                    stars = finalScore.stars,
                    score = finalScore.finalScore,
                    time = _timeElapsed.value,
                    moves = finalScore.moves
                )
                Log.d(TAG, "Level $currentLevelNumber completed in RoomDB")

                // Upload level progress to cloud storage
                syncProgressToFirestore(userId)
            }

            // Sync all local data changes to Firestore
            syncToFirestore()

        } catch (e: Exception) {
            Log.e(TAG, "Error completing game: ${e.message}", e)
        }
    }

    /**
     * Updates the player's profile statistics and awards experience points.
     * Checks for level ups based on total XP earned.
     */
    private suspend fun updateUserProfileStatsAndXP(userId: String, isWin: Boolean, stars: Int, score: Int) {
        try {
            val userProfile = userProfileRepository.getUserProfile(userId)
            if (userProfile != null) {
                // Update basic game statistics
                userProfileRepository.updateUserStats(
                    userId = userId,
                    totalGames = userProfile.totalGamesPlayed + 1,
                    gamesWon = userProfile.gamesWon + if (isWin) 1 else 0,
                    currentStreak = userProfile.currentStreak,
                    bestStreak = userProfile.bestStreak,
                    avgTime = ((userProfile.averageCompletionTime * userProfile.totalGamesPlayed) + _timeElapsed.value) / (userProfile.totalGamesPlayed + 1),
                    accuracy = userProfile.accuracyRate
                )
                Log.d(TAG, "User profile stats updated")

                // Calculate experience points earned from this game
                val earnedXP = calculateXP(stars, score, _timeElapsed.value)
                val newTotalXP = userProfile.totalXP + earnedXP
                val newLevel = calculateLevel(newTotalXP)

                // Save new XP and level
                userProfileRepository.updateXPAndLevel(
                    userId = userId,
                    xp = newTotalXP,
                    level = newLevel
                )

                // Check if player leveled up
                if (newLevel > userProfile.level) {
                    Log.d(TAG, "LEVEL UP! Now level $newLevel")
                }
                Log.d(TAG, "Earned $earnedXP XP (Total: $newTotalXP, Level: $newLevel)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile: ${e.message}", e)
        }
    }

    /**
     * Calculates how much experience points to award based on performance.
     * Better performance (more stars, higher score, faster time) = more XP.
     */
    private fun calculateXP(stars: Int, score: Int, timeTaken: Int): Int {
        var xp = 0

        // Award XP based on score achieved
        xp += (score / 10)

        // Bonus XP for star rating
        xp += when (stars) {
            3 -> 100  // Perfect game
            2 -> 50   // Good game
            1 -> 25   // Completed but could be better
            else -> 0 // Failed or incomplete
        }

        // Bonus for completing quickly
        if (timeTaken < 30) xp += 50       // Very fast
        else if (timeTaken < 60) xp += 25  // Fast

        return xp
    }

    /**
     * Determines the player's level based on total XP.
     * Uses a square root formula so higher levels require progressively more XP.
     */
    private fun calculateLevel(totalXP: Int): Int {
        // Level calculation: Level = sqrt(XP / 100) + 1
        // This means: Level 1 needs 0 XP, Level 2 needs 100 XP, Level 3 needs 400 XP, etc.
        return (sqrt(totalXP.toDouble() / 100.0).toInt() + 1).coerceAtLeast(1)
    }

    /**
     * Updates whether the player played today to maintain their daily streak.
     */
    private fun updateDailyStreak() {
        viewModelScope.launch {
            try {
                val userId = AuthManager.getCurrentUser()?.uid
                if (userId != null) {
                    val success = userProfileRepository.updateDailyStreak(userId)
                    if (success) {
                        Log.d(TAG, "Daily streak updated successfully!")
                    } else {
                        Log.w(TAG, "Failed to update daily streak")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating streak: ${e.message}", e)
            }
        }
    }

    /**
     * Checks if the player earned any achievements from this game.
     * The repository method checks all possible achievements automatically.
     */
    private suspend fun checkAchievements(userId: String, finalScore: GameEngine.FinalScore) {
        try {
            val achievementRepo = RepositoryProvider.getAchievementRepository()
            val config = GameConfig.getLevelConfig(currentLevelNumber)
            val accuracy = calculateAccuracy(finalScore.moves, config.totalPairs)

            // Check all achievements in one call (handles First Win, Speed Demon, Memory Guru, etc.)
            achievementRepo.checkAllAchievements(
                userId = userId,
                score = finalScore.finalScore,
                moves = finalScore.moves,
                perfectMoves = config.totalPairs,
                timeTaken = _timeElapsed.value,
                accuracy = accuracy,
                isWin = finalScore.stars > 0
            )

            Log.d(TAG, "All achievements checked")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking achievements: ${e.message}", e)
        }
    }

    /**
     * Triggers a background sync of all local data to Firestore cloud storage.
     */
    private fun syncToFirestore() {
        try {
            Log.d(TAG, "Syncing all data to Firestore...")
            syncManager.syncToFirestore()
            Log.d(TAG, "Firestore sync initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to Firestore: ${e.message}", e)
        }
    }

    /**
     * Calculates accuracy percentage based on moves taken vs perfect moves.
     * Perfect accuracy = completing in minimum possible moves.
     */
    private fun calculateAccuracy(moves: Int, totalPairs: Int): Float {
        val perfectMoves = totalPairs * 2 // Each pair requires exactly 2 flips
        return if (moves > 0) {
            ((perfectMoves.toFloat() / moves.toFloat()) * 100).coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    /**
     * Uploads the player's level progress data to Firestore.
     * Includes all completed levels, stars earned, and best scores.
     */
    private suspend fun syncProgressToFirestore(userId: String) {
        try {
            Log.d(TAG, "Syncing progress to Firestore...")

            // Get all level data from local database
            val allLevels = levelRepository.getAllLevelsProgress(userId)

            // Convert to the format needed for Firestore
            val levelProgressMap = allLevels.associate { level ->
                level.levelNumber to LevelData(
                    levelNumber = level.levelNumber,
                    stars = level.stars,
                    bestScore = level.bestScore,
                    bestTime = level.bestTime,
                    bestMoves = level.bestMoves,
                    isUnlocked = level.isUnlocked,
                    isCompleted = level.isCompleted,
                    timesPlayed = level.timesPlayed
                )
            }

            // Determine highest unlocked level
            val currentLevel = allLevels.filter { it.isUnlocked }.maxOfOrNull { it.levelNumber } ?: 1

            // Count how many levels were completed
            val completedCount = allLevels.count { it.isCompleted }

            // Package everything into a progress object
            val gameProgress = GameProgress(
                userId = userId,
                currentLevel = currentLevel,
                levelProgress = levelProgressMap,
                unlockedCategories = listOf("Animals", "Fruits"),
                totalGamesPlayed = allLevels.sumOf { it.timesPlayed },
                gamesWon = completedCount
            )

            // Upload to Firestore
            val result = firestoreManager.saveGameProgress(gameProgress)
            if (result.isSuccess) {
                Log.d(TAG, "Progress synced to Firestore! Level: $currentLevel, Completed: $completedCount")
            } else {
                Log.e(TAG, "Firestore sync failed: ${result.exceptionOrNull()?.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to Firestore: ${e.message}", e)
        }
    }

    /**
     * Calculates the final score with bonuses and star rating.
     * Returns a complete breakdown of the player's performance.
     */
    fun getFinalScore(): GameEngine.FinalScore {
        val engine = gameEngine ?: return GameEngine.FinalScore(0, 0, 0, 0, 0, 0)
        val config = GameConfig.getLevelConfig(currentLevelNumber)
        val timeRemaining = if (config.timeLimit > 0) _timeRemaining.value else 0
        return engine.calculateFinalScore(timeRemaining)
    }

    /**
     * Clean up resources when ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}