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
    tableName = "achievements",
    // This sets up a link between each achievement and a specific user.
    // If the user is deleted, all their achievements will also be deleted.
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Adding an index on userId improves performance when filtering achievements by user.
    indices = [Index(value = ["userId"])]
)
data class AchievementEntity(

    // Each achievement has its own unique ID.
    @PrimaryKey
    val achievementId: String,

    // The user that this achievement belongs to.
    val userId: String,

    // A short identifier for the achievement type.
    // Helps the app recognise and apply logic for different types of achievements.
    val achievementType: String, // e.g. "FIRST_WIN", "SPEED_DEMON", etc.

    // The readable title of the achievement.
    val name: String,

    // A simple description explaining how the achievement was earned.
    val description: String,

    // The name of the icon used for this achievement.
    // This usually refers to a drawable file.
    val iconName: String,

    // Timestamp of when the achievement was unlocked.
    // Defaults to the current time.
    val unlockedAt: Long = System.currentTimeMillis(),

    // Optional progress value for achievements that are unlocked gradually.
    // Always 0-100, and defaults to 100 for fully unlocked achievements.
    val progress: Int = 100,

    // Indicates if the achievement is fully unlocked.
    val isUnlocked: Boolean = true,

    // Marks whether the achievement has been synced to the server.
    val isSynced: Boolean = false
)
