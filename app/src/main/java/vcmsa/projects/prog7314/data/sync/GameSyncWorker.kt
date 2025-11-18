package vcmsa.projects.prog7314.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.NetworkManager

/*
    Code Attribution for: Using a CoroutineWorker
    ===================================================
    Android Developers, 2024. CoroutineWorker | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/reference/kotlin/androidx/work/CoroutineWorker>
    [Accessed 18 November 2025].
*/

/**
 * WorkManager CoroutineWorker that handles syncing game progress (level and arcade sessions)
 * to a remote API. Runs in the background, even if the app is closed.
 */
class GameSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "GameSyncWorker" // Tag used for logging

    /**
     * The main work method executed by WorkManager in a background thread.
     * Uses IO dispatcher to avoid blocking the main thread.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting game sync...")

            // Check for network availability
            // NetworkManager is a singleton object, so we can directly call isNetworkAvailable()
            if (!NetworkManager.isNetworkAvailable()) {
                Log.d(TAG, "Network not available, skipping sync")
                return@withContext Result.retry() // Retry later when network is available
            }

            // Get the currently authenticated user's ID
            // AuthManager is an object, use getCurrentUser()?.uid
            val userId = AuthManager.getCurrentUser()?.uid

            // If no user is logged in, we cannot sync
            if (userId == null) {
                Log.e(TAG, "No user ID found, cannot sync")
                return@withContext Result.failure()
            }

            // Initialize repository provider with application context
            // RepositoryProvider provides access to all app repositories
            RepositoryProvider.initialize(applicationContext)
            val levelRepository = RepositoryProvider.getLevelRepository()
            val arcadeRepository = RepositoryProvider.getArcadeRepository()
            val apiRepository = RepositoryProvider.getApiRepository()

            // ===== SYNC LEVEL PROGRESS =====

            // Get all unsynced levels for the current user
            val unsyncedLevels = levelRepository.getUnsyncedLevels(userId)
            Log.d(TAG, "Found ${unsyncedLevels.size} unsynced levels")

            // Loop through each unsynced level and sync it
            unsyncedLevels.forEach { level ->
                try {
                    // TODO: Call API to sync level progress when endpoint is available
                    // val result = apiRepository.syncLevelProgress(level)

                    // For now, mark level as synced locally
                    levelRepository.markLevelAsSynced(userId, level.levelNumber)
                    Log.d(TAG, "✅ Synced level ${level.levelNumber}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing level ${level.levelNumber}: ${e.message}", e)
                }
            }

            // ===== SYNC ARCADE SESSIONS =====

            // Get all unsynced arcade sessions for the current user
            val unsyncedSessions = arcadeRepository.getUnsyncedSessions(userId)
            Log.d(TAG, "Found ${unsyncedSessions.size} unsynced arcade sessions")

            // Loop through each unsynced session and sync it
            unsyncedSessions.forEach { session ->
                try {
                    // TODO: Call API to sync arcade session when endpoint is available
                    // val result = apiRepository.syncArcadeSession(session)

                    // For now, mark session as synced locally
                    arcadeRepository.markSessionAsSynced(session.sessionId)
                    Log.d(TAG, "✅ Synced arcade session ${session.sessionId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing arcade session: ${e.message}", e)
                }
            }

            Log.d(TAG, "✅ Game sync completed successfully")
            Result.success() // Return success to WorkManager

        } catch (e: Exception) {
            // Catch any unexpected exception during sync
            Log.e(TAG, "❌ Error during game sync: ${e.message}", e)
            Result.retry() // Retry the sync later
        }
    }

    companion object {
        // Unique name used to schedule or identify this WorkManager task
        const val WORK_NAME = "game_sync_work"
    }
}
