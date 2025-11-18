package vcmsa.projects.prog7314.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.LocalNotificationManager

/**
 * Background worker that checks daily play streaks and sends appropriate notifications.
 * Scheduled to run once per day via WorkManager to:
 * - Remind users to maintain their streak if they haven't played yet today
 * - Notify users when they've lost their streak
 * - Send comeback reminders to inactive users
 *
 * Extends CoroutineWorker to allow suspending functions and async operations.
 * Runs on a background thread so it doesn't block the main UI.
 */
class DailyStreakWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "DailyStreakWorker"

    /**
     * Main work method that executes when the worker runs.
     * Checks how long it's been since the user last played and sends appropriate notifications.
     *
     * Returns Result.success() if work completes successfully.
     * Returns Result.failure() if an error occurs (WorkManager will retry).
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔥 Daily Streak Worker started")

            // Check if a user is logged in
            val userId = AuthManager.getCurrentUser()?.uid
            if (userId == null) {
                Log.d(TAG, "No user logged in, skipping streak check")
                return@withContext Result.success()
            }

            // Load the user's profile to get their current streak
            val userProfileRepo = RepositoryProvider.getUserProfileRepository()
            val profile = userProfileRepo.getUserProfile(userId)

            if (profile == null) {
                Log.d(TAG, "No user profile found")
                return@withContext Result.success()
            }

            // Calculate how long it's been since the user last played
            val lastPlayDate = LocalNotificationManager.getLastPlayDate(applicationContext)
            val currentTime = System.currentTimeMillis()
            val hoursSinceLastPlay = if (lastPlayDate > 0) {
                (currentTime - lastPlayDate) / (1000 * 60 * 60)
            } else {
                0
            }

            Log.d(TAG, "Hours since last play: $hoursSinceLastPlay")

            // Determine which notification to send based on inactivity duration
            when {
                // User hasn't played in 3+ days - they're considered inactive
                hoursSinceLastPlay >= 72 -> {
                    Log.d(TAG, "📧 Sending comeback reminder")
                    LocalNotificationManager.notifyComebackReminder(applicationContext)
                }

                // User hasn't played today but it's only been 24-47 hours
                // Their streak is still active but they need a reminder
                hoursSinceLastPlay in 24..47 && profile.currentStreak > 0 -> {
                    Log.d(TAG, "🔥 Sending daily streak reminder (${profile.currentStreak} days)")
                    LocalNotificationManager.notifyDailyStreak(applicationContext, profile.currentStreak)
                }

                // User hasn't played in 48+ hours - their streak is broken
                // Notify them about the lost streak
                hoursSinceLastPlay >= 48 && profile.currentStreak > 0 -> {
                    Log.d(TAG, "💔 User lost streak of ${profile.currentStreak} days")
                    LocalNotificationManager.notifyStreakLost(applicationContext, profile.currentStreak)

                    // Note: The actual streak reset happens in UserProfileRepository
                    // when the user plays again. We don't reset it here to preserve
                    // the information for the notification.
                }

                else -> {
                    // User played recently - no notification needed
                    Log.d(TAG, "✅ User is active, no notification needed")
                }
            }

            Log.d(TAG, "✅ Daily Streak Worker completed")
            Result.success()

        } catch (e: Exception) {
            // Log the error and return failure so WorkManager can retry
            Log.e(TAG, "❌ Daily Streak Worker error: ${e.message}", e)
            Result.failure()
        }
    }
}