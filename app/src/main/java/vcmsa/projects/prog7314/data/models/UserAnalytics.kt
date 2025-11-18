package vcmsa.projects.prog7314.data.models

/*
    Code Attribution for: Data Classes
    ===================================================
    Kotlin, 2025a. Data classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/data-classes.html>
    [Accessed 18 November 2025].
*/

/**
 * Represents aggregated analytics and performance statistics for a user.
 *
 * @property totalGames Total number of games played.
 * @property totalWins Total number of games won.
 * @property winRate Win rate as a percentage (0-100).
 * @property totalPlaytime Total playtime in seconds across all games.
 * @property currentLevel Current user level.
 * @property totalXP Total experience points accumulated by the user.
 *
 * @property averageScore Average score across all games.
 * @property bestScore Highest score achieved.
 * @property averageTime Average time taken per game in seconds.
 * @property fastestTime Fastest game completion time in seconds.
 * @property averageAccuracy Average accuracy percentage (0-100).
 * @property averageMoves Average number of moves per game.
 *
 * @property favoriteTheme The most played game theme.
 * @property mostPlayedMode The most played game mode.
 * @property themeStats Map of theme names to number of games played per theme.
 * @property modeStats Map of game modes to number of games played per mode.
 *
 * @property achievementsUnlocked Number of achievements unlocked.
 * @property totalAchievements Total number of possible achievements.
 * @property achievementProgress Progress toward total achievements as a percentage.
 *
 * @property currentStreak Current winning or play streak.
 * @property longestStreak Longest winning or play streak.
 */
data class UserAnalytics(
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val winRate: Float = 0f,
    val totalPlaytime: Int = 0,
    val currentLevel: Int = 1,
    val totalXP: Int = 0,

    val averageScore: Float = 0f,
    val bestScore: Int = 0,
    val averageTime: Float = 0f,
    val fastestTime: Int = 0,
    val averageAccuracy: Float = 0f,
    val averageMoves: Float = 0f,

    val favoriteTheme: String = "Animals",
    val mostPlayedMode: String = "ARCADE",
    val themeStats: Map<String, Int> = emptyMap(),
    val modeStats: Map<String, Int> = emptyMap(),

    val achievementsUnlocked: Int = 0,
    val totalAchievements: Int = 20,
    val achievementProgress: Float = 0f,

    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)

/**
 * Represents a concise summary of a recently played game.
 *
 * @property gameId Unique identifier of the game.
 * @property gameMode Mode of the game (e.g., "ARCADE", "ADVENTURE").
 * @property theme Theme used in the game.
 * @property score Score achieved in the game.
 * @property timeTaken Time taken to complete the game (in seconds).
 * @property moves Number of moves made during the game.
 * @property stars Number of stars earned (0-3).
 * @property completedAt Timestamp when the game was completed.
 */
data class RecentGameSummary(
    val gameId: String,
    val gameMode: String,
    val theme: String,
    val score: Int,
    val timeTaken: Int,
    val moves: Int,
    val stars: Int,
    val completedAt: Long
)
