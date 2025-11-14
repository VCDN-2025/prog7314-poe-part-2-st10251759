package vcmsa.projects.prog7314.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.dao.AchievementDao
import vcmsa.projects.prog7314.data.entities.AchievementEntity
import java.util.UUID

class AchievementRepository(
    private val achievementDao: AchievementDao
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
                    true
                } else {
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
                    progress = 100,
                    isUnlocked = true
                )
                achievementId != null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error awarding achievement: ${e.message}", e)
            false
        }
    }

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
            description = "Achieve ${threshold}% accuracy",
            iconName = "ic_brain"
        )
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