package vcmsa.projects.prog7314.data.repository

import android.content.Context
import vcmsa.projects.prog7314.data.AppDatabase

object RepositoryProvider {

    @Volatile
    private var userProfileRepository: UserProfileRepository? = null

    @Volatile
    private var gameResultRepository: GameResultRepository? = null

    @Volatile
    private var achievementRepository: AchievementRepository? = null

    /**
     * Get UserProfileRepository instance
     */
    fun getUserProfileRepository(context: Context): UserProfileRepository {
        return userProfileRepository ?: synchronized(this) {
            val instance = UserProfileRepository(
                AppDatabase.getDatabase(context).userProfileDao()
            )
            userProfileRepository = instance
            instance
        }
    }

    /**
     * Get GameResultRepository instance
     */
    fun getGameResultRepository(context: Context): GameResultRepository {
        return gameResultRepository ?: synchronized(this) {
            val instance = GameResultRepository(
                AppDatabase.getDatabase(context).gameResultDao()
            )
            gameResultRepository = instance
            instance
        }
    }

    /**
     * Get AchievementRepository instance
     */
    fun getAchievementRepository(context: Context): AchievementRepository {
        return achievementRepository ?: synchronized(this) {
            val instance = AchievementRepository(
                AppDatabase.getDatabase(context).achievementDao()
            )
            achievementRepository = instance
            instance
        }
    }

    /**
     * Clear all repository instances (for testing/logout)
     */
    fun clearInstances() {
        userProfileRepository = null
        gameResultRepository = null
        achievementRepository = null
    }
}