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

/*
    Code Attribution for: Push Notifications
    ===================================================
    Firebase, 2019. Firebase Cloud Messaging | Firebase (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/cloud-messaging>
    [Accessed 18 November 2025].
*/


/**
 * Utility object for managing local push notifications in the app.
 * Handles showing notifications for achievements, level unlocks, high scores, and daily streaks.
 * Includes duplicate prevention and database storage of all notifications.
 */
object LocalNotificationManager {

    private const val TAG = "LocalNotificationManager"

    // Notification channel configuration for Android 8.0 and above
    private const val CHANNEL_ID = "game_events"
    private const val CHANNEL_NAME = "Game Events"
    private const val CHANNEL_DESCRIPTION = "Notifications for achievements, level unlocks, and records"

    /**
     * Sets up the notification system for the app.
     * Should be called when the app starts, typically in Application.onCreate().
     */
    fun initialize(context: Context) {
        createNotificationChannel(context)
    }

    /**
     * Creates a notification channel for Android 8.0 and above.
     * Channels are required on newer Android versions to organize and control notifications.
     * On older versions, this method does nothing.
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
     * Displays a notification when the player unlocks a new achievement.
     * Only shows if achievement notifications are enabled in user settings.
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
     * Displays a notification when a new level becomes available to play.
     * Only shows if general notifications are enabled in user settings.
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
     * Displays a notification when the player beats their previous best score.
     * Shows the improvement amount and the new record score.
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
     * Displays a reminder notification to encourage the player to maintain their daily streak.
     * Only shows if daily reminder notifications are enabled in user settings.
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
     * Displays a notification when the player's daily streak is broken.
     * Encourages them to start a new streak.
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
     * Displays a reminder notification for players who haven't played in several days.
     * Encourages them to return to the game.
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
     * Displays a congratulatory notification when the player completes their first level.
     * This is a special one-time milestone notification.
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
     * Displays a notification when a new visual theme is unlocked for the game.
     * Encourages the player to try out the new theme.
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
     * Core method that builds and displays a notification.
     * Includes duplicate prevention to avoid showing the same notification multiple times.
     * Saves each notification to the database for the notification history screen.
     *
     * Duplicate prevention logic:
     * 1. Checks if an identical notification was shown in the last 24 hours
     * 2. Prevents flooding by limiting similar notification types to 3 per day
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
        // Check if the app has permission to show notifications
        if (!NotificationHelper.hasNotificationPermission(context)) {
            Log.d(TAG, "No notification permission")
            return
        }

        var shouldShowPopup = true

        // Handle database storage and duplicate checking
        val userId = AuthManager.getCurrentUser()?.uid
        if (userId != null) {
            runBlocking {
                try {
                    val notificationRepo = RepositoryProvider.getNotificationRepository()

                    // Get all existing notifications for this user
                    val existingNotifications = notificationRepo.getAllNotifications(userId)
                    val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

                    // Check if this exact notification was already sent in the last 24 hours
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

                    // Count how many similar notifications were sent today to prevent flooding
                    val recentSimilarCount = existingNotifications.count { notif ->
                        notif.type == type && notif.timestamp > oneDayAgo
                    }

                    if (recentSimilarCount >= 3) {
                        Log.d(TAG, "⏭️ Skipping notification to prevent flooding: $title (already sent $recentSimilarCount similar notifications today)")
                        shouldShowPopup = false
                        return@runBlocking
                    }

                    // Save this notification to the database
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

        // Exit if duplicate was detected
        if (!shouldShowPopup) {
            return
        }

        // Ensure notification channel exists
        createNotificationChannel(context)

        // Create intent that opens the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            type.hashCode(), // Use type as unique identifier
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Select the appropriate icon based on notification type
        val iconRes = when (iconType) {
            "trophy" -> R.drawable.ic_launcher_foreground
            "level" -> R.drawable.ic_launcher_foreground
            "star" -> R.drawable.ic_launcher_foreground
            "fire" -> R.drawable.ic_launcher_foreground
            "theme" -> R.drawable.ic_launcher_foreground
            else -> R.drawable.ic_launcher_foreground
        }

        // Build the notification with all properties
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true) // Notification disappears when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Expandable notification

        // Display the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Generate unique ID using type and timestamp to avoid replacing previous notifications
        val notificationId = (type.hashCode() + System.currentTimeMillis()).toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "🔔 Notification displayed: $title")
    }

    /**
     * Records the current time as the last time the user played.
     * This is used for tracking daily streaks and determining when to show comeback reminders.
     * Updates are synced to Firestore through the UserProfileRepository.
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
     * Retrieves the timestamp of when the user last played the game.
     * Returns 0 if no play date is recorded or if the user is not logged in.
     * Used for calculating streaks and determining if comeback reminders should be shown.
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
     * Determines if the player's daily streak should continue.
     * Returns true if they played within the last 48 hours.
     * The 48-hour window gives players some flexibility in maintaining their streak.
     */
    fun shouldContinueStreak(context: Context): Boolean {
        val lastPlayDate = getLastPlayDate(context)
        if (lastPlayDate == 0L) return false

        val currentTime = System.currentTimeMillis()
        val hoursSinceLastPlay = (currentTime - lastPlayDate) / (1000 * 60 * 60)

        return hoursSinceLastPlay < 48
    }

    /**
     * Determines if a comeback reminder notification should be shown.
     * Returns true if the player hasn't played in 3 or more days.
     * Used by the notification scheduler to re-engage inactive players.
     */
    fun shouldShowComebackReminder(context: Context): Boolean {
        val lastPlayDate = getLastPlayDate(context)
        if (lastPlayDate == 0L) return false

        val currentTime = System.currentTimeMillis()
        val daysSinceLastPlay = (currentTime - lastPlayDate) / (1000 * 60 * 60 * 24)

        return daysSinceLastPlay >= 3
    }
}