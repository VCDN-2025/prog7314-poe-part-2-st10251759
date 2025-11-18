package vcmsa.projects.prog7314.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vcmsa.projects.prog7314.data.dao.*
import vcmsa.projects.prog7314.data.entities.*

/*
    Code Attribution for: Creating Data Access Objects
    ===================================================
    Android Developers, 2019. Accessing data using Room DAOs | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/accessing-data>
    [Accessed 17 November 2025].
*/

/**
 * Main Room Database class for the app.
 * Handles local persistence of user profiles, game results, achievements, level progress, arcade sessions, and notifications.
 */
@Database(
    entities = [
        UserProfileEntity::class,      // Stores user profile info (XP, level, streaks, etc.)
        GameResultEntity::class,       // Stores individual game results for tracking performance
        AchievementEntity::class,      // Stores unlocked achievements
        LevelProgressEntity::class,    // Stores progress per level (stars, best score, completion status)
        ArcadeSessionEntity::class,    // Stores arcade game session info
        NotificationEntity::class      // Stores local notifications for the user
    ],
    version = 5,                       // Database version for migrations
    exportSchema = false               // Don't export schema files (useful for small apps / dev)
)
abstract class AppDatabase : RoomDatabase() {

    // ===== Abstract DAO accessors =====
    // These provide access to DAO interfaces for each entity.
    abstract fun userProfileDao(): UserProfileDao          // CRUD for UserProfileEntity
    abstract fun gameResultDao(): GameResultDao            // CRUD for GameResultEntity
    abstract fun achievementDao(): AchievementDao          // CRUD for AchievementEntity
    abstract fun levelProgressDao(): LevelProgressDao      // CRUD for LevelProgressEntity
    abstract fun arcadeSessionDao(): ArcadeSessionDao      // CRUD for ArcadeSessionEntity
    abstract fun notificationDao(): NotificationDao        // CRUD for NotificationEntity

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null       // Volatile ensures thread-safe singleton

        private const val DATABASE_NAME = "memory_match_madness_db" // Database filename

        /**
         * Get database instance (Singleton pattern)
         * Ensures only one instance of Room database exists in the app.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {   // Synchronize to prevent race conditions
                val instance = Room.databaseBuilder(
                    context.applicationContext,        // Use application context to prevent leaks
                    AppDatabase::class.java,          // Reference this database class
                    DATABASE_NAME                     // Provide database name
                )
                    .fallbackToDestructiveMigration() // During dev: drops and recreates DB if schema changes
                    .build()

                INSTANCE = instance                  // Save instance for future access
                instance
            }
        }

        /**
         * Clear database instance (useful for testing or resetting app data)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
