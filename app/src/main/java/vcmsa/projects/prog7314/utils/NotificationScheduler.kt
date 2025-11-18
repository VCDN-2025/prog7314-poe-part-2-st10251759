package vcmsa.projects.prog7314.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import vcmsa.projects.prog7314.workers.DailyStreakWorker
import java.util.concurrent.TimeUnit

/*
    Code Attribution for: Push Notifications
    ===================================================
    Firebase, 2019. Firebase Cloud Messaging | Firebase (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/cloud-messaging>
    [Accessed 18 November 2025]
*/


/**
 * Utility object for scheduling background tasks using Android WorkManager.
 * Primarily handles scheduling the daily streak check that runs at 8 PM each day.
 * WorkManager ensures the task runs even if the app is closed or the device restarts.
 */
object NotificationScheduler {

    private const val TAG = "NotificationScheduler"

    // Unique identifier for the daily streak work task
    private const val DAILY_STREAK_WORK_NAME = "daily_streak_check"

    /**
     * Schedules a background worker that checks daily streaks once every 24 hours.
     * The worker runs at 8 PM each day to remind users to maintain their streak.
     * Uses WorkManager which is battery-efficient and survives app restarts.
     *
     * The task will:
     * - Check if the user played today
     * - Send streak reminder if they haven't played yet
     * - Send comeback reminder if they haven't played in several days
     */
    fun scheduleDailyStreakCheck(context: Context) {
        try {
            // Set conditions that must be met before the task runs
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true) // Only run if battery is not critically low
                .build()

            // Create a periodic work request that repeats every 24 hours
            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyStreakWorker>(
                24, TimeUnit.HOURS // Repeat interval
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            // Schedule the work, replacing any existing schedule with the same name
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                DAILY_STREAK_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already scheduled
                dailyWorkRequest
            )

            Log.d(TAG, "✅ Daily streak check scheduled (runs every 24 hours)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to schedule daily streak check: ${e.message}", e)
        }
    }

    /**
     * Calculates how long to wait before the first execution.
     * Schedules the task to run at 8 PM today, or 8 PM tomorrow if it's already past 8 PM.
     *
     * For example:
     * - If it's 3 PM, the task runs in 5 hours (at 8 PM today)
     * - If it's 9 PM, the task runs in 23 hours (at 8 PM tomorrow)
     *
     * Returns the delay in milliseconds.
     */
    private fun calculateInitialDelay(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()

        // Set calendar to 8 PM (20:00) today
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 20)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)

        var scheduledTime = calendar.timeInMillis

        // If 8 PM already passed today, move to 8 PM tomorrow
        if (scheduledTime <= currentTime) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            scheduledTime = calendar.timeInMillis
        }

        // Return how many milliseconds until the scheduled time
        return scheduledTime - currentTime
    }

    /**
     * Cancels all scheduled notification tasks.
     * Useful when user logs out or disables notifications entirely.
     * The task will not run again until rescheduled.
     */
    fun cancelAllScheduledNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_STREAK_WORK_NAME)
        Log.d(TAG, "🚫 All scheduled notifications cancelled")
    }

    /**
     * Checks if the daily worker is currently scheduled.
     * This is a simplified implementation that assumes the worker is scheduled
     * after calling scheduleDailyStreakCheck().
     *
     * Returns true if the worker should be scheduled, false otherwise.
     * Note: This doesn't actually query WorkManager to avoid blocking operations.
     */
    fun isDailyWorkerScheduled(context: Context): Boolean {
        return try {
            true // Simplified check - assumes scheduled after setup
        } catch (e: Exception) {
            false
        }
    }
}