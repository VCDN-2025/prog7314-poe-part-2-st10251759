package vcmsa.projects.prog7314.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import vcmsa.projects.prog7314.MainActivity
import vcmsa.projects.prog7314.R

/*
    Code Attribution for: Push Notifications
    ===================================================
    Firebase, 2019. Firebase Cloud Messaging | Firebase (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/cloud-messaging>
    [Accessed 18 November 2025]
*/

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "memory_match_notifications"
        private const val CHANNEL_NAME = "Memory Match Madness"
        private const val CHANNEL_DESCRIPTION = "Notifications for game updates and reminders"
    }

    /**
     * Called when a new FCM token is generated
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")

        // Save token to SharedPreferences
        saveFCMToken(token)

        // TODO: Send token to your server if needed
        // sendTokenToServer(token)
    }

    /**
     * Called when a message is received
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Check if message contains data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleDataMessage(message.data)
        }

        // Check if message contains notification payload
        message.notification?.let {
            Log.d(TAG, "Message notification: ${it.title} - ${it.body}")
            showNotification(it.title, it.body)
        }
    }

    /**
     * Handle data messages (for custom logic)
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val notificationType = data["type"]
        val title = data["title"] ?: "Memory Match Madness"
        val body = data["body"] ?: "You have a new notification"

        when (notificationType) {
            "daily_streak" -> {
                Log.d(TAG, "Daily streak reminder received")
                showNotification(title, body, notificationType)
            }
            "achievement" -> {
                Log.d(TAG, "Achievement notification received")
                showNotification(title, body, notificationType)
            }
            "level_unlock" -> {
                Log.d(TAG, "Level unlock notification received")
                showNotification(title, body, notificationType)
            }
            "multiplayer_invite" -> {
                Log.d(TAG, "Multiplayer invite received")
                showNotification(title, body, notificationType)
            }
            else -> {
                showNotification(title, body)
            }
        }
    }

    /**
     * Display notification to user
     */
    private fun showNotification(
        title: String?,
        message: String?,
        type: String? = null
    ) {
        createNotificationChannel()

        // Intent to open app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
            .setContentTitle(title ?: "Memory Match Madness")
            .setContentText(message ?: "You have a new notification")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * Create notification channel (required for Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Save FCM token to SharedPreferences
     */
    private fun saveFCMToken(token: String) {
        val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        Log.d(TAG, "FCM token saved to SharedPreferences")
    }

    /**
     * Get saved FCM token
     */
    fun getFCMToken(context: Context): String? {
        val prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        return prefs.getString("fcm_token", null)
    }
}