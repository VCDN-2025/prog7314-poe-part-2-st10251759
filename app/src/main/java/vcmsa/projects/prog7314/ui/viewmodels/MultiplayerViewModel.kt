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

/*
    Code Attribution for: Creating ViewModels
    ===================================================
    Android Developers, 2019b. ViewModel Overview | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/libraries/architecture/viewmodel>
    [Accessed 18 November 2025].
*/

/**
 * ViewModel that manages the multiplayer memory game mode.
 * Handles two-player gameplay where players take turns finding matching pairs.
 * Tracks individual player scores and determines the winner at the end.
 */
class MultiplayerViewModel(application: Application) : AndroidViewModel(application) {

    // Core game engine that manages multiplayer-specific logic
    private var gameEngine: MultiplayerGameEngine? = null

    // Timer job for counting game duration
    private var timerJob: Job? = null

    // Repository for submitting results to the API
    private lateinit var apiRepository: ApiRepository

    // Complete game state including both players' progress and scores
    private val _gameState = MutableStateFlow<MultiplayerGameState?>(null)
    val gameState: StateFlow<MultiplayerGameState?> = _gameState.asStateFlow()

    // Time elapsed since game started (in seconds)
    private val _timeElapsed = MutableStateFlow(0)
    val timeElapsed: StateFlow<Int> = _timeElapsed.asStateFlow()

    // Combined move count for both players
    private val _totalMoves = MutableStateFlow(0)
    val totalMoves: StateFlow<Int> = _totalMoves.asStateFlow()

    // Whether all pairs have been matched
    private val _isGameComplete = MutableStateFlow(false)
    val isGameComplete: StateFlow<Boolean> = _isGameComplete.asStateFlow()

    // Whether the game is currently paused
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    companion object {
        private const val TAG = "MultiplayerViewModel"
    }

    init {
        // Get the API repository for submitting game results
        val repositories = RepositoryProvider.getRepositories(application)
        apiRepository = repositories.apiRepository
    }

    /**
     * Sets up a new multiplayer game with the specified theme.
     * Creates the card grid and initializes both players with zero scores.
     */
    fun initializeGame(theme: GameTheme) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing multiplayer game with theme: ${theme.name}")

                // Create game engine and generate shuffled cards
                gameEngine = MultiplayerGameEngine(theme)
                val cards = gameEngine!!.initializeCards()

                // Reset all game tracking values
                _gameState.value = gameEngine!!.getGameState()
                _timeElapsed.value = 0
                _totalMoves.value = 0
                _isGameComplete.value = false
                _isPaused.value = false

                // Begin timing the game
                startTimer()

                Log.d(TAG, "✅ Game initialized with ${cards.size} cards")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error initializing game: ${e.message}", e)
            }
        }
    }

    /**
     * Handles when a player clicks on a card during their turn.
     * Flips the card, checks for matches, and switches turns appropriately.
     */
    fun onCardClick(cardId: Int) {
        viewModelScope.launch {
            try {
                // Ignore clicks if game is paused or finished
                if (_isPaused.value || _isGameComplete.value) return@launch

                val engine = gameEngine ?: return@launch
                val (success, result) = engine.flipCard(cardId)

                if (success) {
                    // Update UI with new card states
                    updateGameState()

                    when (result) {
                        MultiplayerGameEngine.FlipResult.MATCH -> {
                            // Cards matched - current player scored a point
                            _totalMoves.value++

                            // Wait briefly for match animation
                            delay(500)
                            engine.clearMatchedCards()
                            updateGameState()

                            // Check if all pairs are now matched
                            if (engine.isGameComplete) {
                                onGameComplete()
                            }
                        }
                        MultiplayerGameEngine.FlipResult.NO_MATCH -> {
                            // Cards didn't match - switch to other player
                            _totalMoves.value++

                            // Wait so players can see the cards before they flip back
                            delay(1200)
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
     * Pauses or resumes the game.
     * Stops the timer when paused and restarts it when resumed.
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
     * Refreshes the game state with current values from the engine.
     */
    private fun updateGameState() {
        val engine = gameEngine ?: return
        _gameState.value = engine.getGameState()
    }

    /**
     * Starts the game timer that counts up every second.
     * Automatically stops when game is completed or paused.
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
     * Called when all pairs are matched.
     * Determines the winner and submits results to the API.
     */
    private suspend fun onGameComplete() {
        try {
            Log.d(TAG, "🎉 Game complete!")
            timerJob?.cancel()
            _isGameComplete.value = true

            val state = _gameState.value ?: return

            // Upload game results to the server
            val userId = AuthManager.getCurrentUser()?.uid ?: "unknown"
            apiRepository.submitMultiplayerResult(
                userId = userId,
                theme = state.theme.name,
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
     * Returns the final game results including scores and winner.
     * Called by the UI to display the results screen.
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

    /**
     * Clean up resources when ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}