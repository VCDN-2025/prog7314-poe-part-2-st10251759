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
import vcmsa.projects.prog7314.data.models.*
import vcmsa.projects.prog7314.data.repository.ApiRepository
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.game.MultiplayerGameEngine
import vcmsa.projects.prog7314.utils.AuthManager

class MultiplayerViewModel(application: Application) : AndroidViewModel(application) {

    private var gameEngine: MultiplayerGameEngine? = null
    private var timerJob: Job? = null
    private lateinit var apiRepository: ApiRepository

    private val _gameState = MutableStateFlow<MultiplayerGameState?>(null)
    val gameState: StateFlow<MultiplayerGameState?> = _gameState.asStateFlow()

    private val _timeElapsed = MutableStateFlow(0)
    val timeElapsed: StateFlow<Int> = _timeElapsed.asStateFlow()

    private val _totalMoves = MutableStateFlow(0)
    val totalMoves: StateFlow<Int> = _totalMoves.asStateFlow()

    private val _isGameComplete = MutableStateFlow(false)
    val isGameComplete: StateFlow<Boolean> = _isGameComplete.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    companion object {
        private const val TAG = "MultiplayerViewModel"
    }

    init {
        // Initialize API repository
        val repositories = RepositoryProvider.getRepositories(application)
        apiRepository = repositories.apiRepository
    }

    /**
     * Initialize multiplayer game with theme
     */
    fun initializeGame(theme: GameTheme) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing multiplayer game with theme: ${theme.themeName}")

                gameEngine = MultiplayerGameEngine(theme)
                val cards = gameEngine!!.initializeCards()

                _gameState.value = gameEngine!!.getGameState()
                _timeElapsed.value = 0
                _totalMoves.value = 0
                _isGameComplete.value = false
                _isPaused.value = false

                startTimer()

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
                if (_isPaused.value || _isGameComplete.value) return@launch

                val engine = gameEngine ?: return@launch
                val (success, result) = engine.flipCard(cardId)

                if (success) {
                    updateGameState()

                    when (result) {
                        MultiplayerGameEngine.FlipResult.MATCH -> {
                            // Match - increment move count
                            _totalMoves.value++

                            // Brief delay before clearing
                            delay(500)
                            engine.clearMatchedCards()
                            updateGameState()

                            // Check game completion
                            if (engine.isGameComplete) {
                                onGameComplete()
                            }
                        }
                        MultiplayerGameEngine.FlipResult.NO_MATCH -> {
                            // No match - increment move count
                            _totalMoves.value++

                            // Delay before flipping back
                            delay(1200)
                            engine.resetFlippedCards()
                            updateGameState()
                        }
                        else -> {
                            // Single flip - do nothing
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling card click: ${e.message}", e)
            }
        }
    }

    /**
     * Pause/Resume game
     */
    fun togglePause() {
        _isPaused.value = !_isPaused.value
        if (_isPaused.value) {
            timerJob?.cancel()
        } else {
            startTimer()
        }
    }

    /**
     * Update game state from engine
     */
    private fun updateGameState() {
        val engine = gameEngine ?: return
        _gameState.value = engine.getGameState()
    }

    /**
     * Start game timer
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_isGameComplete.value && !_isPaused.value) {
                delay(1000)
                _timeElapsed.value++
            }
        }
    }

    /**
     * Handle game completion
     */
    private suspend fun onGameComplete() {
        try {
            Log.d(TAG, "🎉 Game complete!")
            timerJob?.cancel()
            _isGameComplete.value = true

            val state = _gameState.value ?: return

            // Submit to API
            val userId = AuthManager.getCurrentUser()?.uid ?: "unknown"
            apiRepository.submitMultiplayerResult(
                userId = userId,
                theme = state.theme.themeName,
                player1Score = state.player1.score,
                player2Score = state.player2.score,
                timeTaken = _timeElapsed.value,
                totalMoves = _totalMoves.value
            )

            Log.d(TAG, "✅ Multiplayer result submitted to API")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error completing game: ${e.message}", e)
        }
    }

    /**
     * Get game result
     */
    fun getGameResult(): MultiplayerGameResult {
        val state = _gameState.value!!
        val engine = gameEngine!!

        return MultiplayerGameResult(
            theme = state.theme,
            player1Score = state.player1.score,
            player2Score = state.player2.score,
            winner = engine.getWinner(),
            timeTaken = _timeElapsed.value,
            totalMoves = _totalMoves.value
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}