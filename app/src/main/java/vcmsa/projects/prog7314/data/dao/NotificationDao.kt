package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import vcmsa.projects.prog7314.data.entities.NotificationEntity


/*
    Code Attribution for: Creating Data Access Objects
    ===================================================
    Android Developers, 2019. Accessing data using Room DAOs | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/accessing-data>
    [Accessed 17 November 2025].

    Code Attribution for: Push Notifications
    ===================================================
    Firebase, 2019. Firebase Cloud Messaging | Firebase (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/cloud-messaging>
    [Accessed 18 November 2025].
*/


@Dao
interface NotificationDao {

    // Get all notifications for a user, ordered from newest to oldest.
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllNotifications(userId: String): List<NotificationEntity>

    // Get only unread notifications for a user.
    // Useful for displaying badges or alerts.
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY timestamp DESC")
    suspend fun getUnreadNotifications(userId: String): List<NotificationEntity>

    // Get notifications filtered by category.
    // Category may be something like "system", "game", "achievement", etc.
    @Query("SELECT * FROM notifications WHERE userId = :userId AND category = :category ORDER BY timestamp DESC")
    suspend fun getNotificationsByCategory(userId: String, category: String): List<NotificationEntity>

    // Count how many unread notifications a user has.
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadCount(userId: String): Int

    // Count the number of notifications received today.
    // Converts the stored timestamp into a date for comparison.
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND DATE(timestamp/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayCount(userId: String): Int

    // Count all notifications belonging to a user.
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId")
    suspend fun getTotalCount(userId: String): Int

    // Insert a new notification. Replaces an existing one if IDs match.
    // Returns the inserted row ID.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    // Update an existing notification entry.
    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    // Mark a specific notification as read.
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long)

    // Mark all of a user's notifications as read.
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    // Delete a single notification by its ID.
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: Long)

    // Delete all notifications for a user.
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllNotifications(userId: String)

    // Get all notifications that still need to be synced to the server.
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedNotifications(userId: String): List<NotificationEntity>

    // Mark a single notification as synced.
    @Query("UPDATE notifications SET isSynced = 1 WHERE id = :notificationId")
    suspend fun markAsSynced(notificationId: Long)
}
