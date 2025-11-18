package vcmsa.projects.prog7314.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.dao.LevelProgressDao
import vcmsa.projects.prog7314.data.entities.LevelProgressEntity
import vcmsa.projects.prog7314.utils.LocalNotificationManager
import vcmsa.projects.prog7314.utils.NotificationTracker

/*
    Code Attribution for: Repositories
    ===================================================
    Android Developers, 2025. Data layer (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/architecture/data-layer>
    [Accessed 18 November 2025].
*/

/**
 * Repository responsible for managing user level progress.
 *
 * Provides methods to:
 * - Initialize levels for new users.
 * - Fetch level progress (all levels or specific level).
 * - Track completed levels and total stars.
 * - Complete levels, unlock next levels, and send notifications for milestones.
 * - Sync level progress with a backend API.
 *
 * Interacts with:
 * - LevelProgressDao: Local Room database for level persistence.
 * - LocalNotificationManager: Sends notifications when milestones occur.
 * - NotificationTracker: Ensures notifications are sent only once per milestone.
 */
class LevelRepository(
    private val levelProgressDao: LevelProgressDao, // DAO for accessing level progress in local database
    private val context: Context  // Android context required for sending local notifications
) {
    private val TAG = "LevelRepository" // Tag for logging

    companion object {
        const val TOTAL_LEVELS = 16 // Total number of levels in the app
        const val LEVELS_PER_DIFFICULTY = 4 // Levels grouped by difficulty
    }

    /**
     * Initialize all levels for a user. Called on first login.
     * - Creates LevelProgressEntity for each level.
     * - Unlocks only level 1 initially.
     * - Saves all levels to the local database.
     *
     * @param userId ID of the user for whom levels are being initialized
     * @return Boolean indicating success (true) or failure (false)
     */
    suspend fun initializeLevelsForUser(userId: String): Boolean {
        return try {
            Log.d(TAG, "Initializing levels for user: $userId")

            // Check if user already has level progress to avoid re-initializing
            val existingLevels = levelProgressDao.getAllLevelsProgress(userId)
            if (existingLevels.isNotEmpty()) {
                Log.d(TAG, "Levels already initialized")
                return true
            }

            // Create LevelProgressEntity objects for all levels
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
                        isSynced = false // Unsynced until uploaded to API
                    )
                )
            }

            // Insert all level progress entries into the database
            levelProgressDao.insertAllLevelProgress(levels)
            Log.d(TAG, "✅ Successfully initialized $TOTAL_LEVELS levels")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing levels: ${e.message}", e)
            false
        }
    }

    /**
     * Fetch all levels progress for a specific user.
     *
     * @param userId User ID for fetching levels
     * @return List of LevelProgressEntity, empty if error occurs
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
     * Fetch all levels progress as a Flow for real-time updates.
     *
     * Useful for UI observers to automatically react to database changes.
     *
     * @param userId User ID
     * @return Flow emitting lists of LevelProgressEntity
     */
    fun getAllLevelsProgressFlow(userId: String): Flow<List<LevelProgressEntity>> {
        return levelProgressDao.getAllLevelsProgressFlow(userId)
    }

    /**
     * Fetch a specific level progress for a user.
     *
     * @param userId User ID
     * @param levelNumber Level number to fetch
     * @return LevelProgressEntity if exists, null otherwise
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
     * Get the count of levels completed by a user.
     *
     * @param userId User ID
     * @return Number of completed levels, 0 if error occurs
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
     * Get completed levels count as Flow for real-time observation.
     *
     * @param userId User ID
     * @return Flow emitting integer of completed levels
     */
    fun getCompletedLevelsCountFlow(userId: String): Flow<Int> {
        return levelProgressDao.getCompletedLevelsCountFlow(userId)
    }

    /**
     * Check if a specific level is unlocked for a user.
     *
     * @param userId User ID
     * @param levelNumber Level number
     * @return Boolean indicating if level is unlocked
     */
    suspend fun isLevelUnlocked(userId: String, levelNumber: Int): Boolean {
        return try {
            val level = levelProgressDao.getLevelProgress(userId, levelNumber)
            level?.isUnlocked ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if level is unlocked: ${e.message}", e)
            false
        }
    }

    /**
     * Complete a level and unlock the next one.
     * - Updates the current level result (stars, score, time, moves).
     * - Sends first-level-completion notification if applicable.
     * - Unlocks the next level if it exists and sends notification only if newly unlocked.
     *
     * @param userId User ID
     * @param levelNumber Level being completed
     * @param stars Number of stars earned
     * @param score Score achieved
     * @param time Completion time
     * @param moves Number of moves used
     * @return Boolean indicating success or failure
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

            // Determine if this is the first level completion
            val completedCount = getCompletedLevelsCount(userId)
            val isFirstLevel = completedCount == 0

            // Update current level stats in database
            levelProgressDao.updateLevelResult(
                userId = userId,
                levelNumber = levelNumber,
                stars = stars,
                score = score,
                time = time,
                moves = moves
            )

            // Send first-level completion notification if applicable
            if (isFirstLevel && !NotificationTracker.hasFirstLevelNotificationBeenSent(context, userId)) {
                try {
                    LocalNotificationManager.notifyFirstLevelCompleted(context)
                    NotificationTracker.markFirstLevelNotificationAsSent(context, userId)
                    Log.d(TAG, "🔔 First level completion notification sent")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error sending first level notification: ${e.message}", e)
                }
            }

            // Unlock next level if it exists
            if (levelNumber < TOTAL_LEVELS) {
                val nextLevel = levelNumber + 1

                // Check if level was already unlocked
                val wasAlreadyUnlocked = isLevelUnlocked(userId, nextLevel)

                levelProgressDao.unlockLevel(userId, nextLevel)
                Log.d(TAG, "✅ Unlocked level $nextLevel")

                // Send notification only if newly unlocked
                if (!wasAlreadyUnlocked && !NotificationTracker.hasLevelUnlockBeenSent(context, userId, nextLevel)) {
                    try {
                        LocalNotificationManager.notifyLevelUnlocked(context, nextLevel)
                        NotificationTracker.markLevelUnlockAsSent(context, userId, nextLevel)
                        Log.d(TAG, "🔔 Level unlock notification sent for level $nextLevel")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error sending level unlock notification: ${e.message}", e)
                    }
                } else {
                    Log.d(TAG, "⏭️ Skipping level unlock notification (already sent or already unlocked)")
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error completing level: ${e.message}", e)
            false
        }
    }

    /**
     * Get total stars earned by a user across all levels.
     *
     * @param userId User ID
     * @return Total stars, 0 if error occurs
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
     * Get unsynced levels for syncing with API.
     *
     * @param userId User ID
     * @return List of LevelProgressEntity not yet synced
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
     * Mark a specific level as synced with the API.
     *
     * @param userId User ID
     * @param levelNumber Level number to mark as synced
     */
    suspend fun markLevelAsSynced(userId: String, levelNumber: Int) {
        try {
            levelProgressDao.markAsSynced(userId, levelNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking level as synced: ${e.message}", e)
        }
    }
}
