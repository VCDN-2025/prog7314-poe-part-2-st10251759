package vcmsa.projects.prog7314.data.repository

import android.content.Context
import vcmsa.projects.prog7314.data.AppDatabase

/*
    Code Attribution for: Repositories
    ===================================================
    Android Developers, 2025. Data layer (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/architecture/data-layer>
    [Accessed 18 November 2025].
*/

/**
 * RepositoryProvider is a centralized singleton object responsible for:
 * - Providing access to all repositories in the app
 * - Managing the AppDatabase instance
 * - Storing application context for repositories that require it
 *
 * This object acts as a dependency injection helper, ensuring only a single
 * database instance is created and reused across the app.
 */
object RepositoryProvider {

    // Singleton instance of the AppDatabase
    private var database: AppDatabase? = null

    // Application context, stored for repositories that require Context
    private var appContext: Context? = null  // 🔥 STORED CONTEXT

    /**
     * Initialize the RepositoryProvider with the application context.
     * Ensures the AppDatabase is only created once.
     *
     * @param context The application context
     */
    fun initialize(context: Context) {
        if (database == null) {
            appContext = context.applicationContext  // Store application context for later use
            database = AppDatabase.getDatabase(appContext!!) // Initialize Room database
        }
    }

    /**
     * Returns a data class containing all repository instances.
     * Automatically initializes the provider if not already initialized.
     *
     * @param context The context used for initialization if needed
     * @return Repositories object containing all repositories
     */
    fun getRepositories(context: Context): Repositories {
        initialize(context) // Ensure database and context are initialized
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

    /**
     * Returns an instance of UserProfileRepository using UserProfileDao.
     */
    fun getUserProfileRepository(): UserProfileRepository {
        return UserProfileRepository(requireDatabase().userProfileDao())
    }

    /**
     * Returns an instance of GameResultRepository using GameResultDao.
     */
    fun getGameResultRepository(): GameResultRepository {
        return GameResultRepository(requireDatabase().gameResultDao())
    }

    /**
     * Returns an instance of AchievementRepository.
     * Requires multiple DAOs and Context.
     */
    fun getAchievementRepository(): AchievementRepository {
        val db = requireDatabase()
        return AchievementRepository(
            achievementDao = db.achievementDao(),
            gameResultDao = db.gameResultDao(),
            userProfileDao = db.userProfileDao(),
            levelProgressDao = db.levelProgressDao(),
            context = requireContext()  // Use stored application context
        )
    }

    /**
     * Returns an instance of LevelRepository, passing LevelProgressDao and Context.
     */
    fun getLevelRepository(): LevelRepository {
        return LevelRepository(
            levelProgressDao = requireDatabase().levelProgressDao(),
            context = requireContext()
        )
    }

    /**
     * Returns an instance of ArcadeRepository using ArcadeSessionDao.
     */
    fun getArcadeRepository(): ArcadeRepository {
        return ArcadeRepository(requireDatabase().arcadeSessionDao())
    }

    /**
     * Returns an instance of ApiRepository.
     * Does not require DAO or context.
     */
    fun getApiRepository(): ApiRepository {
        return ApiRepository()
    }

    /**
     * Returns an instance of AnalyticsRepository.
     * Requires multiple DAOs for data aggregation.
     */
    fun getAnalyticsRepository(): AnalyticsRepository {
        val db = requireDatabase()
        return AnalyticsRepository(
            gameResultDao = db.gameResultDao(),
            achievementDao = db.achievementDao(),
            userProfileDao = db.userProfileDao()
        )
    }

    /**
     * Returns an instance of NotificationRepository using NotificationDao.
     */
    fun getNotificationRepository(): NotificationRepository {
        return NotificationRepository(requireDatabase().notificationDao())
    }

    /**
     * Helper function to ensure the database instance is not null.
     * Throws an exception if RepositoryProvider was not initialized.
     *
     * @return AppDatabase instance
     */
    private fun requireDatabase(): AppDatabase {
        return database ?: throw IllegalStateException(
            "RepositoryProvider must be initialized before use"
        )
    }

    /**
     * Helper function to ensure the application context is not null.
     * Throws an exception if RepositoryProvider was not initialized.
     *
     * @return Context instance
     */
    private fun requireContext(): Context {
        return appContext ?: throw IllegalStateException(
            "RepositoryProvider must be initialized before use"
        )
    }

    /**
     * Data class representing all repositories.
     * Used as a container to provide easy access to all repositories at once.
     */
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
