package vcmsa.projects.prog7314.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.dao.AchievementDao
import vcmsa.projects.prog7314.data.dao.GameResultDao
import vcmsa.projects.prog7314.data.dao.LevelProgressDao
import vcmsa.projects.prog7314.data.dao.UserProfileDao
import vcmsa.projects.prog7314.data.entities.AchievementEntity
import vcmsa.projects.prog7314.utils.LocalNotificationManager
import vcmsa.projects.prog7314.utils.NotificationTracker
import java.util.UUID

class AchievementRepository(
    private val achievementDao: AchievementDao,
    private val gameResultDao: GameResultDao,
    private val userProfileDao: UserProfileDao,
    private val levelProgressDao: LevelProgressDao,
    private val context: Context  // 🔥 NEW: Context for notifications
) {
    private val TAG = "AchievementRepository"

    // ===== LOCAL DATABASE OPERATIONS =====

    /**
     * Save an achievement (local first)
     */
    suspend fun saveAchievement(achievement: AchievementEntity): Boolean {
        return try {
            Log.d(TAG, "Saving achievement: ${achievement.name}")

            // Save to local database
            achievementDao.insertAchievement(achievement.copy(isSynced = false))

            Log.d(TAG, "✅ Achievement saved locally")

            // TODO: Sync to cloud if online (we'll add this later)

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving achievement: ${e.message}", e)
            false
        }
    }

    /**
     * Get all achievements for a user
     */
    suspend fun getAllAchievementsForUser(userId: String): List<AchievementEntity> {
        return try {
            Log.d(TAG, "Getting all achievements for user: $userId")
            achievementDao.getAllAchievementsForUser(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting achievements: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get all achievements as Flow (reactive)
     */
    fun getAllAchievementsForUserFlow(userId: String): Flow<List<AchievementEntity>> {
        return achievementDao.getAllAchievementsForUserFlow(userId)
    }

    /**
     * Get unlocked achievements
     */
    suspend fun getUnlockedAchievements(userId: String): List<AchievementEntity> {
        return try {
            Log.d(TAG, "Getting unlocked achievements for user: $userId")
            achievementDao.getUnlockedAchievements(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unlocked achievements: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get recent achievements (limited)
     */
    suspend fun getRecentAchievements(userId: String, limit: Int = 5): List<AchievementEntity> {
        return try {
            Log.d(TAG, "Getting recent $limit achievements for user: $userId")
            achievementDao.getRecentAchievements(userId, limit)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent achievements: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get achievement by type
     */
    suspend fun getAchievementByType(userId: String, type: String): AchievementEntity? {
        return try {
            achievementDao.getAchievementByType(userId, type)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting achievement by type: ${e.message}", e)
            null
        }
    }

    /**
     * Check if achievement exists
     */
    suspend fun achievementExists(userId: String, type: String): Boolean {
        return try {
            achievementDao.achievementExists(userId, type) > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error checking achievement existence: ${e.message}", e)
            false
        }
    }

    /**
     * Get unlocked count
     */
    suspend fun getUnlockedCount(userId: String): Int {
        return try {
            achievementDao.getUnlockedCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unlocked count: ${e.message}", e)
            0
        }
    }

    // ===== ACHIEVEMENT MANAGEMENT =====

    /**
     * Unlock an achievement
     */
    suspend fun unlockAchievement(achievementId: String): Boolean {
        return try {
            Log.d(TAG, "Unlocking achievement: $achievementId")

            achievementDao.unlockAchievement(achievementId)

            Log.d(TAG, "✅ Achievement unlocked!")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error unlocking achievement: ${e.message}", e)
            false
        }
    }

    /**
     * Update achievement progress
     */
    suspend fun updateProgress(achievementId: String, progress: Int): Boolean {
        return try {
            Log.d(TAG, "Updating progress for $achievementId: $progress%")

            achievementDao.updateProgress(achievementId, progress)

            // Auto-unlock if progress reaches 100
            if (progress >= 100) {
                unlockAchievement(achievementId)
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating progress: ${e.message}", e)
            false
        }
    }

    // ===== SYNC OPERATIONS =====

    /**
     * Get unsynced achievements (to upload to cloud)
     */
    suspend fun getUnsyncedAchievements(): List<AchievementEntity> {
        return try {
            achievementDao.getUnsyncedAchievements()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced achievements: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get unsynced achievements for specific user
     */
    suspend fun getUnsyncedAchievementsForUser(userId: String): List<AchievementEntity> {
        return try {
            achievementDao.getUnsyncedAchievementsForUser(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced achievements for user: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Mark achievement as synced
     */
    suspend fun markAsSynced(achievementId: String): Boolean {
        return try {
            achievementDao.markAsSynced(achievementId)
            Log.d(TAG, "✅ Achievement marked as synced: $achievementId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marking achievement as synced: ${e.message}", e)
            false
        }
    }

    /**
     * Mark multiple achievements as synced
     */
    suspend fun markMultipleAsSynced(achievementIds: List<String>): Boolean {
        return try {
            achievementDao.markMultipleAsSynced(achievementIds)
            Log.d(TAG, "✅ ${achievementIds.size} achievements marked as synced")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marking multiple achievements as synced: ${e.message}", e)
            false
        }
    }

    // ===== HELPER FUNCTIONS =====

    /**
     * Create and save a new achievement
     */
    suspend fun createAchievement(
        userId: String,
        achievementType: String,
        name: String,
        description: String,
        iconName: String,
        progress: Int = 0,
        isUnlocked: Boolean = false
    ): String? {
        return try {
            val achievementId = UUID.randomUUID().toString()

            val achievement = AchievementEntity(
                achievementId = achievementId,
                userId = userId,
                achievementType = achievementType,
                name = name,
                description = description,
                iconName = iconName,
                unlockedAt = if (isUnlocked) System.currentTimeMillis() else 0L,
                progress = progress,
                isUnlocked = isUnlocked,
                isSynced = false
            )

            val success = saveAchievement(achievement)
            if (success) achievementId else null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating achievement: ${e.message}", e)
            null
        }
    }

    /**
     * Award achievement by type (creates if doesn't exist, unlocks if exists)
     * 🔥 FIXED: Only triggers notifications for new achievements, prevents duplicates
     */
    suspend fun awardAchievement(
        userId: String,
        achievementType: String,
        name: String,
        description: String,
        iconName: String
    ): Boolean {
        return try {
            // Check if achievement already exists
            val existing = getAchievementByType(userId, achievementType)

            if (existing != null) {
                // Achievement exists, unlock it if not already unlocked
                if (!existing.isUnlocked) {
                    unlockAchievement(existing.achievementId)

                    // 🔥 FIXED: Check if notification already sent
                    if (!NotificationTracker.hasAchievementNotificationBeenSent(context, userId, achievementType)) {
                        try {
                            LocalNotificationManager.notifyAchievementUnlocked(
                                context = context,
                                achievementTitle = name,
                                achievementDescription = description
                            )
                            NotificationTracker.markAchievementNotificationAsSent(context, userId, achievementType)
                            Log.d(TAG, "🔔 Notification sent for: $name")
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error sending notification: ${e.message}", e)
                        }
                    } else {
                        Log.d(TAG, "⏭️ Skipping achievement notification (already sent for $achievementType)")
                    }

                    true
                } else {
                    Log.d(TAG, "⏭️ Achievement already unlocked: $achievementType")
                    false // Already unlocked
                }
            } else {
                // Achievement doesn't exist, create and unlock it
                val achievementId = createAchievement(
                    userId = userId,
                    achievementType = achievementType,
                    name = name,
                    description = description,
                    iconName = iconName,
                    isUnlocked = true,
                    progress = 100
                )

                if (achievementId != null) {
                    // 🔥 FIXED: Check if notification already sent
                    if (!NotificationTracker.hasAchievementNotificationBeenSent(context, userId, achievementType)) {
                        try {
                            LocalNotificationManager.notifyAchievementUnlocked(
                                context = context,
                                achievementTitle = name,
                                achievementDescription = description
                            )
                            NotificationTracker.markAchievementNotificationAsSent(context, userId, achievementType)
                            Log.d(TAG, "🔔 Notification sent for new achievement: $name")
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error sending notification: ${e.message}", e)
                        }
                    } else {
                        Log.d(TAG, "⏭️ Skipping achievement notification (already sent for $achievementType)")
                    }
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error awarding achievement: ${e.message}", e)
            false
        }
    }

    // ===== ACHIEVEMENT CHECK FUNCTIONS =====

    /**
     * Check and award "First Win" achievement
     */
    suspend fun checkFirstWinAchievement(userId: String, hasWon: Boolean): Boolean {
        if (!hasWon) return false

        return awardAchievement(
            userId = userId,
            achievementType = "FIRST_WIN",
            name = "First Victory",
            description = "Win your first game",
            iconName = "ic_trophy"
        )
    }

    /**
     * Check and award "Speed Demon" achievement
     */
    suspend fun checkSpeedDemonAchievement(userId: String, timeTaken: Int, threshold: Int = 30): Boolean {
        if (timeTaken > threshold) return false

        return awardAchievement(
            userId = userId,
            achievementType = "SPEED_DEMON",
            name = "Speed Demon",
            description = "Complete a game in under $threshold seconds",
            iconName = "ic_speed"
        )
    }

    /**
     * Check and award "Memory Guru" achievement
     */
    suspend fun checkMemoryGuruAchievement(userId: String, accuracy: Float, threshold: Float = 95f): Boolean {
        if (accuracy < threshold) return false

        return awardAchievement(
            userId = userId,
            achievementType = "MEMORY_GURU",
            name = "Memory Guru",
            description = "Achieve ${threshold.toInt()}% accuracy",
            iconName = "ic_brain"
        )
    }

    /**
     * 🆕 Check and award "Champion" achievement - Win 50 games
     */
    suspend fun checkChampionAchievement(userId: String): Boolean {
        return try {
            val winsCount = gameResultDao.getWinsCount(userId)
            if (winsCount >= 50) {
                awardAchievement(
                    userId = userId,
                    achievementType = "CHAMPION",
                    name = "Champion",
                    description = "Win 50 games",
                    iconName = "ic_trophy"
                )
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Champion achievement: ${e.message}", e)
            false
        }
    }

    /**
     * 🆕 Check and award "High Scorer" achievement - Score over 2000 in one game
     */
    suspend fun checkHighScorerAchievement(userId: String, score: Int): Boolean {
        if (score < 2000) return false

        return awardAchievement(
            userId = userId,
            achievementType = "HIGH_SCORER",
            name = "High Scorer",
            description = "Score over 2000 points in one game",
            iconName = "ic_star"
        )
    }

    /**
     * 🆕 Check and award "Persistent Player" achievement - Play 100 games
     */
    suspend fun checkPersistentAchievement(userId: String): Boolean {
        return try {
            val totalGames = gameResultDao.getTotalGamesCount(userId)
            if (totalGames >= 100) {
                awardAchievement(
                    userId = userId,
                    achievementType = "PERSISTENT",
                    name = "Persistent Player",
                    description = "Play 100 games",
                    iconName = "ic_schedule"
                )
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Persistent achievement: ${e.message}", e)
            false
        }
    }

    /**
     * 🆕 Check and award "Streak Master" achievement - Maintain a 7-day streak
     */
    suspend fun checkStreakMasterAchievement(userId: String): Boolean {
        return try {
            val userProfile = userProfileDao.getUserProfile(userId)
            val currentStreak = userProfile?.currentStreak ?: 0

            if (currentStreak >= 7) {
                awardAchievement(
                    userId = userId,
                    achievementType = "STREAK_MASTER",
                    name = "Streak Master",
                    description = "Maintain a 7-day streak",
                    iconName = "ic_fire"
                )
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Streak Master achievement: ${e.message}", e)
            false
        }
    }

    /**
     * 🆕 Check and award "Theme Explorer" achievement - Try all 5 themes
     */
    suspend fun checkThemeExplorerAchievement(userId: String): Boolean {
        return try {
            val allGames = gameResultDao.getAllGamesForUser(userId)
            val uniqueThemes = allGames.map { it.theme }.distinct()

            Log.d(TAG, "   Unique themes played: ${uniqueThemes.size} - $uniqueThemes")

            // If user has played 5 or more different themes, unlock
            if (uniqueThemes.size >= 5) {
                awardAchievement(
                    userId = userId,
                    achievementType = "THEME_EXPLORER",
                    name = "Theme Explorer",
                    description = "Try all 5 themes",
                    iconName = "ic_palette"
                )
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Theme Explorer achievement: ${e.message}", e)
            false
        }
    }

    /**
     * 🆕 Check and award "Arcade Master" achievement - Complete all arcade levels (16 levels)
     */
    suspend fun checkArcadeMasterAchievement(userId: String): Boolean {
        return try {
            val allLevels = levelProgressDao.getAllLevelsProgress(userId)
            val completedLevels = allLevels.filter { it.isCompleted && it.levelNumber <= 16 }

            if (completedLevels.size >= 16) {
                awardAchievement(
                    userId = userId,
                    achievementType = "ARCADE_MASTER",
                    name = "Arcade Master",
                    description = "Complete all arcade levels",
                    iconName = "ic_gamepad"
                )
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Arcade Master achievement: ${e.message}", e)
            false
        }
    }

    /**
     * 🆕 Check and award "Level Conqueror" achievement - Complete all 16 levels
     */
    suspend fun checkLevelConquerorAchievement(userId: String): Boolean {
        return try {
            val allLevels = levelProgressDao.getAllLevelsProgress(userId)
            val completedLevels = allLevels.filter { it.isCompleted && it.levelNumber <= 16 }

            if (completedLevels.size >= 16) {
                awardAchievement(
                    userId = userId,
                    achievementType = "LEVEL_CONQUEROR",
                    name = "Level Conqueror",
                    description = "Complete all 16 levels",
                    iconName = "ic_stars"
                )
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Level Conqueror achievement: ${e.message}", e)
            false
        }
    }

    /**
     * 🆕 Check and award "Flawless Victory" achievement - Complete a game with no mistakes
     */
    suspend fun checkFlawlessAchievement(userId: String, moves: Int, perfectMoves: Int): Boolean {
        // Flawless = moves equals perfect moves (no wrong guesses)
        if (moves != perfectMoves) return false

        return awardAchievement(
            userId = userId,
            achievementType = "FLAWLESS",
            name = "Flawless Victory",
            description = "Complete a game with no mistakes",
            iconName = "ic_check_circle"
        )
    }

    /**
     * 🔥 NEW: Check ALL achievements at once after a game
     * Call this from ViewModels after each game completion
     */
    suspend fun checkAllAchievements(
        userId: String,
        score: Int,
        moves: Int,
        perfectMoves: Int,
        timeTaken: Int,
        accuracy: Float,
        isWin: Boolean
    ) {
        try {
            Log.d(TAG, "🏆 Checking all achievements...")
            Log.d(TAG, "   Score: $score, Moves: $moves, Time: $timeTaken, Accuracy: $accuracy, Win: $isWin")

            // Check instant achievements (based on current game)
            Log.d(TAG, "   Checking High Scorer (need 2000+, have: $score)...")
            val highScorer = checkHighScorerAchievement(userId, score)
            if (highScorer) Log.d(TAG, "   🏆 HIGH SCORER UNLOCKED!")

            Log.d(TAG, "   Checking Flawless (need $perfectMoves moves, have: $moves)...")
            val flawless = checkFlawlessAchievement(userId, moves, perfectMoves)
            if (flawless) Log.d(TAG, "   🏆 FLAWLESS UNLOCKED!")

            // Check cumulative achievements (based on total stats)
            val totalGames = gameResultDao.getTotalGamesCount(userId)
            val totalWins = gameResultDao.getWinsCount(userId)
            Log.d(TAG, "   Total Games: $totalGames, Total Wins: $totalWins")

            Log.d(TAG, "   Checking Champion (need 50 wins, have: $totalWins)...")
            val champion = checkChampionAchievement(userId)
            if (champion) Log.d(TAG, "   🏆 CHAMPION UNLOCKED!")

            Log.d(TAG, "   Checking Persistent (need 100 games, have: $totalGames)...")
            val persistent = checkPersistentAchievement(userId)
            if (persistent) Log.d(TAG, "   🏆 PERSISTENT UNLOCKED!")

            val userProfile = userProfileDao.getUserProfile(userId)
            val currentStreak = userProfile?.currentStreak ?: 0
            Log.d(TAG, "   Checking Streak Master (need 7 days, have: $currentStreak)...")
            val streakMaster = checkStreakMasterAchievement(userId)
            if (streakMaster) Log.d(TAG, "   🏆 STREAK MASTER UNLOCKED!")

            val allGames = gameResultDao.getAllGamesForUser(userId)
            val uniqueThemes = allGames.map { it.theme }.distinct()
            Log.d(TAG, "   Checking Theme Explorer (need 5 themes, have: ${uniqueThemes.size} - $uniqueThemes)...")
            val themeExplorer = checkThemeExplorerAchievement(userId)
            if (themeExplorer) Log.d(TAG, "   🏆 THEME EXPLORER UNLOCKED!")

            val allLevels = levelProgressDao.getAllLevelsProgress(userId)
            val completedLevels = allLevels.filter { it.isCompleted && it.levelNumber <= 16 }
            Log.d(TAG, "   Checking Arcade Master (need 16 levels, have: ${completedLevels.size})...")
            val arcadeMaster = checkArcadeMasterAchievement(userId)
            if (arcadeMaster) Log.d(TAG, "   🏆 ARCADE MASTER UNLOCKED!")

            Log.d(TAG, "   Checking Level Conqueror (need 16 levels, have: ${completedLevels.size})...")
            val levelConqueror = checkLevelConquerorAchievement(userId)
            if (levelConqueror) Log.d(TAG, "   🏆 LEVEL CONQUEROR UNLOCKED!")

            Log.d(TAG, "✅ All achievements checked!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking all achievements: ${e.message}", e)
        }
    }

    /**
     * Delete all achievements for user
     */
    suspend fun deleteAllAchievementsForUser(userId: String): Boolean {
        return try {
            achievementDao.deleteAllAchievementsForUser(userId)
            Log.d(TAG, "✅ All achievements deleted for user: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting achievements: ${e.message}", e)
            false
        }
    }
}