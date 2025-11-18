package vcmsa.projects.prog7314.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
    Code Attribution for: Room DB Entities
    ===================================================
    Android Developers, 2020. Defining data using Room entities | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/defining-data>
    [Accessed 18 November 2025].

    Code Attribution for: Push Notifications
    ===================================================
    Firebase, 2019. Firebase Cloud Messaging | Firebase (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/cloud-messaging>
    [Accessed 18 November 2025].
*/

@Entity(tableName = "notifications")
data class NotificationEntity(

    // Unique ID for the notification, auto-generated.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // The user this notification belongs to.
    val userId: String,

    // Type of notification, e.g., ACHIEVEMENT, LEVEL_UNLOCK, HIGH_SCORE.
    val type: String,

    // Category for grouping notifications, e.g., GAME, SOCIAL, SYSTEM.
    val category: String,

    // Title of the notification displayed to the user.
    val title: String,

    // Message body of the notification.
    val message: String,

    // Icon to display with the notification, e.g., trophy, star, theme.
    val iconType: String,

    // Timestamp when the notification was created.
    val timestamp: Long = System.currentTimeMillis(),

    // Indicates whether the user has read the notification.
    val isRead: Boolean = false,

    // Optional data for the notification action (e.g., achievement ID, level number) in JSON format.
    val actionData: String? = null,

    // Whether this notification has been synced with the server.
    val isSynced: Boolean = false
)
