package vcmsa.projects.prog7314.data.repository

import android.util.Log
import vcmsa.projects.prog7314.data.dao.NotificationDao
import vcmsa.projects.prog7314.data.entities.NotificationEntity

class NotificationRepository(private val notificationDao: NotificationDao) {

    private val TAG = "NotificationRepository"

    /**
     * Get all notifications for a user
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
     * Get unread notifications
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
     * Get notifications by category
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
     * Get notification counts
     */
    suspend fun getUnreadCount(userId: String): Int {
        return try {
            notificationDao.getUnreadCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread count: ${e.message}", e)
            0
        }
    }

    suspend fun getTodayCount(userId: String): Int {
        return try {
            notificationDao.getTodayCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting today count: ${e.message}", e)
            0
        }
    }

    suspend fun getTotalCount(userId: String): Int {
        return try {
            notificationDao.getTotalCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total count: ${e.message}", e)
            0
        }
    }

    /**
     * Create a new notification
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
                timestamp = System.currentTimeMillis(),
                isRead = false,
                isSynced = false
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
     * Mark notification as read
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
     * Mark all notifications as read
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
     * Delete a notification
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
     * Delete all notifications
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
     * Get unsynced notifications
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
     * Mark notification as synced
     */
    suspend fun markAsSynced(notificationId: Long) {
        try {
            notificationDao.markAsSynced(notificationId)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as synced: ${e.message}", e)
        }
    }
}