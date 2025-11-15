package vcmsa.projects.prog7314.game

import android.util.Log
import vcmsa.projects.prog7314.data.models.GameCard
import vcmsa.projects.prog7314.data.models.GameTheme

/**
 * Pure Kotlin game engine for card matching logic
 * This class is testable and doesn't depend on Android framework
 */
class GameEngine(
    private val theme: GameTheme,
    private val config: GameConfig.LevelConfig
) {
    private val TAG = "GameEngine"

    private var cards: MutableList<GameCard> = mutableListOf()
    private var flippedCards: MutableList<GameCard> = mutableListOf()
    private var matchedPairs = 0
    private var moveCount = 0
    private var score = 0
    private var isProcessing = false

    val totalPairs: Int get() = config.totalPairs
    val isGameComplete: Boolean get() = matchedPairs == totalPairs

    /**
     * Initialize game cards
     */
    fun initializeCards(): List<GameCard> {
        Log.d(TAG, "Initializing ${config.totalPairs} pairs for ${theme.name}")

        cards.clear()
        flippedCards.clear()
        matchedPairs = 0
        moveCount = 0
        score = 0

        // Get required number of images from theme
        val availableImages = theme.cardImages
        val requiredImages = config.totalPairs.coerceAtMost(availableImages.size)
        val selectedImages = availableImages.shuffled().take(requiredImages)

        // Create pairs
        val cardList = mutableListOf<GameCard>()
        var cardId = 0

        selectedImages.forEachIndexed { pairId, imageResId ->
            // Create two cards for each pair
            cardList.add(
                GameCard(
                    id = cardId++,
                    pairId = pairId,
                    imageResId = imageResId,
                    isFlipped = false,
                    isMatched = false
                )
            )
            cardList.add(
                GameCard(
                    id = cardId++,
                    pairId = pairId,
                    imageResId = imageResId,
                    isFlipped = false,
                    isMatched = false
                )
            )
        }

        // Shuffle cards
        cards = cardList.shuffled().toMutableList()

        Log.d(TAG, "✅ Created ${cards.size} cards (${config.totalPairs} pairs)")
        return cards.toList()
    }

    /**
     * Handle card flip
     * Returns: Pair(success: Boolean, result: FlipResult)
     */
    fun flipCard(cardId: Int): Pair<Boolean, FlipResult> {
        if (isProcessing) {
            return Pair(false, FlipResult.PROCESSING)
        }

        val cardIndex = cards.indexOfFirst { it.id == cardId }
        if (cardIndex == -1) return Pair(false, FlipResult.INVALID_CARD)

        val card = cards[cardIndex]

        // Can't flip already matched or flipped cards
        if (card.isMatched || card.isFlipped) {
            return Pair(false, FlipResult.ALREADY_FLIPPED)
        }

        // Can't flip more than 2 cards
        if (flippedCards.size >= 2) {
            return Pair(false, FlipResult.TOO_MANY_FLIPPED)
        }

        // FIXED: Create new card instance with isFlipped = true
        val flippedCard = card.copy(isFlipped = true)
        cards[cardIndex] = flippedCard
        flippedCards.add(flippedCard)

        Log.d(TAG, "Card ${card.id} flipped")

        // Check if we have 2 cards flipped
        if (flippedCards.size == 2) {
            isProcessing = true
            moveCount++

            val card1 = flippedCards[0]
            val card2 = flippedCards[1]

            return if (card1.pairId == card2.pairId) {
                // Match found!
                val card1Index = cards.indexOfFirst { it.id == card1.id }
                val card2Index = cards.indexOfFirst { it.id == card2.id }

                cards[card1Index] = card1.copy(isMatched = true)
                cards[card2Index] = card2.copy(isMatched = true)

                matchedPairs++
                score += config.matchScore

                Log.d(TAG, "✅ Match found! Pairs: $matchedPairs/${config.totalPairs}")

                Pair(true, FlipResult.MATCH)
            } else {
                // No match
                Log.d(TAG, "❌ No match")
                Pair(true, FlipResult.NO_MATCH)
            }
        }

        return Pair(true, FlipResult.CARD_FLIPPED)
    }

    /**
     * Reset flipped cards (call after showing mismatch)
     */
    fun resetFlippedCards() {
        flippedCards.forEach { flippedCard ->
            val index = cards.indexOfFirst { it.id == flippedCard.id }
            if (index != -1 && !cards[index].isMatched) {
                cards[index] = cards[index].copy(isFlipped = false)
            }
        }
        flippedCards.clear()
        isProcessing = false
    }

    /**
     * Clear matched cards from flipped list (call after match)
     */
    fun clearMatchedCards() {
        flippedCards.clear()
        isProcessing = false
    }

    /**
     * Get current game state
     */
    fun getGameState(): GameState {
        return GameState(
            cards = cards.toList(), // Return a new list instance
            moves = moveCount,
            matchedPairs = matchedPairs,
            totalPairs = totalPairs,
            score = score,
            isComplete = isGameComplete
        )
    }

    /**
     * Calculate final score with time bonus
     */
    fun calculateFinalScore(timeRemaining: Int): FinalScore {
        val timeBonus = GameConfig.calculateTimeBonus(timeRemaining, config.timeBonusPerSecond)
        val finalScore = score + timeBonus
        val maxPossibleScore = config.maxPossibleScore
        val stars = GameConfig.calculateStars(finalScore, maxPossibleScore)

        return FinalScore(
            baseScore = score,
            timeBonus = timeBonus,
            finalScore = finalScore,
            stars = stars,
            moves = moveCount,
            matchedPairs = matchedPairs
        )
    }

    /**
     * Flip result enum
     */
    enum class FlipResult {
        CARD_FLIPPED,
        MATCH,
        NO_MATCH,
        ALREADY_FLIPPED,
        TOO_MANY_FLIPPED,
        INVALID_CARD,
        PROCESSING
    }

    /**
     * Game state data class
     */
    data class GameState(
        val cards: List<GameCard>,
        val moves: Int,
        val matchedPairs: Int,
        val totalPairs: Int,
        val score: Int,
        val isComplete: Boolean
    )

    /**
     * Final score data class
     */
    data class FinalScore(
        val baseScore: Int,
        val timeBonus: Int,
        val finalScore: Int,
        val stars: Int,
        val moves: Int,
        val matchedPairs: Int
    )
}