package vcmsa.projects.prog7314.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vcmsa.projects.prog7314.data.dao.*
import vcmsa.projects.prog7314.data.entities.*

@Database(
    entities = [
        UserProfileEntity::class,
        GameResultEntity::class,
        AchievementEntity::class,
        LevelProgressEntity::class,
        ArcadeSessionEntity::class,
        NotificationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Existing DAOs
    abstract fun userProfileDao(): UserProfileDao
    abstract fun gameResultDao(): GameResultDao
    abstract fun achievementDao(): AchievementDao
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun arcadeSessionDao(): ArcadeSessionDao

   // Notification DAO
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "memory_match_madness_db"

        /**
         * Get database instance (Singleton pattern)
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For dev - use proper migration in production
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Clear database instance (for testing)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}