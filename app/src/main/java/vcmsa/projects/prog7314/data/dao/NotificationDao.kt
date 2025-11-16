package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import vcmsa.projects.prog7314.data.entities.NotificationEntity

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllNotifications(userId: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY timestamp DESC")
    suspend fun getUnreadNotifications(userId: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND category = :category ORDER BY timestamp DESC")
    suspend fun getNotificationsByCategory(userId: String, category: String): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND DATE(timestamp/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId")
    suspend fun getTotalCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: Long)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllNotifications(userId: String)

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedNotifications(userId: String): List<NotificationEntity>

    @Query("UPDATE notifications SET isSynced = 1 WHERE id = :notificationId")
    suspend fun markAsSynced(notificationId: Long)
}