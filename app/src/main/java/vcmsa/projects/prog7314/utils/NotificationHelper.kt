package vcmsa.projects.prog7314.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
/*
    Code Attribution for: Push Notifications
    ===================================================
    Firebase, 2019. Firebase Cloud Messaging | Firebase (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/cloud-messaging>
    [Accessed 18 November 2025]
*/


/**
 * Utility object for managing notification permissions and preferences.
 * Handles Android 13+ notification permissions, Firebase Cloud Messaging tokens,
 * topic subscriptions, and user notification settings.
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"

    // Request code used when asking for notification permission
    private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    // SharedPreferences configuration
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
    private const val KEY_ACHIEVEMENT_NOTIFICATIONS = "achievement_notifications"
    private const val KEY_MULTIPLAYER_NOTIFICATIONS = "multiplayer_notifications"

    /**
     * Requests permission to show notifications on Android 13 and above.
     * On Android 13+, apps must explicitly ask users for notification permission.
     * On older versions, notifications are automatically allowed.
     * Shows a system dialog that the user can accept or deny.
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted - show permission request dialog
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
                Log.d(TAG, "Requesting notification permission")
            } else {
                Log.d(TAG, "Notification permission already granted")
            }
        } else {
            // No permission needed on Android 12 and below
            Log.d(TAG, "Notification permission not required for this Android version")
        }
    }

    /**
     * Checks if the app has permission to show notifications.
     * Returns true if permission is granted, or if running on Android 12 or below.
     * Returns false if permission was denied on Android 13+.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No permission required on older Android versions
        }
    }

    /**
     * Retrieves the Firebase Cloud Messaging token for this device.
     * The FCM token uniquely identifies this device for push notifications.
     * This token is needed to send targeted notifications to specific users.
     * Returns null if token retrieval fails.
     */
    suspend fun getFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token retrieved: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token: ${e.message}", e)
            null
        }
    }

    /**
     * Subscribes this device to a Firebase Cloud Messaging topic.
     * Topics allow sending notifications to groups of users at once.
     * For example, subscribing to "all_users" would receive broadcasts sent to that topic.
     */
    suspend fun subscribeToTopic(topic: String) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Log.d(TAG, "Subscribed to topic: $topic")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to subscribe to topic $topic: ${e.message}", e)
        }
    }

    /**
     * Unsubscribes this device from a Firebase Cloud Messaging topic.
     * After unsubscribing, the device will no longer receive notifications sent to that topic.
     * Useful when users opt out of certain notification categories.
     */
    suspend fun unsubscribeFromTopic(topic: String) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Unsubscribed from topic: $topic")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unsubscribe from topic $topic: ${e.message}", e)
        }
    }

    // ===== NOTIFICATION PREFERENCES =====

    /**
     * Checks if the user has enabled notifications in app settings.
     * This is the master switch for all notifications.
     * Even if system permission is granted, notifications won't show if this is false.
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    /**
     * Enables or disables all notifications in the app.
     * This is the master control that affects all notification types.
     * When disabled, no notifications will be shown regardless of other settings.
     */
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        Log.d(TAG, "Notifications ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Checks if daily reminder notifications are enabled.
     * Daily reminders encourage users to maintain their play streak.
     * Includes streak notifications and comeback reminders.
     */
    fun areDailyRemindersEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, true)
    }

    /**
     * Enables or disables daily reminder notifications.
     * Users can turn this off if they find daily reminders annoying.
     */
    fun setDailyRemindersEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled).apply()
        Log.d(TAG, "Daily reminders ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Checks if achievement unlock notifications are enabled.
     * Achievement notifications celebrate when users unlock new achievements.
     * These are typically high-priority notifications that users want to see.
     */
    fun areAchievementNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, true)
    }

    /**
     * Enables or disables achievement unlock notifications.
     * Users can disable these if they prefer not to be notified about achievements.
     */
    fun setAchievementNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, enabled).apply()
        Log.d(TAG, "Achievement notifications ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Checks if multiplayer game notifications are enabled.
     * Multiplayer notifications alert users about game invites, challenges, and results.
     */
    fun areMultiplayerNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MULTIPLAYER_NOTIFICATIONS, true)
    }

    /**
     * Enables or disables multiplayer game notifications.
     * Users who don't play multiplayer mode might want to disable these.
     */
    fun setMultiplayerNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MULTIPLAYER_NOTIFICATIONS, enabled).apply()
        Log.d(TAG, "Multiplayer notifications ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Sets up default notification preferences on first app launch.
     * All notification types are enabled by default to give users the full experience.
     * Users can later disable specific types in the settings screen.
     * Only runs once - subsequent launches won't override user preferences.
     */
    fun initializeNotificationSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_NOTIFICATIONS_ENABLED)) {
            // First time launching the app - set default preferences
            prefs.edit().apply {
                putBoolean(KEY_NOTIFICATIONS_ENABLED, true)
                putBoolean(KEY_DAILY_REMINDER_ENABLED, true)
                putBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, true)
                putBoolean(KEY_MULTIPLAYER_NOTIFICATIONS, true)
                apply()
            }
            Log.d(TAG, "Notification settings initialized with defaults")
        }
    }
}