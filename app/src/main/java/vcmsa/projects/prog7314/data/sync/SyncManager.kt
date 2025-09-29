package vcmsa.projects.prog7314.data.sync

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.utils.NetworkManager

class SyncManager(private val context: Context) {
    private val TAG = "SyncManager"

    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private var autoSyncJob: Job? = null
    private var isInitialized = false

    companion object {
        private const val SYNC_RETRY_DELAY = 5000L // 5 seconds
        private const val SYNC_INTERVAL = 30000L // 30 seconds
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    /**
     * Initialize sync manager and start monitoring
     */
    fun initialize() {
        if (isInitialized) return

        Log.d(TAG, "Initializing SyncManager")
        isInitialized = true

        // Start monitoring network changes
        coroutineScope.launch {
            NetworkManager.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Device came online - starting sync")
                    startAutoSync()
                } else {
                    Log.d(TAG, "Device went offline - stopping sync")
                    stopAutoSync()
                }
            }
        }

        // Initial sync if online
        if (NetworkManager.isNetworkAvailable()) {
            startAutoSync()
        }
    }

    /**
     * Start automatic syncing
     */
    private fun startAutoSync() {
        if (autoSyncJob?.isActive == true) return

        autoSyncJob = coroutineScope.launch {
            while (NetworkManager.isOnline.value) {
                performSync()
                delay(SYNC_INTERVAL)
            }
        }
    }

    /**
     * Stop automatic syncing
     */
    private fun stopAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = null
        _syncStatus.value = SyncStatus.IDLE
    }

    /**
     * Perform manual sync
     */
    suspend fun performManualSync(): Boolean {
        if (!NetworkManager.isNetworkAvailable()) {
            Log.w(TAG, "Cannot sync - no network connection")
            return false
        }

        return performSync()
    }

    /**
     * Main sync logic
     */
    private suspend fun performSync(): Boolean {
        if (_syncStatus.value == SyncStatus.SYNCING) {
            Log.d(TAG, "Sync already in progress, skipping")
            return false
        }

        _syncStatus.value = SyncStatus.SYNCING
        Log.d(TAG, "Starting sync process")

        return try {
            val userRepo = RepositoryProvider.getUserProfileRepository(context)
            val gameRepo = RepositoryProvider.getGameResultRepository(context)
            val achievementRepo = RepositoryProvider.getAchievementRepository(context)

            var syncSuccess = true

            // Sync user profiles
            val unsyncedProfiles = userRepo.getUnsyncedProfiles()
            if (unsyncedProfiles.isNotEmpty()) {
                Log.d(TAG, "Syncing ${unsyncedProfiles.size} user profiles")
                for (profile in unsyncedProfiles) {
                    val success = syncUserProfileToCloud(profile.userId)
                    if (success) {
                        userRepo.markAsSynced(profile.userId)
                        Log.d(TAG, "✅ Profile synced: ${profile.username}")
                    } else {
                        syncSuccess = false
                        Log.e(TAG, "❌ Failed to sync profile: ${profile.username}")
                    }
                }
            }

            // Sync game results
            val unsyncedGames = gameRepo.getUnsyncedGames()
            if (unsyncedGames.isNotEmpty()) {
                Log.d(TAG, "Syncing ${unsyncedGames.size} game results")
                for (game in unsyncedGames) {
                    val success = syncGameResultToCloud(game.gameId)
                    if (success) {
                        gameRepo.markAsSynced(game.gameId)
                        Log.d(TAG, "✅ Game synced: ${game.gameId}")
                    } else {
                        syncSuccess = false
                        Log.e(TAG, "❌ Failed to sync game: ${game.gameId}")
                    }
                }
            }

            // Sync achievements
            val unsyncedAchievements = achievementRepo.getUnsyncedAchievements()
            if (unsyncedAchievements.isNotEmpty()) {
                Log.d(TAG, "Syncing ${unsyncedAchievements.size} achievements")
                for (achievement in unsyncedAchievements) {
                    val success = syncAchievementToCloud(achievement.achievementId)
                    if (success) {
                        achievementRepo.markAsSynced(achievement.achievementId)
                        Log.d(TAG, "✅ Achievement synced: ${achievement.name}")
                    } else {
                        syncSuccess = false
                        Log.e(TAG, "❌ Failed to sync achievement: ${achievement.name}")
                    }
                }
            }

            // Update sync status
            if (syncSuccess) {
                _syncStatus.value = SyncStatus.SUCCESS
                _lastSyncTime.value = System.currentTimeMillis()
                Log.d(TAG, "✅ Sync completed successfully")
            } else {
                _syncStatus.value = SyncStatus.ERROR
                Log.e(TAG, "❌ Sync completed with errors")
            }

            // Reset to idle after delay
            delay(2000)
            _syncStatus.value = SyncStatus.IDLE

            syncSuccess

        } catch (e: Exception) {
            Log.e(TAG, "❌ Sync failed with exception: ${e.message}", e)
            _syncStatus.value = SyncStatus.ERROR

            // Reset to idle after delay
            delay(2000)
            _syncStatus.value = SyncStatus.IDLE

            false
        }
    }

    /**
     * Sync user profile to Firebase (placeholder)
     */
    private suspend fun syncUserProfileToCloud(userId: String): Boolean {
        return try {
            // TODO: Implement actual Firebase sync
            // For now, simulate network request
            delay(500)

            // Simulate success rate (90% success for testing)
            val success = (0..100).random() < 90

            if (success) {
                Log.d(TAG, "User profile uploaded to cloud: $userId")
            } else {
                Log.e(TAG, "Failed to upload user profile: $userId")
            }

            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception syncing user profile: ${e.message}", e)
            false
        }
    }

    /**
     * Sync game result to Firebase (placeholder)
     */
    private suspend fun syncGameResultToCloud(gameId: String): Boolean {
        return try {
            // TODO: Implement actual Firebase sync
            // For now, simulate network request
            delay(300)

            // Simulate success rate (95% success for testing)
            val success = (0..100).random() < 95

            if (success) {
                Log.d(TAG, "Game result uploaded to cloud: $gameId")
            } else {
                Log.e(TAG, "Failed to upload game result: $gameId")
            }

            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception syncing game result: ${e.message}", e)
            false
        }
    }

    /**
     * Sync achievement to Firebase (placeholder)
     */
    private suspend fun syncAchievementToCloud(achievementId: String): Boolean {
        return try {
            // TODO: Implement actual Firebase sync
            // For now, simulate network request
            delay(200)

            // Simulate success rate (98% success for testing)
            val success = (0..100).random() < 98

            if (success) {
                Log.d(TAG, "Achievement uploaded to cloud: $achievementId")
            } else {
                Log.e(TAG, "Failed to upload achievement: $achievementId")
            }

            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception syncing achievement: ${e.message}", e)
            false
        }
    }

    /**
     * Get unsynced data counts
     */
    suspend fun getUnsyncedCounts(): UnsyncedCounts {
        return try {
            val userRepo = RepositoryProvider.getUserProfileRepository(context)
            val gameRepo = RepositoryProvider.getGameResultRepository(context)
            val achievementRepo = RepositoryProvider.getAchievementRepository(context)

            UnsyncedCounts(
                profiles = userRepo.getUnsyncedProfiles().size,
                games = gameRepo.getUnsyncedGames().size,
                achievements = achievementRepo.getUnsyncedAchievements().size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced counts: ${e.message}", e)
            UnsyncedCounts()
        }
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopAutoSync()
        isInitialized = false
        Log.d(TAG, "SyncManager cleaned up")
    }
}

enum class SyncStatus {
    IDLE,       // Not syncing
    SYNCING,    // Currently syncing
    SUCCESS,    // Last sync successful
    ERROR       // Last sync failed
}

data class UnsyncedCounts(
    val profiles: Int = 0,
    val games: Int = 0,
    val achievements: Int = 0
) {
    val total: Int get() = profiles + games + achievements
    val hasUnsynced: Boolean get() = total > 0
}