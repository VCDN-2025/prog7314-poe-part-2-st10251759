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

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    // Notification preference keys
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
    private const val KEY_ACHIEVEMENT_NOTIFICATIONS = "achievement_notifications"
    private const val KEY_MULTIPLAYER_NOTIFICATIONS = "multiplayer_notifications"

    /**
     * Request notification permission (Android 13+)
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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
            Log.d(TAG, "Notification permission not required for this Android version")
        }
    }

    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions not required for older versions
        }
    }

    /**
     * Get FCM token asynchronously
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
     * Subscribe to a topic
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
     * Unsubscribe from a topic
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
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    /**
     * Enable or disable all notifications
     */
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        Log.d(TAG, "Notifications ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Check if daily reminders are enabled
     */
    fun areDailyRemindersEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, true)
    }

    /**
     * Enable or disable daily reminders
     */
    fun setDailyRemindersEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled).apply()
        Log.d(TAG, "Daily reminders ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Check if achievement notifications are enabled
     */
    fun areAchievementNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, true)
    }

    /**
     * Enable or disable achievement notifications
     */
    fun setAchievementNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, enabled).apply()
        Log.d(TAG, "Achievement notifications ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Check if multiplayer notifications are enabled
     */
    fun areMultiplayerNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MULTIPLAYER_NOTIFICATIONS, true)
    }

    /**
     * Enable or disable multiplayer notifications
     */
    fun setMultiplayerNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MULTIPLAYER_NOTIFICATIONS, enabled).apply()
        Log.d(TAG, "Multiplayer notifications ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Initialize notification settings on first launch
     */
    fun initializeNotificationSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_NOTIFICATIONS_ENABLED)) {
            // First time setup - enable all by default
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