package vcmsa.projects.prog7314.data.models

/*
    Code Attribution for: Data Classes
    ===================================================
    Kotlin, 2025a. Data classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/data-classes.html>
    [Accessed 18 November 2025].
*/

/**
 * Represents a user's overall progress in the game.
 *
 * @property userId The unique ID of the user.
 * @property currentLevel The user's current level.
 * @property levelProgress Map of level numbers to their corresponding LevelData.
 * @property unlockedCategories List of unlocked game categories (e.g., "Animals", "Pokemon").
 * @property totalGamesPlayed Total number of games played by the user.
 * @property gamesWon Total number of games won by the user.
 * @property lastUpdated Timestamp of the last update to this progress.
 */
data class GameProgress(
    val userId: String = "",
    val currentLevel: Int = 1,
    val levelProgress: Map<Int, LevelData> = emptyMap(), // Level number -> Level data
    val unlockedCategories: List<String> = listOf("Animals"),
    val totalGamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Represents the progress and statistics for a single level.
 *
 * @property levelNumber The level number.
 * @property stars Number of stars earned (0-3).
 * @property bestScore Best score achieved on this level.
 * @property bestTime Fastest completion time in seconds.
 * @property bestMoves Fewest moves taken to complete the level.
 * @property isUnlocked Whether the level is unlocked for the user.
 * @property isCompleted Whether the level has been completed by the user.
 * @property timesPlayed Number of times the level has been played.
 */
data class LevelData(
    val levelNumber: Int,
    val stars: Int = 0,
    val bestScore: Int = 0,
    val bestTime: Int = 0,
    val bestMoves: Int = 0,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val timesPlayed: Int = 0
)
