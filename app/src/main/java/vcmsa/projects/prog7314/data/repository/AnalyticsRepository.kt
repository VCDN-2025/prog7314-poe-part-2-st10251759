package vcmsa.projects.prog7314.data.repository

import android.util.Log
import vcmsa.projects.prog7314.data.dao.AchievementDao
import vcmsa.projects.prog7314.data.dao.GameResultDao
import vcmsa.projects.prog7314.data.dao.UserProfileDao
import vcmsa.projects.prog7314.data.models.RecentGameSummary
import vcmsa.projects.prog7314.data.models.UserAnalytics

class AnalyticsRepository(
    private val gameResultDao: GameResultDao,
    private val achievementDao: AchievementDao,
    private val userProfileDao: UserProfileDao
) {

    companion object {
        private const val TAG = "AnalyticsRepository"
    }

    /**
     * Get comprehensive analytics for a user
     */
    suspend fun getUserAnalytics(userId: String): UserAnalytics {
        return try {
            Log.d(TAG, "Calculating analytics for user: $userId")

            // Get user profile
            val userProfile = userProfileDao.getUserProfile(userId)

            // Get all game results
            val allGames = gameResultDao.getAllGamesForUser(userId)
            val totalGames = allGames.size
            val totalWins = allGames.count { it.isWin }

            // Calculate win rate
            val winRate = if (totalGames > 0) {
                (totalWins.toFloat() / totalGames.toFloat()) * 100f
            } else {
                0f
            }

            // Calculate performance stats
            val averageScore = if (totalGames > 0) {
                allGames.map { it.score }.average().toFloat()
            } else {
                0f
            }

            val bestScore = allGames.maxOfOrNull { it.score } ?: 0

            val averageTime = if (totalGames > 0) {
                allGames.map { it.timeTaken }.average().toFloat()
            } else {
                0f
            }

            val fastestTime = allGames.filter { it.isWin }.minOfOrNull { it.timeTaken } ?: 0

            val averageAccuracy = if (totalGames > 0) {
                allGames.map { it.accuracy }.average().toFloat()
            } else {
                0f
            }

            val averageMoves = if (totalGames > 0) {
                allGames.map { it.moves }.average().toFloat()
            } else {
                0f
            }

            // Calculate total playtime (sum of all game times)
            val totalPlaytime = allGames.sumOf { it.timeTaken }

            // Calculate theme stats
            val themeStats = allGames.groupBy { it.theme }
                .mapValues { it.value.size }
                .toMap()

            val favoriteTheme = themeStats.maxByOrNull { it.value }?.key ?: "Animals"

            // Calculate mode stats
            val modeStats = allGames.groupBy { it.gameMode }
                .mapValues { it.value.size }
                .toMap()

            val mostPlayedMode = modeStats.maxByOrNull { it.value }?.key ?: "ARCADE"

            // Get achievement stats
            val achievementsUnlocked = achievementDao.getUnlockedCount(userId)
            val totalAchievements = 12 // Update based on your total achievements
            val achievementProgress = if (totalAchievements > 0) {
                (achievementsUnlocked.toFloat() / totalAchievements.toFloat()) * 100f
            } else {
                0f
            }

            // Calculate streak (basic implementation - you can enhance this)
            val currentStreak = calculateCurrentStreak(userId)
            val longestStreak = calculateLongestStreak(userId)

            val analytics = UserAnalytics(
                totalGames = totalGames,
                totalWins = totalWins,
                winRate = winRate,
                totalPlaytime = totalPlaytime,
                currentLevel = userProfile?.level ?: 1,
                totalXP = userProfile?.totalXP ?: 0,
                averageScore = averageScore,
                bestScore = bestScore,
                averageTime = averageTime,
                fastestTime = fastestTime,
                averageAccuracy = averageAccuracy,
                averageMoves = averageMoves,
                favoriteTheme = favoriteTheme,
                mostPlayedMode = mostPlayedMode,
                themeStats = themeStats,
                modeStats = modeStats,
                achievementsUnlocked = achievementsUnlocked,
                totalAchievements = totalAchievements,
                achievementProgress = achievementProgress,
                currentStreak = currentStreak,
                longestStreak = longestStreak
            )

            Log.d(TAG, "✅ Analytics calculated successfully")
            Log.d(TAG, "   Total Games: $totalGames, Win Rate: ${"%.1f".format(winRate)}%")

            analytics

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error calculating analytics: ${e.message}", e)
            UserAnalytics() // Return empty analytics on error
        }
    }

    /**
     * Get recent game summaries
     */
    suspend fun getRecentGames(userId: String, limit: Int = 5): List<RecentGameSummary> {
        return try {
            val games = gameResultDao.getRecentGames(userId, limit)
            games.map { game ->
                // Calculate stars based on score (you can adjust this logic)
                val stars = when {
                    game.score >= 2000 -> 3
                    game.score >= 1500 -> 2
                    game.score >= 1000 -> 1
                    else -> 0
                }

                RecentGameSummary(
                    gameId = game.gameId,
                    gameMode = game.gameMode,
                    theme = game.theme,
                    score = game.score,
                    timeTaken = game.timeTaken,
                    moves = game.moves,
                    stars = stars,
                    completedAt = game.completedAt
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting recent games: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get theme-specific statistics
     */
    suspend fun getThemeStats(userId: String): Map<String, ThemeStats> {
        return try {
            val allGames = gameResultDao.getAllGamesForUser(userId)

            allGames.groupBy { it.theme }
                .mapValues { (theme, games) ->
                    ThemeStats(
                        themeName = theme,
                        gamesPlayed = games.size,
                        bestScore = games.maxOfOrNull { it.score } ?: 0,
                        averageScore = games.map { it.score }.average().toFloat(),
                        averageTime = games.map { it.timeTaken }.average().toFloat()
                    )
                }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting theme stats: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * Calculate current streak (consecutive days played)
     */
    private suspend fun calculateCurrentStreak(userId: String): Int {
        // This is a basic implementation
        // You can enhance this with proper date tracking
        return try {
            val games = gameResultDao.getAllGamesForUser(userId)
            if (games.isEmpty()) return 0

            // For now, return a simple count based on recent activity
            // In a real implementation, you'd check consecutive days
            val recentGames = games.takeLast(7)
            recentGames.size
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Calculate longest streak
     */
    private suspend fun calculateLongestStreak(userId: String): Int {
        // This is a basic implementation
        // You can enhance this with proper date tracking
        return try {
            val games = gameResultDao.getAllGamesForUser(userId)
            games.size.coerceAtMost(30) // Cap at 30 for now
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Get performance trend (improving, stable, declining)
     */
    suspend fun getPerformanceTrend(userId: String): PerformanceTrend {
        return try {
            val games = gameResultDao.getAllGamesForUser(userId)
            if (games.size < 5) return PerformanceTrend.INSUFFICIENT_DATA

            val recentGames = games.takeLast(5)
            val olderGames = games.dropLast(5).takeLast(5)

            if (olderGames.isEmpty()) return PerformanceTrend.INSUFFICIENT_DATA

            val recentAvg = recentGames.map { it.score }.average()
            val olderAvg = olderGames.map { it.score }.average()

            when {
                recentAvg > olderAvg * 1.1 -> PerformanceTrend.IMPROVING
                recentAvg < olderAvg * 0.9 -> PerformanceTrend.DECLINING
                else -> PerformanceTrend.STABLE
            }
        } catch (e: Exception) {
            PerformanceTrend.INSUFFICIENT_DATA
        }
    }
}

/**
 * Theme-specific statistics
 */
data class ThemeStats(
    val themeName: String,
    val gamesPlayed: Int,
    val bestScore: Int,
    val averageScore: Float,
    val averageTime: Float
)

/**
 * Performance trend indicator
 */
enum class PerformanceTrend {
    IMPROVING,
    STABLE,
    DECLINING,
    INSUFFICIENT_DATA
}