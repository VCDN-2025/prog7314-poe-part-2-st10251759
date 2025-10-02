package vcmsa.projects.prog7314.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "arcade_sessions",
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
data class ArcadeSessionEntity(
    @PrimaryKey
    val sessionId: String, // UUID
    val userId: String,
    val theme: String, // Theme name
    val gridSize: String, // e.g., "4x3"
    val difficulty: String, // BEGINNER, INTERMEDIATE, HARD, EXPERT
    val score: Int,
    val timeTaken: Int, // in seconds
    val moves: Int,
    val bonus: Int = 0,
    val stars: Int = 0, // 0-3 based on performance
    val completedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)