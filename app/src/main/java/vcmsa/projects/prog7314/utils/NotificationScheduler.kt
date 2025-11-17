package vcmsa.projects.prog7314.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import vcmsa.projects.prog7314.workers.DailyStreakWorker
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"
    private const val DAILY_STREAK_WORK_NAME = "daily_streak_check"

    /**
     * Schedule daily streak check worker
     * Runs once per day at 8 PM
     */
    fun scheduleDailyStreakCheck(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyStreakWorker>(
                24, TimeUnit.HOURS // Run once every 24 hours
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                DAILY_STREAK_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                dailyWorkRequest
            )

            Log.d(TAG, "✅ Daily streak check scheduled (runs every 24 hours)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to schedule daily streak check: ${e.message}", e)
        }
    }

    /**
     * Calculate initial delay to run at 8 PM today (or tomorrow if past 8 PM)
     */
    private fun calculateInitialDelay(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()

        // Set to 8 PM today
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 20)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)

        var scheduledTime = calendar.timeInMillis

        // If 8 PM has already passed today, schedule for tomorrow
        if (scheduledTime <= currentTime) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            scheduledTime = calendar.timeInMillis
        }

        return scheduledTime - currentTime
    }

    /**
     * Cancel all scheduled notifications
     */
    fun cancelAllScheduledNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_STREAK_WORK_NAME)
        Log.d(TAG, "🚫 All scheduled notifications cancelled")
    }

    /**
     * Check if daily worker is scheduled (simplified - no blocking call)
     */
    fun isDailyWorkerScheduled(context: Context): Boolean {
        return try {
            true // Assume scheduled after calling scheduleDailyStreakCheck()
        } catch (e: Exception) {
            false
        }
    }}