package vcmsa.projects.prog7314.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import vcmsa.projects.prog7314.MainActivity
import vcmsa.projects.prog7314.R

object LocalNotificationManager {

    private const val TAG = "LocalNotificationManager"
    private const val CHANNEL_ID = "game_events"
    private const val CHANNEL_NAME = "Game Events"
    private const val CHANNEL_DESCRIPTION = "Notifications for achievements, level unlocks, and records"

    /**
     * Initialize notification channel
     */
    fun initialize(context: Context) {
        createNotificationChannel(context)
    }

    /**
     * Create notification channel (Android 8.0+)
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    /**
     * Show Achievement Unlocked notification
     */
    fun notifyAchievementUnlocked(context: Context, achievementTitle: String, achievementDescription: String) {
        if (!NotificationHelper.areAchievementNotificationsEnabled(context)) {
            Log.d(TAG, "Achievement notifications disabled")
            return
        }

        val title = "🏆 Achievement Unlocked!"
        val message = "$achievementTitle: $achievementDescription"

        showNotification(context, title, message, "achievement")
        Log.d(TAG, "Achievement notification sent: $achievementTitle")
    }

    /**
     * Show Level Unlocked notification
     */
    fun notifyLevelUnlocked(context: Context, levelNumber: Int) {
        if (!NotificationHelper.areNotificationsEnabled(context)) {
            Log.d(TAG, "Notifications disabled")
            return
        }

        val title = "🆕 New Level Unlocked!"
        val message = "Level $levelNumber is now available to play!"

        showNotification(context, title, message, "level_unlock")
        Log.d(TAG, "Level unlock notification sent: Level $levelNumber")
    }

    /**
     * Show High Score notification
     */
    fun notifyNewHighScore(context: Context, score: Int, previousBest: Int) {
        if (!NotificationHelper.areNotificationsEnabled(context)) {
            Log.d(TAG, "Notifications disabled")
            return
        }

        val improvement = score - previousBest
        val title = "🌟 New Personal Record!"
        val message = "You beat your best score by $improvement points! New record: $score"

        showNotification(context, title, message, "high_score")
        Log.d(TAG, "High score notification sent: $score (was $previousBest)")
    }

    /**
     * Show Daily Streak Reminder notification
     */
    fun notifyDailyStreak(context: Context, currentStreak: Int) {
        if (!NotificationHelper.areDailyRemindersEnabled(context)) {
            Log.d(TAG, "Daily reminders disabled")
            return
        }

        val title = "🔥 Keep Your Streak Going!"
        val message = "You're on a $currentStreak day streak! Play today to keep it alive!"

        showNotification(context, title, message, "daily_streak")
        Log.d(TAG, "Daily streak notification sent: $currentStreak days")
    }

    /**
     * Show Streak Lost notification
     */
    fun notifyStreakLost(context: Context, lostStreak: Int) {
        if (!NotificationHelper.areDailyRemindersEnabled(context)) {
            Log.d(TAG, "Daily reminders disabled")
            return
        }

        val title = "💔 Streak Lost"
        val message = "You lost your $lostStreak day streak. Start a new one today!"

        showNotification(context, title, message, "streak_lost")
        Log.d(TAG, "Streak lost notification sent: $lostStreak days")
    }

    /**
     * Show Comeback Reminder notification
     */
    fun notifyComebackReminder(context: Context) {
        if (!NotificationHelper.areDailyRemindersEnabled(context)) {
            Log.d(TAG, "Daily reminders disabled")
            return
        }

        val title = "🎮 We Miss You!"
        val message = "It's been a while! Come back and test your memory skills!"

        showNotification(context, title, message, "comeback")
        Log.d(TAG, "Comeback reminder notification sent")
    }

    /**
     * Show First Level Completed notification
     */
    fun notifyFirstLevelCompleted(context: Context) {
        if (!NotificationHelper.areNotificationsEnabled(context)) {
            Log.d(TAG, "Notifications disabled")
            return
        }

        val title = "🎉 First Level Completed!"
        val message = "Congratulations! You've completed your first level. Keep going!"

        showNotification(context, title, message, "first_level")
        Log.d(TAG, "First level completion notification sent")
    }

    /**
     * Show New Theme Unlocked notification
     */
    fun notifyThemeUnlocked(context: Context, themeName: String) {
        if (!NotificationHelper.areNotificationsEnabled(context)) {
            Log.d(TAG, "Notifications disabled")
            return
        }

        val title = "🎨 New Theme Unlocked!"
        val message = "$themeName theme is now available! Try it out!"

        showNotification(context, title, message, "theme_unlock")
        Log.d(TAG, "Theme unlock notification sent: $themeName")
    }

    /**
     * Generic notification display method
     */
    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        type: String
    ) {
        // Check if user has notification permission
        if (!NotificationHelper.hasNotificationPermission(context)) {
            Log.d(TAG, "No notification permission")
            return
        }

        // Intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        Log.d(TAG, "Notification displayed: $title")
    }

    /**
     * Save last play date for streak tracking
     */
    fun saveLastPlayDate(context: Context) {
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        prefs.edit().putLong("last_play_date", currentTime).apply()
        Log.d(TAG, "Last play date saved: $currentTime")
    }

    /**
     * Get last play date
     */
    fun getLastPlayDate(context: Context): Long {
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("last_play_date", 0L)
    }

    /**
     * Calculate if streak should continue (within 48 hours)
     */
    fun shouldContinueStreak(context: Context): Boolean {
        val lastPlayDate = getLastPlayDate(context)
        if (lastPlayDate == 0L) return false

        val currentTime = System.currentTimeMillis()
        val hoursSinceLastPlay = (currentTime - lastPlayDate) / (1000 * 60 * 60)

        return hoursSinceLastPlay < 48
    }

    /**
     * Check if user should get a comeback reminder (not played in 3+ days)
     */
    fun shouldShowComebackReminder(context: Context): Boolean {
        val lastPlayDate = getLastPlayDate(context)
        if (lastPlayDate == 0L) return false

        val currentTime = System.currentTimeMillis()
        val daysSinceLastPlay = (currentTime - lastPlayDate) / (1000 * 60 * 60 * 24)

        return daysSinceLastPlay >= 3
    }
}