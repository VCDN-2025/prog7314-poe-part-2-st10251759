package vcmsa.projects.prog7314.data.sync

import android.util.Log
import vcmsa.projects.prog7314.data.entities.LevelProgressEntity
import vcmsa.projects.prog7314.data.repository.LevelRepository

/*
    Code Attribution for: Connecting to Firebase Database
    ===================================================
    Firebase, 2025. Installation & Setup on Android | Firebase Realtime Database (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/database/android/start>
    [Accessed 18 November 2025].
*/


/**
 * Helper class for syncing game level progress between Firestore (cloud) and
 * local Room database. Handles downloading, merging, and updating level progress.
 */
class ProgressSyncHelper(
    private val levelRepository: LevelRepository,  // Repository to access local level data
    private val firestoreManager: FirestoreManager // Manager for Firestore operations
) {
    private val TAG = "ProgressSyncHelper" // Logging tag

    /**
     * Load progress from Firestore for a specific user and sync it to the local database.
     * - If no cloud progress exists, initialize fresh levels.
     * - If local data exists, merge cloud data with local data (cloud wins conflicts).
     * @param userId The UID of the currently logged-in user.
     * @return true if sync succeeded, false if any error occurred.
     */
    suspend fun loadProgressFromCloud(userId: String): Boolean {
        return try {
            Log.d(TAG, "Loading progress from Firestore for user: $userId")

            // Attempt to load game progress from Firestore
            val result = firestoreManager.loadGameProgress()

            if (result.isFailure) {
                // If Firestore fetch fails, log error and return false
                Log.e(TAG, "Failed to load from Firestore: ${result.exceptionOrNull()?.message}")
                return false
            }

            val cloudProgress = result.getOrNull()
            if (cloudProgress == null) {
                // No progress found in cloud - initialize local database with default levels
                Log.d(TAG, "No cloud progress found, initializing fresh")
                levelRepository.initializeLevelsForUser(userId)
                return true
            }

            Log.d(TAG, "Cloud progress found: ${cloudProgress.levelProgress.size} levels")

            // Fetch all levels from local database
            val localLevels = levelRepository.getAllLevelsProgress(userId)

            if (localLevels.isEmpty()) {
                // No local data - sync everything from cloud directly
                Log.d(TAG, "No local data, syncing from cloud...")
                syncCloudToLocal(userId, cloudProgress.levelProgress)
            } else {
                // Local data exists - merge with cloud data (cloud wins for conflicts)
                Log.d(TAG, "Merging cloud and local data...")
                mergeCloudAndLocal(userId, cloudProgress.levelProgress, localLevels)
            }

            Log.d(TAG, "Progress sync complete")
            true

        } catch (e: Exception) {
            // Catch any unexpected exception
            Log.e(TAG, "Error loading progress: ${e.message}", e)
            false
        }
    }

    /**
     * Sync all cloud levels to local Room database.
     * - Initializes 16 levels first, then applies cloud completion data.
     * @param userId The UID of the current user.
     * @param cloudLevels Map of cloud level data keyed by level number.
     */
    private suspend fun syncCloudToLocal(
        userId: String,
        cloudLevels: Map<Int, vcmsa.projects.prog7314.data.models.LevelData>
    ) {
        // Initialize default levels locally
        levelRepository.initializeLevelsForUser(userId)

        // Iterate through each level from the cloud and update local DB if completed
        cloudLevels.forEach { (levelNum, cloudData) ->
            if (cloudData.isCompleted) {
                // Update level progress in local DB and unlock next level
                levelRepository.completeLevelAndUnlockNext(
                    userId = userId,
                    levelNumber = levelNum,
                    stars = cloudData.stars,
                    score = cloudData.bestScore,
                    time = cloudData.bestTime,
                    moves = cloudData.bestMoves
                )
                Log.d(TAG, "Synced Level $levelNum: ${cloudData.stars} stars")
            }
        }
    }

    /**
     * Merge cloud level progress with local Room database.
     * - If level exists in cloud but not locally, sync cloud data.
     * - If level exists in both, use better score from cloud.
     * @param userId Current user ID.
     * @param cloudLevels Map of cloud level data.
     * @param localLevels List of local level entities from Room.
     */
    private suspend fun mergeCloudAndLocal(
        userId: String,
        cloudLevels: Map<Int, vcmsa.projects.prog7314.data.models.LevelData>,
        localLevels: List<LevelProgressEntity>
    ) {
        cloudLevels.forEach { (levelNum, cloudData) ->
            val localData = localLevels.find { it.levelNumber == levelNum }

            if (localData == null) {
                // Cloud has level, local doesn't - sync cloud data if completed
                if (cloudData.isCompleted) {
                    levelRepository.completeLevelAndUnlockNext(
                        userId = userId,
                        levelNumber = levelNum,
                        stars = cloudData.stars,
                        score = cloudData.bestScore,
                        time = cloudData.bestTime,
                        moves = cloudData.bestMoves
                    )
                }
            } else {
                // Both cloud and local exist - update local only if cloud has better score
                if (cloudData.bestScore >= localData.bestScore && cloudData.isCompleted) {
                    levelRepository.completeLevelAndUnlockNext(
                        userId = userId,
                        levelNumber = levelNum,
                        stars = cloudData.stars,
                        score = cloudData.bestScore,
                        time = cloudData.bestTime,
                        moves = cloudData.bestMoves
                    )
                    Log.d(TAG, "Updated Level $levelNum with cloud data (better score)")
                }
            }
        }
    }
}
