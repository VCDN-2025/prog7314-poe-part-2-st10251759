package vcmsa.projects.prog7314.data.sync

import android.util.Log
import vcmsa.projects.prog7314.data.entities.LevelProgressEntity
import vcmsa.projects.prog7314.data.repository.LevelRepository

class ProgressSyncHelper(
    private val levelRepository: LevelRepository,
    private val firestoreManager: FirestoreManager
) {
    private val TAG = "ProgressSyncHelper"

    /**
     * Load progress from Firestore and sync to RoomDB
     */
    suspend fun loadProgressFromCloud(userId: String): Boolean {
        return try {
            Log.d(TAG, "Loading progress from Firestore for user: $userId")

            // Load from Firestore
            val result = firestoreManager.loadGameProgress()

            if (result.isFailure) {
                Log.e(TAG, "Failed to load from Firestore: ${result.exceptionOrNull()?.message}")
                return false
            }

            val cloudProgress = result.getOrNull()
            if (cloudProgress == null) {
                Log.d(TAG, "No cloud progress found, initializing fresh")
                levelRepository.initializeLevelsForUser(userId)
                return true
            }

            Log.d(TAG, "Cloud progress found: ${cloudProgress.levelProgress.size} levels")

            // Check if local DB has any data
            val localLevels = levelRepository.getAllLevelsProgress(userId)

            if (localLevels.isEmpty()) {
                // No local data - sync everything from cloud
                Log.d(TAG, "No local data, syncing from cloud...")
                syncCloudToLocal(userId, cloudProgress.levelProgress)
            } else {
                // Local data exists - merge with cloud (cloud wins for conflicts)
                Log.d(TAG, "Merging cloud and local data...")
                mergeCloudAndLocal(userId, cloudProgress.levelProgress, localLevels)
            }

            Log.d(TAG, "Progress sync complete")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Error loading progress: ${e.message}", e)
            false
        }
    }

    private suspend fun syncCloudToLocal(
        userId: String,
        cloudLevels: Map<Int, vcmsa.projects.prog7314.data.models.LevelData>
    ) {
        // Initialize all 16 levels first
        levelRepository.initializeLevelsForUser(userId)

        // Update with cloud data
        cloudLevels.forEach { (levelNum, cloudData) ->
            if (cloudData.isCompleted) {
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

    private suspend fun mergeCloudAndLocal(
        userId: String,
        cloudLevels: Map<Int, vcmsa.projects.prog7314.data.models.LevelData>,
        localLevels: List<LevelProgressEntity>
    ) {
        cloudLevels.forEach { (levelNum, cloudData) ->
            val localData = localLevels.find { it.levelNumber == levelNum }

            if (localData == null) {
                // Cloud has data, local doesn't - sync from cloud
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
                // Both have data - use better score (cloud wins if tied)
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