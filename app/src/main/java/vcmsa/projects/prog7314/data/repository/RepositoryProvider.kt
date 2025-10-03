package vcmsa.projects.prog7314.data.repository

import android.content.Context
import vcmsa.projects.prog7314.data.AppDatabase

/**
 * Centralized repository provider for dependency injection
 */
object RepositoryProvider {

    private var database: AppDatabase? = null

    fun initialize(context: Context) {
        if (database == null) {
            database = AppDatabase.getDatabase(context.applicationContext)
        }
    }

    // ADD THIS NEW METHOD
    fun getRepositories(context: Context): Repositories {
        initialize(context)
        return Repositories(
            userProfileRepository = getUserProfileRepository(),
            gameResultRepository = getGameResultRepository(),
            achievementRepository = getAchievementRepository(),
            levelRepository = getLevelRepository(),
            arcadeRepository = getArcadeRepository(),
            apiRepository = getApiRepository()
        )
    }

    // FIXED: No parameter needed
    fun getUserProfileRepository(): UserProfileRepository {
        return UserProfileRepository(requireDatabase().userProfileDao())
    }

    fun getGameResultRepository(): GameResultRepository {
        return GameResultRepository(requireDatabase().gameResultDao())
    }

    fun getAchievementRepository(): AchievementRepository {
        return AchievementRepository(requireDatabase().achievementDao())
    }

    fun getLevelRepository(): LevelRepository {
        return LevelRepository(requireDatabase().levelProgressDao())
    }

    fun getArcadeRepository(): ArcadeRepository {
        return ArcadeRepository(requireDatabase().arcadeSessionDao())
    }

    fun getApiRepository(): ApiRepository {
        return ApiRepository()
    }

    private fun requireDatabase(): AppDatabase {
        return database ?: throw IllegalStateException(
            "RepositoryProvider must be initialized before use"
        )
    }
    data class Repositories(
        val userProfileRepository: UserProfileRepository,
        val gameResultRepository: GameResultRepository,
        val achievementRepository: AchievementRepository,
        val levelRepository: LevelRepository,
        val arcadeRepository: ArcadeRepository,
        val apiRepository: ApiRepository
    )
}