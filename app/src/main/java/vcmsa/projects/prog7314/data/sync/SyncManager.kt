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

    // ===== FIRESTORE SYNC METHODS =====

    private val firestoreManager = FirestoreManager()

    /**
     * Sync TO Firestore (upload local data to cloud)
     * Call this after each game or when user logs in
     */
    fun syncToFirestore() {
        if (!NetworkManager.isNetworkAvailable()) {
            Log.w(TAG, "⚠️ Cannot sync to Firestore: No network connection")
            return
        }

        coroutineScope.launch {
            try {
                Log.d(TAG, "🔄 Starting Firestore upload...")

                val userRepo = RepositoryProvider.getUserProfileRepository()
                val gameRepo = RepositoryProvider.getGameResultRepository()
                val achievementRepo = RepositoryProvider.getAchievementRepository()
                val authManager = com.google.firebase.auth.FirebaseAuth.getInstance()
                val userId = authManager.currentUser?.uid ?: return@launch

                // Upload user profile
                val userProfile = userRepo.getUserProfile(userId)
                userProfile?.let {
                    firestoreManager.uploadUserProfile(it)
                    Log.d(TAG, "✅ User profile uploaded to Firestore")
                }

                // Upload unsynced achievements
                val achievements = achievementRepo.getUnsyncedAchievementsForUser(userId)
                if (achievements.isNotEmpty()) {
                    firestoreManager.uploadAchievements(achievements)
                    achievements.forEach { achievementRepo.markAsSynced(it.achievementId) }
                    Log.d(TAG, "✅ ${achievements.size} achievements uploaded to Firestore")
                }

                // Upload recent game results
                val recentGames = gameRepo.getRecentGames(userId, 100)
                if (recentGames.isNotEmpty()) {
                    firestoreManager.uploadGameResults(recentGames)
                    Log.d(TAG, "✅ ${recentGames.size} game results uploaded to Firestore")
                }

                Log.d(TAG, "✅ Firestore upload completed!")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Firestore upload error: ${e.message}", e)
            }
        }
    }

    /**
     * Sync FROM Firestore (download cloud data and merge with local)
     * Call this on app start
     */
    fun syncFromFirestore() {
        if (!NetworkManager.isNetworkAvailable()) {
            Log.w(TAG, "⚠️ Cannot sync from Firestore: No network connection")
            return
        }

        coroutineScope.launch {
            try {
                Log.d(TAG, "🔄 Starting Firestore download...")

                val userRepo = RepositoryProvider.getUserProfileRepository()
                val gameRepo = RepositoryProvider.getGameResultRepository()
                val achievementRepo = RepositoryProvider.getAchievementRepository()
                val authManager = com.google.firebase.auth.FirebaseAuth.getInstance()
                val userId = authManager.currentUser?.uid ?: return@launch

                // Download and merge user profile
                val cloudProfileResult = firestoreManager.downloadUserProfile()
                cloudProfileResult.getOrNull()?.let { cloudProfile ->
                    val localProfile = userRepo.getUserProfile(userId)

                    if (localProfile != null) {
                        // Merge: keep the best values
                        val mergedProfile = localProfile.copy(
                            totalXP = maxOf(localProfile.totalXP, cloudProfile.totalXP),
                            level = maxOf(localProfile.level, cloudProfile.level),
                            totalGamesPlayed = maxOf(localProfile.totalGamesPlayed, cloudProfile.totalGamesPlayed),
                            gamesWon = maxOf(localProfile.gamesWon, cloudProfile.gamesWon),
                            currentStreak = maxOf(localProfile.currentStreak, cloudProfile.currentStreak),
                            bestStreak = maxOf(localProfile.bestStreak, cloudProfile.bestStreak),
                            lastPlayDate = maxOf(localProfile.lastPlayDate, cloudProfile.lastPlayDate),
                            isSynced = true
                        )
                        userRepo.saveUserProfile(mergedProfile)
                        Log.d(TAG, "✅ User profile merged and saved")
                    } else {
                        // No local profile, save cloud profile
                        userRepo.saveUserProfile(cloudProfile)
                        Log.d(TAG, "✅ User profile downloaded from Firestore")
                    }
                }

                // Download achievements
                val cloudAchievementsResult = firestoreManager.downloadAchievements()
                cloudAchievementsResult.getOrNull()?.let { cloudAchievements ->
                    cloudAchievements.forEach { cloudAchievement ->
                        val localAchievement = achievementRepo.getAchievementByType(userId, cloudAchievement.achievementType)

                        if (localAchievement != null) {
                            // Merge: if either is unlocked, mark as unlocked
                            if (cloudAchievement.isUnlocked && !localAchievement.isUnlocked) {
                                achievementRepo.unlockAchievement(localAchievement.achievementId)
                            }
                            // Update progress to max
                            val maxProgress = maxOf(localAchievement.progress, cloudAchievement.progress)
                            if (maxProgress > localAchievement.progress) {
                                achievementRepo.updateProgress(localAchievement.achievementId, maxProgress)
                            }
                        } else {
                            // No local achievement, save cloud achievement
                            achievementRepo.saveAchievement(cloudAchievement)
                        }
                    }
                    Log.d(TAG, "✅ ${cloudAchievements.size} achievements synced from Firestore")
                }

                // Download game results
                val cloudResultsResult = firestoreManager.downloadGameResults(100)
                cloudResultsResult.getOrNull()?.let { cloudResults ->
                    cloudResults.forEach { cloudResult ->
                        // Only save if it doesn't exist locally
                        val allLocalGames = gameRepo.getAllGamesForUser(userId)
                        if (!allLocalGames.any { it.gameId == cloudResult.gameId }) {
                            gameRepo.saveGameResult(cloudResult)
                        }
                    }
                    Log.d(TAG, "✅ ${cloudResults.size} game results synced from Firestore")
                }

                Log.d(TAG, "✅ Firestore download completed!")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Firestore download error: ${e.message}", e)
            }
        }
    }

    /**
     * Perform full two-way sync with Firestore
     */
    fun performFullFirestoreSync() {
        syncFromFirestore() // Download first

        // Upload after a short delay to ensure download completes
        coroutineScope.launch {
            kotlinx.coroutines.delay(2000) // Wait 2 seconds
            syncToFirestore()
        }
    }

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

                // FIXED: Remove context parameter
                val userRepo = RepositoryProvider.getUserProfileRepository()
                val gameRepo = RepositoryProvider.getGameResultRepository()
                val achievementRepo = RepositoryProvider.getAchievementRepository()

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
        // FIXED: Remove context parameter
        val userRepo = RepositoryProvider.getUserProfileRepository()
        val gameRepo = RepositoryProvider.getGameResultRepository()
        val achievementRepo = RepositoryProvider.getAchievementRepository()

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