package vcmsa.projects.prog7314.data.models

import androidx.annotation.DrawableRes

/*
    Code Attribution for: Data Classes
    ===================================================
    Kotlin, 2025a. Data classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/data-classes.html>
    [Accessed 18 November 2025].
*/

/**
 * Represents a single card in the memory matching game.
 *
 * @property id Unique identifier for this card instance.
 * @property pairId Identifier linking this card to its matching pair.
 * @property imageResId Drawable resource ID for the card's front image.
 * @property isFlipped Tracks whether the card is currently face-up.
 * @property isMatched Tracks whether the card has been successfully matched.
 */
data class GameCard(
    val id: Int,
    val pairId: Int,
    @DrawableRes val imageResId: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

/**
 * Represents the current state of a memory matching game session.
 *
 * @property cards List of all cards in the current game.
 * @property moves Number of moves the player has made so far.
 * @property matchedPairs Number of pairs successfully matched.
 * @property points Player's current points.
 * @property timeElapsed Time elapsed in milliseconds.
 * @property isComplete Whether the game has been completed.
 */
data class GameState(
    val cards: List<GameCard>,
    val moves: Int = 0,
    val matchedPairs: Int = 0,
    val points: Int = 0,
    val timeElapsed: Long = 0,
    val isComplete: Boolean = false
)

// Note: GameResult data class has been removed from here and is now only in GameResult.kt
