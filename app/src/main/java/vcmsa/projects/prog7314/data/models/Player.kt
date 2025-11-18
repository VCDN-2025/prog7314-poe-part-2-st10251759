package vcmsa.projects.prog7314.data.models

import androidx.compose.ui.graphics.Color

/*
    Code Attribution for: Data Classes
    ===================================================
    Kotlin, 2025a. Data classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/data-classes.html>
    [Accessed 18 November 2025].

    Code Attribution for: Enum Classes
    ===================================================
    Kotlin, 2025b. Enum classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/enum-classes.html>
    [Accessed 18 November 2025].
*/

/**
 * Enum representing the color and identity of a player in a multiplayer game.
 *
 * @property displayName A descriptive name for the player (includes player number and color).
 * @property color The Color associated with the player.
 */
enum class PlayerColor(val displayName: String, val color: Color) {
    RED("Player 1 [Red]", Color(0xFFE53935)),
    BLUE("Player 2 [Blue]", Color(0xFF1E88E5))
}

/**
 * Represents a player in a multiplayer game.
 *
 * @property playerColor The color and identity of the player.
 * @property score The current score of the player (default 0).
 * @property isCurrentTurn Flag indicating if it is currently this player's turn.
 */
data class Player(
    val playerColor: PlayerColor,
    val score: Int = 0,
    val isCurrentTurn: Boolean = false
) {
    /** Returns the display name of the player. */
    val displayName: String get() = playerColor.displayName

    /** Returns the color associated with the player. */
    val color: Color get() = playerColor.color
}
