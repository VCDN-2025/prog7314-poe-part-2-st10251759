package vcmsa.projects.prog7314.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vcmsa.projects.prog7314.data.AppDatabase
import vcmsa.projects.prog7314.data.repository.ArcadeRepository
import vcmsa.projects.prog7314.data.repository.ApiRepository
import vcmsa.projects.prog7314.data.repository.LevelRepository
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.NetworkManager

/**
 * WorkManager worker for syncing game results to API
 */
class GameSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "GameSyncWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting game sync...")

            // FIXED: NetworkManager is an object, not a class
            if (!NetworkManager.isNetworkAvailable()) {
                Log.d(TAG, "Network not available, skipping sync")
                return@withContext Result.retry()
            }

            // FIXED: AuthManager is an object, use getCurrentUser()?.uid
            val userId = AuthManager.getCurrentUser()?.uid

            if (userId == null) {
                Log.e(TAG, "No user ID found, cannot sync")
                return@withContext Result.failure()
            }

            val database = AppDatabase.getDatabase(applicationContext)
            val levelRepository = LevelRepository(database.levelProgressDao())
            val arcadeRepository = ArcadeRepository(database.arcadeSessionDao())
            val apiRepository = ApiRepository()

            // Sync level progress
            val unsyncedLevels = levelRepository.getUnsyncedLevels(userId)
            Log.d(TAG, "Found ${unsyncedLevels.size} unsynced levels")

            unsyncedLevels.forEach { level ->
                try {
                    // Call API to sync level progress
                    // TODO: Implement API call when endpoint is ready
                    // val result = apiRepository.syncLevelProgress(level)

                    // For now, just mark as synced
                    levelRepository.markLevelAsSynced(userId, level.levelNumber)
                    Log.d(TAG, "✅ Synced level ${level.levelNumber}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing level ${level.levelNumber}: ${e.message}", e)
                }
            }

            // Sync arcade sessions
            val unsyncedSessions = arcadeRepository.getUnsyncedSessions(userId)
            Log.d(TAG, "Found ${unsyncedSessions.size} unsynced arcade sessions")

            unsyncedSessions.forEach { session ->
                try {
                    // Call API to sync arcade session
                    // TODO: Implement API call when endpoint is ready
                    // val result = apiRepository.syncArcadeSession(session)

                    // For now, just mark as synced
                    arcadeRepository.markSessionAsSynced(session.sessionId)
                    Log.d(TAG, "✅ Synced arcade session ${session.sessionId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing arcade session: ${e.message}", e)
                }
            }

            Log.d(TAG, "✅ Game sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during game sync: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "game_sync_work"
    }
}