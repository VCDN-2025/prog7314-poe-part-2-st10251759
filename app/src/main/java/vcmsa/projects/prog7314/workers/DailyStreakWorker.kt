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

class DailyStreakWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "DailyStreakWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔥 Daily Streak Worker started")

            val userId = AuthManager.getCurrentUser()?.uid
            if (userId == null) {
                Log.d(TAG, "No user logged in, skipping streak check")
                return@withContext Result.success()
            }

            val userProfileRepo = RepositoryProvider.getUserProfileRepository()
            val profile = userProfileRepo.getUserProfile(userId)

            if (profile == null) {
                Log.d(TAG, "No user profile found")
                return@withContext Result.success()
            }

            val lastPlayDate = LocalNotificationManager.getLastPlayDate(applicationContext)
            val currentTime = System.currentTimeMillis()
            val hoursSinceLastPlay = if (lastPlayDate > 0) {
                (currentTime - lastPlayDate) / (1000 * 60 * 60)
            } else {
                0
            }

            Log.d(TAG, "Hours since last play: $hoursSinceLastPlay")

            when {
                // User hasn't played in 3+ days - Send comeback reminder
                hoursSinceLastPlay >= 72 -> {
                    Log.d(TAG, "📧 Sending comeback reminder")
                    LocalNotificationManager.notifyComebackReminder(applicationContext)
                }

                // User hasn't played today but still within 48 hours - Send streak reminder
                hoursSinceLastPlay in 24..47 && profile.currentStreak > 0 -> {
                    Log.d(TAG, "🔥 Sending daily streak reminder (${profile.currentStreak} days)")
                    LocalNotificationManager.notifyDailyStreak(applicationContext, profile.currentStreak)
                }

                // User lost their streak (48+ hours)
                hoursSinceLastPlay >= 48 && profile.currentStreak > 0 -> {
                    Log.d(TAG, "💔 User lost streak of ${profile.currentStreak} days")
                    LocalNotificationManager.notifyStreakLost(applicationContext, profile.currentStreak)

                    // Reset current streak to 0 (keep best streak)
                    // The streak will be recalculated when user plays again
                }

                else -> {
                    Log.d(TAG, "✅ User is active, no notification needed")
                }
            }

            Log.d(TAG, "✅ Daily Streak Worker completed")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Daily Streak Worker error: ${e.message}", e)
            Result.failure()
        }
    }
}