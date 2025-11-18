package vcmsa.projects.prog7314.data.models

/*
    Code Attribution for: Data Classes
    ===================================================
    Kotlin, 2025a. Data classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/data-classes.html>
    [Accessed 18 November 2025].
*/

/**
 * Represents the result of a single game session.
 *
 * @property theme The theme of the game played.
 * @property gridSize The size of the game grid (e.g., 3x2, 4x3).
 * @property points Total points earned in the game.
 * @property timeTaken Time taken to complete the game in milliseconds or seconds.
 * @property moves Number of moves made during the game.
 * @property stars Number of stars earned (0-3) based on performance.
 * @property bonus Bonus points awarded for achievements or special actions.
 */
data class GameResult(
    val theme: GameTheme,
    val gridSize: GridSize,
    val points: Int,
    val timeTaken: Long,  // Changed from timeInSeconds to timeTaken for consistency
    val moves: Int,
    val stars: Int,
    val bonus: Int
)
