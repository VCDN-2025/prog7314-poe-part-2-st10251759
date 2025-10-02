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
import vcmsa.projects.prog7314.data.AppDatabase
import vcmsa.projects.prog7314.data.models.GameTheme // FIXED: Import GameTheme enum
import vcmsa.projects.prog7314.data.repository.ArcadeRepository
import vcmsa.projects.prog7314.data.repository.LevelRepository
import vcmsa.projects.prog7314.game.GameConfig
import vcmsa.projects.prog7314.game.GameEngine
import vcmsa.projects.prog7314.utils.AuthManager

class ArcadeGameViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ArcadeGameViewModel"

    private val levelRepository: LevelRepository
    private val arcadeRepository: ArcadeRepository

    private var gameEngine: GameEngine? = null
    private var timerJob: Job? = null
    private var currentLevelNumber: Int = 1
    private var isArcade: Boolean = false

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
        val database = AppDatabase.getDatabase(application)
        levelRepository = LevelRepository(database.levelProgressDao())
        arcadeRepository = ArcadeRepository(database.arcadeSessionDao())
    }

    /**
     * Initialize game with level or arcade mode
     */
    fun initializeGame(levelNumber: Int, arcadeMode: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing game - Level: $levelNumber, Arcade: $arcadeMode")

                currentLevelNumber = levelNumber
                isArcade = arcadeMode

                // Get level configuration
                val config = GameConfig.getLevelConfig(levelNumber)

                // FIXED: GameTheme is an enum, use .entries or .values()
                val randomTheme = GameTheme.entries.random()

                // FIXED: Use themeName instead of name or displayName
                Log.d(TAG, "Theme: ${randomTheme.themeName}, Grid: ${config.gridRows}x${config.gridColumns}")

                // Initialize game engine
                gameEngine = GameEngine(randomTheme, config)
                val cards = gameEngine!!.initializeCards()

                // Update UI state
                _gameState.value = gameEngine!!.getGameState()
                _timeElapsed.value = 0
                _timeRemaining.value = config.timeLimit
                _moves.value = 0
                _score.value = 0
                _isGameComplete.value = false

                // Start timer
                startTimer(config.timeLimit)

                Log.d(TAG, "✅ Game initialized with ${cards.size} cards")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error initializing game: ${e.message}", e)
            }
        }
    }

    /**
     * Handle card click
     */
    fun onCardClick(cardId: Int) {
        viewModelScope.launch {
            try {
                val engine = gameEngine ?: return@launch

                val (success, result) = engine.flipCard(cardId)

                if (success) {
                    // Update UI state immediately
                    updateGameState()

                    when (result) {
                        GameEngine.FlipResult.MATCH -> {
                            // Match found - clear after short delay
                            delay(500)
                            engine.clearMatchedCards()
                            updateGameState()

                            // Check if game is complete
                            if (engine.isGameComplete) {
                                onGameComplete()
                            }
                        }
                        GameEngine.FlipResult.NO_MATCH -> {
                            // No match - flip back after delay
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

    /**
     * Update game state from engine
     */
    private fun updateGameState() {
        val engine = gameEngine ?: return
        val state = engine.getGameState()

        _gameState.value = state
        _moves.value = state.moves
        _score.value = state.score
    }

    /**
     * Start game timer
     */
    private fun startTimer(timeLimit: Int) {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (!_isGameComplete.value) {
                delay(1000)
                _timeElapsed.value++

                if (timeLimit > 0) {
                    val remaining = timeLimit - _timeElapsed.value
                    _timeRemaining.value = remaining.coerceAtLeast(0)

                    // Check time limit
                    if (remaining <= 0) {
                        onGameComplete()
                        break
                    }
                }
            }
        }
    }

    /**
     * Handle game completion
     */
    private suspend fun onGameComplete() {
        try {
            Log.d(TAG, "Game complete!")
            timerJob?.cancel()
            _isGameComplete.value = true

            // FIXED: AuthManager is an object
            val userId = AuthManager.getCurrentUser()?.uid ?: return
            val finalScore = getFinalScore()
            val config = GameConfig.getLevelConfig(currentLevelNumber)

            if (isArcade) {
                // Save arcade session
                arcadeRepository.saveArcadeSession(
                    userId = userId,
                    theme = "Random", // You can track actual theme if needed
                    gridSize = "${config.gridRows}x${config.gridColumns}",
                    difficulty = config.difficulty.displayName,
                    score = finalScore.finalScore,
                    timeTaken = _timeElapsed.value,
                    moves = finalScore.moves,
                    bonus = finalScore.timeBonus,
                    stars = finalScore.stars
                )
                Log.d(TAG, "✅ Arcade session saved")
            } else {
                // Complete level and unlock next
                levelRepository.completeLevelAndUnlockNext(
                    userId = userId,
                    levelNumber = currentLevelNumber,
                    stars = finalScore.stars,
                    score = finalScore.finalScore,
                    time = _timeElapsed.value,
                    moves = finalScore.moves
                )
                Log.d(TAG, "✅ Level $currentLevelNumber completed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error completing game: ${e.message}", e)
        }
    }

    /**
     * Get final score calculation
     */
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