package vcmsa.projects.prog7314.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.dao.LevelProgressDao
import vcmsa.projects.prog7314.data.entities.LevelProgressEntity
import vcmsa.projects.prog7314.utils.LocalNotificationManager

class LevelRepository(
    private val levelProgressDao: LevelProgressDao,
    private val context: Context  // 🔥 ADD CONTEXT
) {
    private val TAG = "LevelRepository"

    companion object {
        const val TOTAL_LEVELS = 16
        const val LEVELS_PER_DIFFICULTY = 4
    }

    /**
     * Initialize levels for a user (called on first login)
     */
    suspend fun initializeLevelsForUser(userId: String): Boolean {
        return try {
            Log.d(TAG, "Initializing levels for user: $userId")

            val existingLevels = levelProgressDao.getAllLevelsProgress(userId)
            if (existingLevels.isNotEmpty()) {
                Log.d(TAG, "Levels already initialized")
                return true
            }

            val levels = mutableListOf<LevelProgressEntity>()
            for (i in 1..TOTAL_LEVELS) {
                levels.add(
                    LevelProgressEntity(
                        userId = userId,
                        levelNumber = i,
                        stars = 0,
                        bestScore = 0,
                        bestTime = 0,
                        bestMoves = 0,
                        isUnlocked = i == 1, // Only level 1 is unlocked initially
                        isCompleted = false,
                        lastPlayed = 0,
                        timesPlayed = 0,
                        isSynced = false
                    )
                )
            }

            levelProgressDao.insertAllLevelProgress(levels)
            Log.d(TAG, "✅ Successfully initialized $TOTAL_LEVELS levels")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing levels: ${e.message}", e)
            false
        }
    }

    /**
     * Get all levels progress for a user
     */
    suspend fun getAllLevelsProgress(userId: String): List<LevelProgressEntity> {
        return try {
            levelProgressDao.getAllLevelsProgress(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all levels: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get all levels progress as Flow
     */
    fun getAllLevelsProgressFlow(userId: String): Flow<List<LevelProgressEntity>> {
        return levelProgressDao.getAllLevelsProgressFlow(userId)
    }

    /**
     * Get specific level progress
     */
    suspend fun getLevelProgress(userId: String, levelNumber: Int): LevelProgressEntity? {
        return try {
            levelProgressDao.getLevelProgress(userId, levelNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting level progress: ${e.message}", e)
            null
        }
    }

    /**
     * Get completed levels count
     */
    suspend fun getCompletedLevelsCount(userId: String): Int {
        return try {
            levelProgressDao.getCompletedLevelsCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting completed count: ${e.message}", e)
            0
        }
    }

    /**
     * Get completed levels count as Flow
     */
    fun getCompletedLevelsCountFlow(userId: String): Flow<Int> {
        return levelProgressDao.getCompletedLevelsCountFlow(userId)
    }

    /**
     * Complete a level and update progress
     * 🔥 NOW WITH NOTIFICATIONS!
     */
    suspend fun completeLevelAndUnlockNext(
        userId: String,
        levelNumber: Int,
        stars: Int,
        score: Int,
        time: Int,
        moves: Int
    ): Boolean {
        return try {
            Log.d(TAG, "Completing level $levelNumber for user $userId")

            // Check if this is the first level completion
            val completedCount = getCompletedLevelsCount(userId)
            val isFirstLevel = completedCount == 0

            // Update current level
            levelProgressDao.updateLevelResult(
                userId = userId,
                levelNumber = levelNumber,
                stars = stars,
                score = score,
                time = time,
                moves = moves
            )

            // 🔥 NOTIFY: First Level Completed
            if (isFirstLevel) {
                try {
                    LocalNotificationManager.notifyFirstLevelCompleted(context)
                    Log.d(TAG, "🔔 First level completion notification sent")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error sending first level notification: ${e.message}", e)
                }
            }

            // Unlock next level if exists
            if (levelNumber < TOTAL_LEVELS) {
                val nextLevel = levelNumber + 1
                levelProgressDao.unlockLevel(userId, nextLevel)
                Log.d(TAG, "✅ Unlocked level $nextLevel")

                // 🔥 NOTIFY: Level Unlocked
                try {
                    LocalNotificationManager.notifyLevelUnlocked(context, nextLevel)
                    Log.d(TAG, "🔔 Level unlock notification sent for level $nextLevel")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error sending level unlock notification: ${e.message}", e)
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error completing level: ${e.message}", e)
            false
        }
    }

    /**
     * Get total stars earned
     */
    suspend fun getTotalStars(userId: String): Int {
        return try {
            levelProgressDao.getTotalStars(userId) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total stars: ${e.message}", e)
            0
        }
    }

    /**
     * Get unsynced levels for syncing with API
     */
    suspend fun getUnsyncedLevels(userId: String): List<LevelProgressEntity> {
        return try {
            levelProgressDao.getUnsyncedLevels(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced levels: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Mark level as synced
     */
    suspend fun markLevelAsSynced(userId: String, levelNumber: Int) {
        try {
            levelProgressDao.markAsSynced(userId, levelNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking level as synced: ${e.message}", e)
        }
    }
}