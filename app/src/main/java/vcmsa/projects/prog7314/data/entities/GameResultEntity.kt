package vcmsa.projects.prog7314.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/*
    Code Attribution for: Room DB Entities
    ===================================================
    Android Developers, 2020. Defining data using Room entities | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/defining-data>
    [Accessed 18 November 2025].
*/


@Entity(
    tableName = "game_results",
    // Link each game result to a specific user.
    // Deleting the user will remove all their game results.
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Index on userId to make queries by user faster.
    indices = [Index(value = ["userId"])]
)
data class GameResultEntity(

    // Unique identifier for this game result.
    @PrimaryKey
    val gameId: String,

    // The user who played this game.
    val userId: String,

    // Type of game played, e.g., ARCADE, ADVENTURE, MULTIPLAYER.
    val gameMode: String,

    // Theme used in the game, like "Animals" or "Fruits".
    val theme: String,

    // Grid layout used for the game, e.g., "3x2", "4x3".
    val gridSize: String,

    // Difficulty level of the game.
    val difficulty: String, // BEGINNER, INTERMEDIATE, EXPERT

    // Score achieved in the game.
    val score: Int,

    // Time taken to complete the game, in seconds.
    val timeTaken: Int,

    // Number of moves made during the game.
    val moves: Int,

    // Accuracy as a percentage (0-100) for the game.
    val accuracy: Float,

    // Timestamp when the game was completed.
    val completedAt: Long = System.currentTimeMillis(),

    // Indicates if the player won the game.
    val isWin: Boolean = true,

    // Indicates whether this result has been synced to the server.
    val isSynced: Boolean = false
)
