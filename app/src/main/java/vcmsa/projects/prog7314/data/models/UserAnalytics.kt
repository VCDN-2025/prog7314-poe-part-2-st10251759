package vcmsa.projects.prog7314.data.models

data class UserAnalytics(
    // Overview Stats
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val winRate: Float = 0f,
    val totalPlaytime: Int = 0, // in seconds
    val currentLevel: Int = 1,
    val totalXP: Int = 0,

    // Performance Stats
    val averageScore: Float = 0f,
    val bestScore: Int = 0,
    val averageTime: Float = 0f, // in seconds
    val fastestTime: Int = 0, // in seconds
    val averageAccuracy: Float = 0f,
    val averageMoves: Float = 0f,

    // Theme & Mode Stats
    val favoriteTheme: String = "Animals",
    val mostPlayedMode: String = "ARCADE",
    val themeStats: Map<String, Int> = emptyMap(), // theme -> games played
    val modeStats: Map<String, Int> = emptyMap(), // mode -> games played

    // Achievement Stats
    val achievementsUnlocked: Int = 0,
    val totalAchievements: Int = 20, // Update this based on your total achievements
    val achievementProgress: Float = 0f, // percentage

    // Streak Stats
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)

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