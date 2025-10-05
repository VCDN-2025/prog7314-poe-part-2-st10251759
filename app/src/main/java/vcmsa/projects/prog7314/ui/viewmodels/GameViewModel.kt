package vcmsa.projects.prog7314.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.models.*
import kotlin.random.Random

class GameViewModel : ViewModel() {

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

    /**
     * Initialize a new game with theme and grid size
     */
    fun initializeGame(theme: GameTheme, gridSize: GridSize) {
        currentTheme = theme
        currentGridSize = gridSize

        // Generate cards
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

    /**
     * Generate shuffled cards for the game
     */
    private fun generateCards(theme: GameTheme, gridSize: GridSize): List<GameCard> {
        val totalPairs = gridSize.totalCards / 2
        val availableImages = theme.cardImages

        // Select random images for pairs
        val selectedImages = if (availableImages.size >= totalPairs) {
            availableImages.shuffled().take(totalPairs)
        } else {
            // If not enough images, repeat some
            (availableImages + availableImages).shuffled().take(totalPairs)
        }

        // Create pairs
        val cards = mutableListOf<GameCard>()
        selectedImages.forEachIndexed { pairId, imageResId ->
            // First card of pair
            cards.add(
                GameCard(
                    id = cards.size,
                    pairId = pairId,
                    imageResId = imageResId,
                    isFlipped = false,
                    isMatched = false
                )
            )
            // Second card of pair
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

        // Shuffle cards
        return cards.shuffled()
    }

    /**
     * Handle card flip
     */
    fun onCardClicked(card: GameCard) {
        if (isProcessing || card.isFlipped || card.isMatched) return

        val currentState = _gameState.value ?: return
        val updatedCards = currentState.cards.toMutableList()
        val cardIndex = updatedCards.indexOfFirst { it.id == card.id }

        if (cardIndex == -1) return

        // Flip the card
        updatedCards[cardIndex] = updatedCards[cardIndex].copy(isFlipped = true)
        _gameState.value = currentState.copy(cards = updatedCards)

        when {
            firstFlippedCard == null -> {
                // First card flipped
                firstFlippedCard = updatedCards[cardIndex]
            }
            secondFlippedCard == null -> {
                // Second card flipped
                secondFlippedCard = updatedCards[cardIndex]
                _moves.value += 1

                // Check for match
                checkForMatch()
            }
        }
    }

    /**
     * Check if two flipped cards match
     */
    private fun checkForMatch() {
        val first = firstFlippedCard ?: return
        val second = secondFlippedCard ?: return

        isProcessing = true

        viewModelScope.launch {
            delay(800) // Show cards for a moment

            val currentState = _gameState.value ?: return@launch
            val updatedCards = currentState.cards.toMutableList()

            if (first.pairId == second.pairId) {
                // Match found!
                updatedCards.replaceAll { card ->
                    if (card.id == first.id || card.id == second.id) {
                        card.copy(isMatched = true, isFlipped = true)
                    } else {
                        card
                    }
                }

                // Update points
                val basePoints = 100
                val timeBonus = maxOf(0, 50 - (_timeElapsed.value.toInt() / 1000))
                _points.value += basePoints + timeBonus

                val matchedPairs = currentState.matchedPairs + 1
                _gameState.value = currentState.copy(
                    cards = updatedCards,
                    matchedPairs = matchedPairs
                )

                // Check if game is complete
                if (matchedPairs == currentGridSize.totalCards / 2) {
                    completeGame()
                }
            } else {
                // No match - flip cards back
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

    /**
     * Complete the game and calculate results
     * FIXED: Now stores time in milliseconds in GameResult for consistency
     */
    private fun completeGame() {
        stopTimer()
        _isGameComplete.value = true

        val timeInMilliseconds = _timeElapsed.value  // Keep as milliseconds
        val timeInSeconds = (timeInMilliseconds / 1000).toLong()
        val moves = _moves.value
        val points = _points.value

        // Calculate stars (1-3)
        val stars = when {
            moves <= currentGridSize.totalCards && timeInSeconds <= 30 -> 3
            moves <= currentGridSize.totalCards * 1.5 && timeInSeconds <= 60 -> 2
            else -> 1
        }

        // Calculate bonus
        val bonus = if (stars == 3) 500 else if (stars == 2) 200 else 0

        _gameResult.value = GameResult(
            theme = currentTheme,
            gridSize = currentGridSize,
            moves = moves,
            timeTaken = timeInMilliseconds,  // FIXED: Store milliseconds instead of seconds
            points = points + bonus,
            stars = stars,
            bonus = bonus
        )
    }

    /**
     * Start game timer
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
     * Stop game timer
     */
    private fun stopTimer() {
        timerJob?.cancel()
    }

    /**
     * Reset game
     */
    fun resetGame() {
        initializeGame(currentTheme, currentGridSize)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}