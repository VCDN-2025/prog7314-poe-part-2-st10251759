package vcmsa.projects.prog7314.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
    Code Attribution for: Room DB Entities
    ===================================================
    Android Developers, 2020. Defining data using Room entities | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/defining-data>
    [Accessed 18 November 2025].
*/

@Entity(tableName = "user_profile")
data class UserProfileEntity(

    // Unique ID for the user.
    @PrimaryKey
    val userId: String,

    // User's display name.
    val username: String,

    // User's email address.
    val email: String,

    // User's avatar image encoded in Base64 (optional).
    val avatarBase64: String? = null,

    // Total experience points accumulated by the user.
    val totalXP: Int = 0,

    // Current level of the user.
    val level: Int = 1,

    // Total number of games the user has played.
    val totalGamesPlayed: Int = 0,

    // Total number of games the user has won.
    val gamesWon: Int = 0,

    // Current winning streak of the user.
    val currentStreak: Int = 0,

    // Best winning streak achieved by the user.
    val bestStreak: Int = 0,

    // Timestamp of the last time the user played.
    val lastPlayDate: Long = 0L,

    // Average time taken to complete games.
    val averageCompletionTime: Float = 0f,

    // Accuracy percentage achieved by the user.
    val accuracyRate: Float = 0f,

    // Timestamp when the user profile was created.
    val createdAt: Long = System.currentTimeMillis(),

    // Timestamp of the last update to the user profile.
    val lastUpdated: Long = System.currentTimeMillis(),

    // Whether this user profile has been synced with the server.
    val isSynced: Boolean = false
)
