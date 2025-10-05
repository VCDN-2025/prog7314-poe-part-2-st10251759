package vcmsa.projects.prog7314.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val email: String,
    val avatarBase64: String? = null, // Changed from avatarUrl to avatarBase64
    val totalXP: Int = 0,
    val level: Int = 1,
    val totalGamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val averageCompletionTime: Float = 0f,
    val accuracyRate: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)