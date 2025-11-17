package vcmsa.projects.prog7314.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.runBlocking
import vcmsa.projects.prog7314.MainActivity
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.repository.RepositoryProvider

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

        showNotification(
            context = context,
            title = title,
            message = message,
            type = "ACHIEVEMENT",
            category = "GAME",
            iconType = "trophy"
        )
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

        showNotification(
            context = context,
            title = title,
            message = message,
            type = "LEVEL_UNLOCK",
            category = "GAME",
            iconType = "level",
            actionData = levelNumber.toString()
        )
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

        showNotification(
            context = context,
            title = title,
            message = message,
            type = "HIGH_SCORE",
            category = "GAME",
            iconType = "star"
        )
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

        showNotification(
            context = context,
            title = title,
            message = message,
            type = "DAILY_STREAK",
            category = "GAME",
            iconType = "fire"
        )
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

        showNotification(
            context = context,
            title = title,
            message = message,
            type = "STREAK_LOST",
            category = "SYSTEM",
            iconType = "fire"
        )
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

        showNotification(
            context = context,
            title = title,
            message = message,
            type = "COMEBACK",
            category = "SYSTEM",
            iconType = "system"
        )
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

        showNotification(
            context = context,
            title = title,
            message = message,
            type = "FIRST_LEVEL",
            category = "GAME",
            iconType = "trophy"
        )
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

        showNotification(
            context = context,
            title = title,
            message = message,
            type = "THEME_UNLOCK",
            category = "GAME",
            iconType = "theme"
        )
        Log.d(TAG, "Theme unlock notification sent: $themeName")
    }

    /**
     * Generic notification display method with database save and duplicate prevention
     * 🔥 FIXED: Enhanced duplicate prevention using both database and time-based checking
     */
    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        type: String,
        category: String,
        iconType: String,
        actionData: String? = null
    ) {
        // Check if user has notification permission
        if (!NotificationHelper.hasNotificationPermission(context)) {
            Log.d(TAG, "No notification permission")
            return
        }

        var shouldShowPopup = true

        val userId = AuthManager.getCurrentUser()?.uid
        if (userId != null) {
            runBlocking {
                try {
                    val notificationRepo = RepositoryProvider.getNotificationRepository()

                    // 🔥 ENHANCED: Check for duplicates in last 24 hours (not just 5 minutes)
                    val existingNotifications = notificationRepo.getAllNotifications(userId)
                    val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

                    val isDuplicate = existingNotifications.any { notif ->
                        notif.type == type &&
                                notif.title == title &&
                                notif.message == message &&
                                notif.timestamp > oneDayAgo
                    }

                    if (isDuplicate) {
                        Log.d(TAG, "⏭️ Skipping duplicate notification: $title (found in last 24 hours)")
                        shouldShowPopup = false
                        return@runBlocking
                    }

                    // 🔥 NEW: Additional duplicate check - count recent similar notifications
                    val recentSimilarCount = existingNotifications.count { notif ->
                        notif.type == type && notif.timestamp > oneDayAgo
                    }

                    if (recentSimilarCount >= 3) {
                        Log.d(TAG, "⏭️ Skipping notification to prevent flooding: $title (already sent $recentSimilarCount similar notifications today)")
                        shouldShowPopup = false
                        return@runBlocking
                    }

                    // Create new notification in database
                    notificationRepo.createNotification(
                        userId = userId,
                        type = type,
                        category = category,
                        title = title,
                        message = message,
                        iconType = iconType,
                        actionData = actionData
                    )

                    Log.d(TAG, "✅ Notification saved to database")

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error saving notification: ${e.message}", e)
                }
            }
        }

        if (!shouldShowPopup) {
            return
        }

        // Create notification channel
        createNotificationChannel(context)

        // Intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            type.hashCode(), // Use type hashCode for unique request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Select appropriate icon
        val iconRes = when (iconType) {
            "trophy" -> R.drawable.ic_launcher_foreground
            "level" -> R.drawable.ic_launcher_foreground
            "star" -> R.drawable.ic_launcher_foreground
            "fire" -> R.drawable.ic_launcher_foreground
            "theme" -> R.drawable.ic_launcher_foreground
            else -> R.drawable.ic_launcher_foreground
        }

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Show notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 🔥 FIXED: Use unique notification ID based on type and timestamp to avoid replacing
        val notificationId = (type.hashCode() + System.currentTimeMillis()).toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "🔔 Notification displayed: $title")
    }

    /**
     * 🔥 NEW: Save last play date using UserProfileRepository (syncs to Firestore)
     */
    fun saveLastPlayDate(context: Context) {
        runBlocking {
            try {
                val userId = AuthManager.getCurrentUser()?.uid
                if (userId != null) {
                    val userProfileRepo = RepositoryProvider.getUserProfileRepository()
                    val success = userProfileRepo.updateDailyStreak(userId)

                    if (success) {
                        Log.d(TAG, "✅ Last play date saved and streak updated via repository")
                    } else {
                        Log.e(TAG, "❌ Failed to save last play date")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving last play date: ${e.message}", e)
            }
        }
    }

    /**
     * 🔥 NEW: Get last play date from UserProfileRepository
     */
    fun getLastPlayDate(context: Context): Long {
        return runBlocking {
            try {
                val userId = AuthManager.getCurrentUser()?.uid
                if (userId != null) {
                    val userProfileRepo = RepositoryProvider.getUserProfileRepository()
                    val profile = userProfileRepo.getUserProfile(userId)
                    profile?.lastPlayDate ?: 0L
                } else {
                    0L
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting last play date: ${e.message}", e)
                0L
            }
        }
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