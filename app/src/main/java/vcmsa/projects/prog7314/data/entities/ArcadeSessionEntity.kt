package vcmsa.projects.prog7314.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
    Code Attribution for: Room DB Entities
    ===================================================
    Kotlin, 2025. Enum classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/enum-classes.html>
    [Accessed 18 November 2025].
*/


@Entity(
    tableName = "arcade_sessions",
    // Link each arcade session to a user. Deleting the user will delete all their sessions.
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Index on userId to improve query performance when filtering sessions by user.
    indices = [Index(value = ["userId"])]
)
data class ArcadeSessionEntity(

    // Unique ID for each session.
    @PrimaryKey
    val sessionId: String, // UUID

    // The user who played this session.
    val userId: String,

    // Theme name used in this session.
    val theme: String,

    // Grid size used for the session, e.g., "4x3".
    val gridSize: String,

    // Difficulty level of the session.
    // Can be "BEGINNER", "INTERMEDIATE", "HARD", "EXPERT".
    val difficulty: String,

    // Score achieved in this session.
    val score: Int,

    // Time taken to complete the session in seconds.
    val timeTaken: Int,

    // Number of moves made in the session.
    val moves: Int,

    // Optional bonus points earned during the session.
    val bonus: Int = 0,

    // Stars earned based on performance, typically 0-3.
    val stars: Int = 0,

    // Timestamp of when the session was completed.
    val completedAt: Long = System.currentTimeMillis(),

    // Marks whether the session has been synced with the server.
    val isSynced: Boolean = false
)
