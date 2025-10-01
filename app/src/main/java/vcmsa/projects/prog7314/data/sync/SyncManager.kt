package vcmsa.projects.prog7314.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.repository.ApiRepository
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.utils.NetworkManager
import java.util.concurrent.TimeUnit

class SyncManager(private val context: Context) {

    private val TAG = "SyncManager"
    private val apiRepository = ApiRepository()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val SYNC_WORK_NAME = "memory_match_sync"
        private const val SYNC_INTERVAL_HOURS = 1L
    }

    /**
     * Initialize sync manager and setup periodic sync
     */
    fun initialize() {
        Log.d(TAG, "Initializing SyncManager")
        setupPeriodicSync()

        // Perform initial sync if online
        if (NetworkManager.isNetworkAvailable()) {
            performManualSync()
        }
    }

    /**
     * Setup periodic background sync using WorkManager
     */
    private fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        Log.d(TAG, "✅ Periodic sync scheduled every $SYNC_INTERVAL_HOURS hour(s)")
    }

    /**
     * Perform manual sync immediately
     */
    fun performManualSync(): Boolean {
        if (!NetworkManager.isNetworkAvailable()) {
            Log.w(TAG, "⚠️ Cannot sync: No network connection")
            return false
        }

        coroutineScope.launch {
            try {
                Log.d(TAG, "🔄 Starting manual sync...")

                val userRepo = RepositoryProvider.getUserProfileRepository(context)
                val gameRepo = RepositoryProvider.getGameResultRepository(context)
                val achievementRepo = RepositoryProvider.getAchievementRepository(context)

                // Get all unsynced data
                val unsyncedProfiles = userRepo.getUnsyncedProfiles()
                val unsyncedGames = gameRepo.getUnsyncedGames()
                val unsyncedAchievements = achievementRepo.getUnsyncedAchievements()

                Log.d(TAG, "📊 Unsynced data: ${unsyncedProfiles.size} profiles, " +
                        "${unsyncedGames.size} games, ${unsyncedAchievements.size} achievements")

                var successCount = 0
                var failCount = 0

                // Sync user profiles
                unsyncedProfiles.forEach { profile ->
                    val result = apiRepository.syncUserProfile(profile)
                    if (result.isSuccess) {
                        userRepo.markAsSynced(profile.userId)
                        successCount++
                    } else {
                        failCount++
                        Log.e(TAG, "Failed to sync profile: ${profile.userId}")
                    }
                }

                // Sync game results
                unsyncedGames.forEach { game ->
                    val result = apiRepository.syncGameResult(game)
                    if (result.isSuccess) {
                        gameRepo.markAsSynced(game.gameId)
                        successCount++
                    } else {
                        failCount++
                        Log.e(TAG, "Failed to sync game: ${game.gameId}")
                    }
                }

                // Sync achievements
                unsyncedAchievements.forEach { achievement ->
                    val result = apiRepository.syncAchievement(achievement)
                    if (result.isSuccess) {
                        achievementRepo.markAsSynced(achievement.achievementId)
                        successCount++
                    } else {
                        failCount++
                        Log.e(TAG, "Failed to sync achievement: ${achievement.achievementId}")
                    }
                }

                Log.d(TAG, "✅ Manual sync completed: $successCount succeeded, $failCount failed")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Manual sync error: ${e.message}", e)
            }
        }

        return true
    }

    /**
     * Get count of unsynced items
     */
    suspend fun getUnsyncedCounts(): SyncCounts {
        val userRepo = RepositoryProvider.getUserProfileRepository(context)
        val gameRepo = RepositoryProvider.getGameResultRepository(context)
        val achievementRepo = RepositoryProvider.getAchievementRepository(context)

        val profiles = userRepo.getUnsyncedProfiles().size
        val games = gameRepo.getUnsyncedGames().size
        val achievements = achievementRepo.getUnsyncedAchievements().size

        return SyncCounts(profiles, games, achievements)
    }

    /**
     * Cancel all scheduled sync work
     */
    fun cleanup() {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        Log.d(TAG, "🧹 Sync work cancelled")
    }
}

data class SyncCounts(
    val profiles: Int,
    val games: Int,
    val achievements: Int
) {
    val total: Int get() = profiles + games + achievements
}

/**
 * Background worker for periodic sync
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "SyncWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "🔄 Starting background sync...")

        return try {
            val syncManager = SyncManager(applicationContext)
            val success = syncManager.performManualSync()

            if (success) {
                Log.d(TAG, "✅ Background sync completed successfully")
                Result.success()
            } else {
                Log.w(TAG, "⚠️ Background sync failed - will retry")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Background sync error: ${e.message}", e)
            Result.failure()
        }
    }
}