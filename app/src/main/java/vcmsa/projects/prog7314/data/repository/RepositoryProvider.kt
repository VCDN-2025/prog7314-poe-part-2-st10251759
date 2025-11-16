package vcmsa.projects.prog7314.data.repository

import android.content.Context
import vcmsa.projects.prog7314.data.AppDatabase

/**
 * Centralized repository provider for dependency injection
 */
object RepositoryProvider {

    private var database: AppDatabase? = null
    private var appContext: Context? = null  // 🔥 ADD THIS

    fun initialize(context: Context) {
        if (database == null) {
            appContext = context.applicationContext  // 🔥 STORE CONTEXT
            database = AppDatabase.getDatabase(appContext!!)
        }
    }

    fun getRepositories(context: Context): Repositories {
        initialize(context)
        return Repositories(
            userProfileRepository = getUserProfileRepository(),
            gameResultRepository = getGameResultRepository(),
            achievementRepository = getAchievementRepository(),
            levelRepository = getLevelRepository(),
            arcadeRepository = getArcadeRepository(),
            apiRepository = getApiRepository(),
            analyticsRepository = getAnalyticsRepository(),
            notificationRepository = getNotificationRepository()
        )
    }

    fun getUserProfileRepository(): UserProfileRepository {
        return UserProfileRepository(requireDatabase().userProfileDao())
    }

    fun getGameResultRepository(): GameResultRepository {
        return GameResultRepository(requireDatabase().gameResultDao())
    }

    fun getAchievementRepository(): AchievementRepository {
        val db = requireDatabase()
        return AchievementRepository(
            achievementDao = db.achievementDao(),
            gameResultDao = db.gameResultDao(),
            userProfileDao = db.userProfileDao(),
            levelProgressDao = db.levelProgressDao(),
            context = requireContext()  //  USE STORED CONTEXT
        )
    }

    fun getLevelRepository(): LevelRepository {
        return LevelRepository(
            levelProgressDao = requireDatabase().levelProgressDao(),
            context = requireContext()
        )
    }

    fun getArcadeRepository(): ArcadeRepository {
        return ArcadeRepository(requireDatabase().arcadeSessionDao())
    }

    fun getApiRepository(): ApiRepository {
        return ApiRepository()
    }

    fun getAnalyticsRepository(): AnalyticsRepository {
        val db = requireDatabase()
        return AnalyticsRepository(
            gameResultDao = db.gameResultDao(),
            achievementDao = db.achievementDao(),
            userProfileDao = db.userProfileDao()
        )
    }

    //  Notification Repository
    fun getNotificationRepository(): NotificationRepository {
        return NotificationRepository(requireDatabase().notificationDao())
    }

    private fun requireDatabase(): AppDatabase {
        return database ?: throw IllegalStateException(
            "RepositoryProvider must be initialized before use"
        )
    }

    // 🔥 NEW: Helper to get context
    private fun requireContext(): Context {
        return appContext ?: throw IllegalStateException(
            "RepositoryProvider must be initialized before use"
        )
    }

    data class Repositories(
        val userProfileRepository: UserProfileRepository,
        val gameResultRepository: GameResultRepository,
        val achievementRepository: AchievementRepository,
        val levelRepository: LevelRepository,
        val arcadeRepository: ArcadeRepository,
        val apiRepository: ApiRepository,
        val analyticsRepository: AnalyticsRepository,
        val notificationRepository: NotificationRepository
    )
}