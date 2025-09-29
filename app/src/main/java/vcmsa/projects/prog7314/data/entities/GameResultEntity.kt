package vcmsa.projects.prog7314.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "game_results",
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
data class GameResultEntity(
    @PrimaryKey
    val gameId: String,
    val userId: String,
    val gameMode: String, // "ARCADE", "ADVENTURE", "MULTIPLAYER"
    val theme: String, // "Animals", "Pokemon", "Fruits", etc.
    val gridSize: String, // "3x2", "4x3", "5x4", etc.
    val difficulty: String, // "BEGINNER", "INTERMEDIATE", "EXPERT"
    val score: Int,
    val timeTaken: Int, // in seconds
    val moves: Int,
    val accuracy: Float, // percentage 0-100
    val completedAt: Long = System.currentTimeMillis(),
    val isWin: Boolean = true,
    val isSynced: Boolean = false
)