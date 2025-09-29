package vcmsa.projects.prog7314.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "achievements",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class AchievementEntity(
    @PrimaryKey
    val achievementId: String,
    val userId: String,
    val achievementType: String, // "FIRST_WIN", "SPEED_DEMON", "MEMORY_GURU", "STREAK_MASTER", etc.
    val name: String,
    val description: String,
    val iconName: String, // Reference to drawable resource
    val unlockedAt: Long = System.currentTimeMillis(),
    val progress: Int = 100, // 0-100, useful for achievements with multiple steps
    val isUnlocked: Boolean = true,
    val isSynced: Boolean = false
)