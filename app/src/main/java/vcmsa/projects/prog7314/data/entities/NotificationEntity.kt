package vcmsa.projects.prog7314.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String,

    val type: String, // "ACHIEVEMENT", "LEVEL_UNLOCK", "HIGH_SCORE", "DAILY_STREAK", "THEME_UNLOCK", "SYSTEM"

    val category: String, // "GAME", "SOCIAL", "SYSTEM"

    val title: String,

    val message: String,

    val iconType: String, // "trophy", "level", "star", "fire", "theme", "system"

    val timestamp: Long = System.currentTimeMillis(),

    val isRead: Boolean = false,

    val actionData: String? = null, // JSON data for action (e.g., achievement ID, level number)

    val isSynced: Boolean = false
)