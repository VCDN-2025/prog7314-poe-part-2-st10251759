package vcmsa.projects.prog7314.game

import android.util.Log
import vcmsa.projects.prog7314.data.models.*

class MultiplayerGameEngine(private val theme: GameTheme) {

    private var cards: MutableList<GameCard> = mutableListOf()
    private var player1 = Player(PlayerColor.RED, isCurrentTurn = true)
    private var player2 = Player(PlayerColor.BLUE, isCurrentTurn = false)
    private var flippedCards: MutableList<GameCard> = mutableListOf()
    private var matchedPairs = 0
    private val totalPairs = 12 // Fixed 6x4 grid = 24 cards = 12 pairs

    companion object {
        private const val TAG = "MultiplayerGameEngine"
        private const val GRID_ROWS = 6
        private const val GRID_COLUMNS = 4
    }

    /**
     * Initialize game with 24 cards (12 pairs) in a 6x4 grid
     */
    fun initializeCards(): List<GameCard> {
        val selectedImages = theme.cardImages.take(totalPairs)
        val cardList = mutableListOf<GameCard>()
        var cardId = 0

        // Create pairs
        selectedImages.forEachIndexed { pairId, imageRes ->
            // First card of pair
            cardList.add(GameCard(
                id = cardId++,
                pairId = pairId,
                imageResId = imageRes,
                isFlipped = false,
                isMatched = false
            ))
            // Second card of pair
            cardList.add(GameCard(
                id = cardId++,
                pairId = pairId,
                imageResId = imageRes,
                isFlipped = false,
                isMatched = false
            ))
        }

        // Shuffle cards
        cards = cardList.shuffled().toMutableList()
        Log.d(TAG, "✅ Initialized ${cards.size} cards (${totalPairs} pairs)")
        return cards.toList()
    }

    /**
     * Handle card flip
     * Returns: Pair<Boolean, FlipResult>
     * - Boolean: whether flip was successful
     * - FlipResult: what happened (MATCH, NO_MATCH, WAITING)
     */
    fun flipCard(cardId: Int): Pair<Boolean, FlipResult> {
        // Can't flip if 2 cards already flipped
        if (flippedCards.size >= 2) {
            return Pair(false, FlipResult.WAITING)
        }

        val card = cards.find { it.id == cardId } ?: return Pair(false, FlipResult.WAITING)

        // Can't flip already flipped or matched cards
        if (card.isFlipped || card.isMatched) {
            return Pair(false, FlipResult.WAITING)
        }

        // Flip the card
        val cardIndex = cards.indexOfFirst { it.id == cardId }
        cards[cardIndex] = card.copy(isFlipped = true)
        flippedCards.add(cards[cardIndex])

        Log.d(TAG, "Flipped card ${card.id}, flipped count: ${flippedCards.size}")

        // Check for match when 2 cards are flipped
        if (flippedCards.size == 2) {
            val card1 = flippedCards[0]
            val card2 = flippedCards[1]

            return if (card1.pairId == card2.pairId) {
                Log.d(TAG, "✅ MATCH! Pair ${card1.pairId}")
                handleMatch()
                Pair(true, FlipResult.MATCH)
            } else {
                Log.d(TAG, "❌ NO MATCH")
                Pair(true, FlipResult.NO_MATCH)
            }
        }

        return Pair(true, FlipResult.SINGLE_FLIP)
    }

    /**
     * Handle match - mark cards as matched, increment score
     */
    private fun handleMatch() {
        flippedCards.forEach { flippedCard ->
            val index = cards.indexOfFirst { it.id == flippedCard.id }
            cards[index] = cards[index].copy(isMatched = true, isFlipped = true)
        }

        // Increment current player's score
        if (player1.isCurrentTurn) {
            player1 = player1.copy(score = player1.score + 1)
        } else {
            player2 = player2.copy(score = player2.score + 1)
        }

        matchedPairs++
        flippedCards.clear()

        Log.d(TAG, "Match count: $matchedPairs/$totalPairs")
        Log.d(TAG, "Scores - Red: ${player1.score}, Blue: ${player2.score}")

        // Player continues their turn after a match (don't switch)
    }

    /**
     * Reset flipped cards (after mismatch)
     */
    fun resetFlippedCards() {
        flippedCards.forEach { flippedCard ->
            val index = cards.indexOfFirst { it.id == flippedCard.id }
            cards[index] = cards[index].copy(isFlipped = false)
        }
        flippedCards.clear()

        // Switch turns after mismatch
        switchTurns()
    }

    /**
     * Clear matched cards visually
     */
    fun clearMatchedCards() {
        flippedCards.clear()
        // Player continues their turn (don't switch)
    }

    /**
     * Switch player turns
     */
    private fun switchTurns() {
        player1 = player1.copy(isCurrentTurn = !player1.isCurrentTurn)
        player2 = player2.copy(isCurrentTurn = !player2.isCurrentTurn)
        Log.d(TAG, "Turn switched - Current: ${if (player1.isCurrentTurn) "RED" else "BLUE"}")
    }

    /**
     * Check if game is complete
     */
    val isGameComplete: Boolean
        get() = matchedPairs >= totalPairs

    /**
     * Get current game state
     */
    fun getGameState(): MultiplayerGameState {
        return MultiplayerGameState(
            cards = cards.toList(),
            player1 = player1,
            player2 = player2,
            matchedPairs = matchedPairs,
            totalPairs = totalPairs,
            isGameComplete = isGameComplete,
            theme = theme
        )
    }

    /**
     * Get winner
     */
    fun getWinner(): PlayerColor? {
        return when {
            player1.score > player2.score -> PlayerColor.RED
            player2.score > player1.score -> PlayerColor.BLUE
            else -> null // Tie
        }
    }

    enum class FlipResult {
        SINGLE_FLIP,
        MATCH,
        NO_MATCH,
        WAITING
    }
}