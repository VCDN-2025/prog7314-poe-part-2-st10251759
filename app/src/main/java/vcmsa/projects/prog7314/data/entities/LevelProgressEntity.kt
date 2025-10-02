package vcmsa.projects.prog7314.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "level_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "levelNumber"], unique = true)]
)
data class LevelProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val levelNumber: Int, // 1-16
    val stars: Int = 0, // 0-3
    val bestScore: Int = 0,
    val bestTime: Int = 0, // in seconds
    val bestMoves: Int = 0,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val lastPlayed: Long = 0,
    val timesPlayed: Int = 0,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)