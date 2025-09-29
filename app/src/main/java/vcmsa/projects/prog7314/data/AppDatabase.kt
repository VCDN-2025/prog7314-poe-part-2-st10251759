package vcmsa.projects.prog7314.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vcmsa.projects.prog7314.data.dao.AchievementDao
import vcmsa.projects.prog7314.data.dao.GameResultDao
import vcmsa.projects.prog7314.data.dao.UserProfileDao
import vcmsa.projects.prog7314.data.entities.AchievementEntity
import vcmsa.projects.prog7314.data.entities.GameResultEntity
import vcmsa.projects.prog7314.data.entities.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        GameResultEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun userProfileDao(): UserProfileDao
    abstract fun gameResultDao(): GameResultDao
    abstract fun achievementDao(): AchievementDao

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
                    .fallbackToDestructiveMigration() // For development - removes this in production
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