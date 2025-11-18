package vcmsa.projects.prog7314.data.repository

import android.util.Log
import vcmsa.projects.prog7314.data.dao.NotificationDao
import vcmsa.projects.prog7314.data.entities.NotificationEntity

/*
    Code Attribution for: Repositories
    ===================================================
    Android Developers, 2025. Data layer (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/architecture/data-layer>
    [Accessed 18 November 2025].

    Code Attribution for: Push Notifications
    ===================================================
    Firebase, 2019. Firebase Cloud Messaging | Firebase (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/cloud-messaging>
    [Accessed 18 November 2025]
*/

/**
 * Repository responsible for managing user notifications.
 *
 * Provides a layer between the app and the database (NotificationDao) for:
 * - Fetching notifications (all, unread, by category, counts)
 * - Creating new notifications
 * - Updating notifications (mark as read, mark as synced)
 * - Deleting notifications (single or all)
 *
 * Handles exceptions gracefully by logging errors and providing safe fallback values.
 */
class NotificationRepository(private val notificationDao: NotificationDao) {

    private val TAG = "NotificationRepository" // Tag for logging

    /**
     * Fetch all notifications for a specific user.
     *
     * @param userId The ID of the user whose notifications are being fetched
     * @return List of NotificationEntity, empty list if an error occurs
     */
    suspend fun getAllNotifications(userId: String): List<NotificationEntity> {
        return try {
            notificationDao.getAllNotifications(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all notifications: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetch only unread notifications for a specific user.
     *
     * @param userId The ID of the user
     * @return List of unread NotificationEntity, empty list if an error occurs
     */
    suspend fun getUnreadNotifications(userId: String): List<NotificationEntity> {
        return try {
            notificationDao.getUnreadNotifications(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread notifications: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetch notifications filtered by category for a user.
     *
     * @param userId The ID of the user
     * @param category The notification category to filter by
     * @return List of NotificationEntity matching the category, empty list if error occurs
     */
    suspend fun getNotificationsByCategory(userId: String, category: String): List<NotificationEntity> {
        return try {
            notificationDao.getNotificationsByCategory(userId, category)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notifications by category: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get the number of unread notifications for a user.
     *
     * @param userId The ID of the user
     * @return Count of unread notifications, 0 if error occurs
     */
    suspend fun getUnreadCount(userId: String): Int {
        return try {
            notificationDao.getUnreadCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread count: ${e.message}", e)
            0
        }
    }

    /**
     * Get the number of notifications created today for a user.
     *
     * @param userId The ID of the user
     * @return Count of today's notifications, 0 if error occurs
     */
    suspend fun getTodayCount(userId: String): Int {
        return try {
            notificationDao.getTodayCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting today count: ${e.message}", e)
            0
        }
    }

    /**
     * Get the total number of notifications for a user.
     *
     * @param userId The ID of the user
     * @return Total notifications count, 0 if error occurs
     */
    suspend fun getTotalCount(userId: String): Int {
        return try {
            notificationDao.getTotalCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total count: ${e.message}", e)
            0
        }
    }

    /**
     * Create a new notification for a user.
     * Saves the notification to the database with default isRead=false and isSynced=false.
     *
     * @param userId ID of the user
     * @param type Type of notification (e.g., "LEVEL_UP", "ACHIEVEMENT")
     * @param category Category of notification for filtering
     * @param title Title displayed in the notification
     * @param message Message body of the notification
     * @param iconType Type of icon to display
     * @param actionData Optional data for click actions (deep links, extra info)
     * @return Long ID of inserted notification, -1 if error occurs
     */
    suspend fun createNotification(
        userId: String,
        type: String,
        category: String,
        title: String,
        message: String,
        iconType: String,
        actionData: String? = null
    ): Long {
        return try {
            val notification = NotificationEntity(
                userId = userId,
                type = type,
                category = category,
                title = title,
                message = message,
                iconType = iconType,
                actionData = actionData,
                timestamp = System.currentTimeMillis(), // current time in millis
                isRead = false,  // Newly created notifications are unread
                isSynced = false // Marked unsynced initially
            )
            val id = notificationDao.insertNotification(notification)
            Log.d(TAG, "✅ Notification created: $title (ID: $id)")
            id
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating notification: ${e.message}", e)
            -1L
        }
    }

    /**
     * Mark a single notification as read.
     *
     * @param notificationId ID of the notification to mark as read
     */
    suspend fun markAsRead(notificationId: Long) {
        try {
            notificationDao.markAsRead(notificationId)
            Log.d(TAG, "✅ Notification marked as read: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marking notification as read: ${e.message}", e)
        }
    }

    /**
     * Mark all notifications as read for a user.
     *
     * @param userId ID of the user
     */
    suspend fun markAllAsRead(userId: String) {
        try {
            notificationDao.markAllAsRead(userId)
            Log.d(TAG, "✅ All notifications marked as read for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marking all notifications as read: ${e.message}", e)
        }
    }

    /**
     * Delete a single notification from the database.
     *
     * @param notificationId ID of the notification to delete
     */
    suspend fun deleteNotification(notificationId: Long) {
        try {
            notificationDao.deleteNotification(notificationId)
            Log.d(TAG, "✅ Notification deleted: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting notification: ${e.message}", e)
        }
    }

    /**
     * Delete all notifications for a specific user.
     *
     * @param userId ID of the user
     */
    suspend fun deleteAllNotifications(userId: String) {
        try {
            notificationDao.deleteAllNotifications(userId)
            Log.d(TAG, "✅ All notifications deleted for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting all notifications: ${e.message}", e)
        }
    }

    /**
     * Fetch notifications that have not yet been synced to a backend API.
     *
     * @param userId ID of the user
     * @return List of unsynced NotificationEntity, empty if error occurs
     */
    suspend fun getUnsyncedNotifications(userId: String): List<NotificationEntity> {
        return try {
            notificationDao.getUnsyncedNotifications(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced notifications: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Mark a notification as synced after successful API upload.
     *
     * @param notificationId ID of the notification to mark as synced
     */
    suspend fun markAsSynced(notificationId: Long) {
        try {
            notificationDao.markAsSynced(notificationId)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as synced: ${e.message}", e)
        }
    }
}
