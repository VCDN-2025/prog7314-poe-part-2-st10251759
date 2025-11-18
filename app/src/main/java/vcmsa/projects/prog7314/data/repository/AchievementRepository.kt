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

/*
    Code Attribution for: Repositories
    ===================================================
    Android Developers, 2025. Data layer (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/architecture/data-layer>
    [Accessed 18 November 2025].
*/

class AchievementRepository(
    private val achievementDao: AchievementDao,
    private val gameResultDao: GameResultDao,
    private val userProfileDao: UserProfileDao,
    private val levelProgressDao: LevelProgressDao,
    private val context: Context  // 🔥 Context for notifications
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
     * 🔥 FIXED: Properly unlocks achievement FIRST, then handles notification
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
                // Achievement exists
                if (!existing.isUnlocked) {
                    // 🔥 UNLOCK THE ACHIEVEMENT FIRST
                    val unlocked = unlockAchievement(existing.achievementId)

                    if (unlocked) {
                        Log.d(TAG, "✅ Achievement '$name' unlocked!")

                        // 🔥 THEN check if notification should be sent
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
                            Log.d(TAG, "⏭️ Skipping notification (already sent for $achievementType)")
                        }
                    }

                    return unlocked
                } else {
                    Log.d(TAG, "⏭️ Achievement '$name' already unlocked")
                    return false // Already unlocked, but this is not an error
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
                    Log.d(TAG, "✅ New achievement '$name' created and unlocked!")

                    // Send notification
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
                    }
                    return true
                } else {
                    Log.e(TAG, "❌ Failed to create achievement: $name")
                    return false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error awarding achievement: ${e.message}", e)
            return false
        }
    }

    // ===== ACHIEVEMENT CHECKERS =====

    /**
     * Check and award "First Win" achievement - Win your first game
     */
    suspend fun checkFirstWinAchievement(userId: String, isWin: Boolean): Boolean {
        if (!isWin) return false

        val totalWins = gameResultDao.getWinsCount(userId)
        if (totalWins >= 1) {
            return awardAchievement(
                userId = userId,
                achievementType = "FIRST_WIN",
                name = "First Victory",
                description = "Win your first game",
                iconName = "ic_trophy"
            )
        }
        return false
    }

    /**
     * Check and award "Speed Demon" achievement - Complete a game in under 30 seconds
     */
    suspend fun checkSpeedDemonAchievement(userId: String, timeTaken: Int, targetTime: Int = 30): Boolean {
        if (timeTaken <= targetTime) {
            return awardAchievement(
                userId = userId,
                achievementType = "SPEED_DEMON",
                name = "Speed Demon",
                description = "Complete a game in under 30 seconds",
                iconName = "ic_flash"
            )
        }
        return false
    }

    /**
     * Check and award "Memory Guru" achievement - Achieve 95% accuracy
     */
    suspend fun checkMemoryGuruAchievement(userId: String, accuracy: Float, targetAccuracy: Float = 95f): Boolean {
        if (accuracy >= targetAccuracy) {
            return awardAchievement(
                userId = userId,
                achievementType = "MEMORY_GURU",
                name = "Memory Guru",
                description = "Achieve 95% accuracy",
                iconName = "ic_brain"
            )
        }
        return false
    }

    /**
     * Check and award "Champion" achievement - Win 50 games
     */
    suspend fun checkChampionAchievement(userId: String): Boolean {
        return try {
            val totalWins = gameResultDao.getWinsCount(userId)
            if (totalWins >= 50) {
                awardAchievement(
                    userId = userId,
                    achievementType = "CHAMPION",
                    name = "Champion",
                    description = "Win 50 games",
                    iconName = "ic_medal"
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
     * Check and award "High Scorer" achievement - Score over 2000 points in one game
     */
    suspend fun checkHighScorerAchievement(userId: String, score: Int): Boolean {
        if (score >= 2000) {
            return awardAchievement(
                userId = userId,
                achievementType = "HIGH_SCORER",
                name = "High Scorer",
                description = "Score over 2000 points in one game",
                iconName = "ic_star"
            )
        }
        return false
    }

    /**
     * Check and award "Persistent Player" achievement - Play 100 games
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
                    iconName = "ic_gamepad"
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
     * Check and award "Streak Master" achievement - Maintain a 7-day streak
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
     * Check and award "Theme Explorer" achievement - Try all 5 themes
     */
    suspend fun checkThemeExplorerAchievement(userId: String): Boolean {
        return try {
            // 🔥 FIXED: Changed from getAllGameResults to getAllGamesForUser
            val allResults = gameResultDao.getAllGamesForUser(userId)

            // Get unique themes from game results
            val uniqueThemes = allResults.map { it.theme }.toSet()

            // 🔥 FIXED: Changed from .size to .count() and using toSet() to get distinct themes
            if (uniqueThemes.count() >= 5) {
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
     * Check and award "Arcade Master" achievement - Complete all arcade levels (16 levels)
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
     * Check and award "Level Conqueror" achievement - Complete all 16 levels
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
     * Check and award "Flawless Victory" achievement - Complete a game with no mistakes
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
     * Check ALL achievements at once after a game
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
            Log.d(TAG, "🏆 ========== CHECKING ALL ACHIEVEMENTS ==========")
            Log.d(TAG, "   Score: $score, Moves: $moves, Time: $timeTaken, Accuracy: $accuracy%, Win: $isWin")

            // 1. First Win (requires win)
            if (isWin) {
                Log.d(TAG, "   Checking First Win...")
                val firstWin = checkFirstWinAchievement(userId, isWin)
                if (firstWin) Log.d(TAG, "   ✅ FIRST WIN UNLOCKED!")
            }

            // 2. High Scorer (instant check)
            Log.d(TAG, "   Checking High Scorer (need 2000+, have: $score)...")
            val highScorer = checkHighScorerAchievement(userId, score)
            if (highScorer) Log.d(TAG, "   ✅ HIGH SCORER UNLOCKED!")

            // 3. Flawless (instant check)
            Log.d(TAG, "   Checking Flawless (need $perfectMoves moves, have: $moves)...")
            val flawless = checkFlawlessAchievement(userId, moves, perfectMoves)
            if (flawless) Log.d(TAG, "   ✅ FLAWLESS UNLOCKED!")

            // 4. Speed Demon (instant check)
            Log.d(TAG, "   Checking Speed Demon (need ≤30s, have: ${timeTaken}s)...")
            val speedDemon = checkSpeedDemonAchievement(userId, timeTaken, 30)
            if (speedDemon) Log.d(TAG, "   ✅ SPEED DEMON UNLOCKED!")

            // 5. Memory Guru (instant check)
            Log.d(TAG, "   Checking Memory Guru (need ≥95%, have: $accuracy%)...")
            val memoryGuru = checkMemoryGuruAchievement(userId, accuracy, 95f)
            if (memoryGuru) Log.d(TAG, "   ✅ MEMORY GURU UNLOCKED!")

            // 6. Cumulative achievements
            val totalGames = gameResultDao.getTotalGamesCount(userId)
            val totalWins = gameResultDao.getWinsCount(userId)
            Log.d(TAG, "   Total Stats - Games: $totalGames, Wins: $totalWins")

            // 7. Champion (50 wins)
            Log.d(TAG, "   Checking Champion (need 50 wins, have: $totalWins)...")
            val champion = checkChampionAchievement(userId)
            if (champion) Log.d(TAG, "   ✅ CHAMPION UNLOCKED!")

            // 8. Persistent (100 games)
            Log.d(TAG, "   Checking Persistent (need 100 games, have: $totalGames)...")
            val persistent = checkPersistentAchievement(userId)
            if (persistent) Log.d(TAG, "   ✅ PERSISTENT UNLOCKED!")

            // 9. Streak Master
            val userProfile = userProfileDao.getUserProfile(userId)
            val currentStreak = userProfile?.currentStreak ?: 0
            Log.d(TAG, "   Checking Streak Master (need 7 days, have: $currentStreak days)...")
            val streakMaster = checkStreakMasterAchievement(userId)
            if (streakMaster) Log.d(TAG, "   ✅ STREAK MASTER UNLOCKED!")

            // 10. Theme Explorer
            Log.d(TAG, "   Checking Theme Explorer...")
            val themeExplorer = checkThemeExplorerAchievement(userId)
            if (themeExplorer) Log.d(TAG, "   ✅ THEME EXPLORER UNLOCKED!")

            // 11. Arcade Master / Level Conqueror
            Log.d(TAG, "   Checking Arcade Master / Level Conqueror...")
            val arcadeMaster = checkArcadeMasterAchievement(userId)
            if (arcadeMaster) Log.d(TAG, "   ✅ ARCADE MASTER UNLOCKED!")

            val levelConqueror = checkLevelConquerorAchievement(userId)
            if (levelConqueror) Log.d(TAG, "   ✅ LEVEL CONQUEROR UNLOCKED!")

            Log.d(TAG, "🏆 ========== ACHIEVEMENT CHECK COMPLETE ==========")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in checkAllAchievements: ${e.message}", e)
        }
    }
}