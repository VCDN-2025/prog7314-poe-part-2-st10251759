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

class ArcadeGameViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ArcadeGameViewModel"

    private val levelRepository: LevelRepository
    private val arcadeRepository: ArcadeRepository
    private val userProfileRepository: UserProfileRepository
    private val firestoreManager = FirestoreManager()
    private val syncManager: SyncManager

    private var gameEngine: GameEngine? = null
    private var timerJob: Job? = null
    private var currentLevelNumber: Int = 1
    private var isArcade: Boolean = false
    private var currentTheme: GameTheme? = null

    // FIXED: Add flag to track if game has actually started
    private var hasGameStarted: Boolean = false

    private val _gameState = MutableStateFlow(GameEngine.GameState(
        cards = emptyList(),
        moves = 0,
        matchedPairs = 0,
        totalPairs = 0,
        score = 0,
        isComplete = false
    ))
    val gameState: StateFlow<GameEngine.GameState> = _gameState.asStateFlow()

    private val _timeElapsed = MutableStateFlow(0)
    val timeElapsed: StateFlow<Int> = _timeElapsed.asStateFlow()

    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _moves = MutableStateFlow(0)
    val moves: StateFlow<Int> = _moves.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _isGameComplete = MutableStateFlow(false)
    val isGameComplete: StateFlow<Boolean> = _isGameComplete.asStateFlow()

    init {
        RepositoryProvider.initialize(application)  // 🔥 INITIALIZE FIRST
        levelRepository = RepositoryProvider.getLevelRepository()  // ✅ HAS CONTEXT
        arcadeRepository = RepositoryProvider.getArcadeRepository()
        userProfileRepository = RepositoryProvider.getUserProfileRepository()
        syncManager = SyncManager(application)
    }

    fun initializeGame(levelNumber: Int, arcadeMode: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing game - Level: $levelNumber, Arcade: $arcadeMode")

                // FIXED: Reset ALL game state flags at the start
                currentLevelNumber = levelNumber
                isArcade = arcadeMode
                hasGameStarted = false

                // CRITICAL FIX: Reset completion state IMMEDIATELY
                _isGameComplete.value = false

                val config = GameConfig.getLevelConfig(levelNumber)
                val randomTheme = GameTheme.entries.random()
                currentTheme = randomTheme

                Log.d(TAG, "Theme: ${randomTheme.name}, Grid: ${config.gridRows}x${config.gridColumns}")

                gameEngine = GameEngine(randomTheme, config)
                gameEngine!!.initializeCards()

                _gameState.value = gameEngine!!.getGameState()
                _timeElapsed.value = 0
                _timeRemaining.value = config.timeLimit
                _moves.value = 0
                _score.value = 0

                startTimer(config.timeLimit)

                Log.d(TAG, "✅ Game initialized with ${_gameState.value.cards.size} cards, isComplete: ${_isGameComplete.value}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error initializing game: ${e.message}", e)
            }
        }
    }

    fun onCardClick(cardId: Int) {
        viewModelScope.launch {
            try {
                val engine = gameEngine ?: return@launch

                // FIXED: Mark game as started on first card click
                if (!hasGameStarted) {
                    hasGameStarted = true
                    Log.d(TAG, "Game started!")
                }

                val (success, result) = engine.flipCard(cardId)

                if (success) {
                    updateGameState()

                    when (result) {
                        GameEngine.FlipResult.MATCH -> {
                            delay(500)
                            engine.clearMatchedCards()
                            updateGameState()

                            // FIXED: Only complete if game has started
                            if (engine.isGameComplete && hasGameStarted) {
                                onGameComplete()
                            }
                        }
                        GameEngine.FlipResult.NO_MATCH -> {
                            delay(1000)
                            engine.resetFlippedCards()
                            updateGameState()
                        }
                        else -> {
                            // Single card flipped
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling card click: ${e.message}", e)
            }
        }
    }

    private fun updateGameState() {
        val engine = gameEngine ?: return
        val state = engine.getGameState()

        _gameState.value = state
        _moves.value = state.moves
        _score.value = state.score
    }

    private fun startTimer(timeLimit: Int) {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (!_isGameComplete.value) {
                delay(1000)
                _timeElapsed.value++

                if (timeLimit > 0) {
                    val remaining = timeLimit - _timeElapsed.value
                    _timeRemaining.value = remaining.coerceAtLeast(0)

                    // FIXED: Only complete if game has started
                    if (remaining <= 0 && hasGameStarted) {
                        onGameComplete()
                        break
                    }
                }
            }
        }
    }

    private suspend fun onGameComplete() {
        try {
            // FIXED: Prevent multiple completion calls
            if (_isGameComplete.value) {
                Log.d(TAG, "Game already complete, skipping...")
                return
            }

            Log.d(TAG, "Game complete!")
            timerJob?.cancel()
            _isGameComplete.value = true

            val userId = AuthManager.getCurrentUser()?.uid ?: return
            val finalScore = getFinalScore()
            val config = GameConfig.getLevelConfig(currentLevelNumber)
            val theme = currentTheme?.name ?: "Unknown"
            val gridSize = "${config.gridRows}x${config.gridColumns}"

            // ✅ ALWAYS SAVE TO GAME RESULTS (for Statistics)
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
            Log.d(TAG, "✅ Game result saved for statistics")

            // 🔥 UPDATE USER PROFILE STATS + XP
            updateUserProfileStatsAndXP(userId, finalScore.stars > 0, finalScore.stars, finalScore.finalScore)

            // 🔥 UPDATE DAILY STREAK
            updateDailyStreak()

            // ✅ CHECK FOR ACHIEVEMENTS (FIXED: Only call once!)
            checkAchievements(userId, finalScore)

            if (isArcade) {
                // Save arcade session to RoomDB
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
                Log.d(TAG, "✅ Arcade session saved to RoomDB")
            } else {
                // Complete level in RoomDB and unlock next
                levelRepository.completeLevelAndUnlockNext(
                    userId = userId,
                    levelNumber = currentLevelNumber,
                    stars = finalScore.stars,
                    score = finalScore.finalScore,
                    time = _timeElapsed.value,
                    moves = finalScore.moves
                )
                Log.d(TAG, "✅ Level $currentLevelNumber completed in RoomDB")

                // NOW SAVE TO FIRESTORE
                syncProgressToFirestore(userId)
            }

            // 🔥 SYNC EVERYTHING TO FIRESTORE
            syncToFirestore()

        } catch (e: Exception) {
            Log.e(TAG, "Error completing game: ${e.message}", e)
        }
    }

    /**
     * 🔥 NEW: Update user profile statistics AND award XP
     */
    private suspend fun updateUserProfileStatsAndXP(userId: String, isWin: Boolean, stars: Int, score: Int) {
        try {
            val userProfile = userProfileRepository.getUserProfile(userId)
            if (userProfile != null) {
                // Update basic stats
                userProfileRepository.updateUserStats(
                    userId = userId,
                    totalGames = userProfile.totalGamesPlayed + 1,
                    gamesWon = userProfile.gamesWon + if (isWin) 1 else 0,
                    currentStreak = userProfile.currentStreak,
                    bestStreak = userProfile.bestStreak,
                    avgTime = ((userProfile.averageCompletionTime * userProfile.totalGamesPlayed) + _timeElapsed.value) / (userProfile.totalGamesPlayed + 1),
                    accuracy = userProfile.accuracyRate
                )
                Log.d(TAG, "✅ User profile stats updated")

                // 🔥 AWARD XP AND CHECK LEVEL UP
                val earnedXP = calculateXP(stars, score, _timeElapsed.value)
                val newTotalXP = userProfile.totalXP + earnedXP
                val newLevel = calculateLevel(newTotalXP)

                userProfileRepository.updateXPAndLevel(
                    userId = userId,
                    xp = newTotalXP,
                    level = newLevel
                )

                if (newLevel > userProfile.level) {
                    Log.d(TAG, "🎉 LEVEL UP! Now level $newLevel")
                }
                Log.d(TAG, "✅ Earned $earnedXP XP (Total: $newTotalXP, Level: $newLevel)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating user profile: ${e.message}", e)
        }
    }

    /**
     * 🔥 NEW: Calculate XP earned from game performance
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
     * 🔥 NEW: Calculate level from total XP
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
     * Update the user's daily streak
     */
    private fun updateDailyStreak() {
        viewModelScope.launch {
            try {
                val userId = AuthManager.getCurrentUser()?.uid
                if (userId != null) {
                    val success = userProfileRepository.updateDailyStreak(userId)
                    if (success) {
                        Log.d(TAG, "🔥 Daily streak updated successfully!")
                    } else {
                        Log.w(TAG, "⚠️ Failed to update daily streak")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error updating streak: ${e.message}", e)
            }
        }
    }

    /**
     * Check and award achievements
     * 🔥 FIXED: Only call checkAllAchievements once - it checks everything!
     */
    private suspend fun checkAchievements(userId: String, finalScore: GameEngine.FinalScore) {
        try {
            val achievementRepo = RepositoryProvider.getAchievementRepository()
            val config = GameConfig.getLevelConfig(currentLevelNumber)
            val accuracy = calculateAccuracy(finalScore.moves, config.totalPairs)

            // 🔥 ONLY USE checkAllAchievements - it checks EVERYTHING (First Win, Speed Demon, Memory Guru, etc.)
            achievementRepo.checkAllAchievements(
                userId = userId,
                score = finalScore.finalScore,
                moves = finalScore.moves,
                perfectMoves = config.totalPairs,
                timeTaken = _timeElapsed.value,
                accuracy = accuracy,
                isWin = finalScore.stars > 0
            )

            Log.d(TAG, "✅ All achievements checked")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking achievements: ${e.message}", e)
        }
    }

    /**
     * Sync all data to Firestore
     */
    private fun syncToFirestore() {
        try {
            Log.d(TAG, "🔄 Syncing all data to Firestore...")
            syncManager.syncToFirestore()
            Log.d(TAG, "✅ Firestore sync initiated")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing to Firestore: ${e.message}", e)
        }
    }

    private fun calculateAccuracy(moves: Int, totalPairs: Int): Float {
        val perfectMoves = totalPairs * 2 // Minimum moves needed
        return if (moves > 0) {
            ((perfectMoves.toFloat() / moves.toFloat()) * 100).coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    private suspend fun syncProgressToFirestore(userId: String) {
        try {
            Log.d(TAG, "🔄 Syncing progress to Firestore...")

            // Get all level progress from RoomDB
            val allLevels = levelRepository.getAllLevelsProgress(userId)

            // Convert to LevelData map
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

            // Find highest unlocked level
            val currentLevel = allLevels.filter { it.isUnlocked }.maxOfOrNull { it.levelNumber } ?: 1

            // Count completed levels
            val completedCount = allLevels.count { it.isCompleted }

            // Create GameProgress object
            val gameProgress = GameProgress(
                userId = userId,
                currentLevel = currentLevel,
                levelProgress = levelProgressMap,
                unlockedCategories = listOf("Animals", "Fruits"),
                totalGamesPlayed = allLevels.sumOf { it.timesPlayed },
                gamesWon = completedCount
            )

            // Save to Firestore
            val result = firestoreManager.saveGameProgress(gameProgress)
            if (result.isSuccess) {
                Log.d(TAG, "✅ Progress synced to Firestore! Level: $currentLevel, Completed: $completedCount")
            } else {
                Log.e(TAG, "❌ Firestore sync failed: ${result.exceptionOrNull()?.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing to Firestore: ${e.message}", e)
        }
    }

    fun getFinalScore(): GameEngine.FinalScore {
        val engine = gameEngine ?: return GameEngine.FinalScore(0, 0, 0, 0, 0, 0)
        val config = GameConfig.getLevelConfig(currentLevelNumber)
        val timeRemaining = if (config.timeLimit > 0) _timeRemaining.value else 0
        return engine.calculateFinalScore(timeRemaining)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}