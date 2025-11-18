package vcmsa.projects.prog7314.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.dao.ArcadeSessionDao
import vcmsa.projects.prog7314.data.entities.ArcadeSessionEntity
import java.util.UUID

/*
    Code Attribution for: Repositories
    ===================================================
    Android Developers, 2025. Data layer (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/architecture/data-layer>
    [Accessed 18 November 2025].
*/

/**
 * Repository class responsible for managing all data operations related to Arcade sessions.
 * This includes inserting new sessions, fetching session data, and tracking sync status with API.
 * Uses ArcadeSessionDao for interacting with the local Room database.
 */
class ArcadeRepository(
    private val arcadeSessionDao: ArcadeSessionDao // DAO for arcade sessions injected via constructor
) {
    private val TAG = "ArcadeRepository" // Tag used for logging

    /**
     * Save arcade session result to the local database.
     *
     * @param userId ID of the user who played the session.
     * @param theme Theme of the arcade game session.
     * @param gridSize Grid size for the game session.
     * @param difficulty Difficulty level of the game session.
     * @param score Score achieved in the session.
     * @param timeTaken Time taken to complete the session in milliseconds.
     * @param moves Number of moves taken during the session.
     * @param bonus Bonus points earned during the session.
     * @param stars Number of stars earned during the session.
     * @return The generated session ID if successful, or null if an error occurred.
     */
    suspend fun saveArcadeSession(
        userId: String,
        theme: String,
        gridSize: String,
        difficulty: String,
        score: Int,
        timeTaken: Int,
        moves: Int,
        bonus: Int,
        stars: Int
    ): String? {
        return try {
            // Generate a unique session ID using UUID
            val sessionId = UUID.randomUUID().toString()

            // Create a new ArcadeSessionEntity object with all session details
            val session = ArcadeSessionEntity(
                sessionId = sessionId,
                userId = userId,
                theme = theme,
                gridSize = gridSize,
                difficulty = difficulty,
                score = score,
                timeTaken = timeTaken,
                moves = moves,
                bonus = bonus,
                stars = stars,
                completedAt = System.currentTimeMillis(), // Record timestamp of completion
                isSynced = false // Initially set as unsynced with the API
            )

            // Insert session into local database using DAO
            arcadeSessionDao.insertSession(session)
            Log.d(TAG, "✅ Arcade session saved: $sessionId") // Log success
            sessionId // Return the generated session ID
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving arcade session: ${e.message}", e) // Log exception
            null // Return null on failure
        }
    }

    /**
     * Get all arcade sessions for a specific user from the local database.
     *
     * @param userId ID of the user whose sessions are fetched.
     * @return List of all ArcadeSessionEntity objects for the user, or empty list on error.
     */
    suspend fun getAllSessions(userId: String): List<ArcadeSessionEntity> {
        return try {
            arcadeSessionDao.getAllSessions(userId) // Fetch all sessions from DAO
        } catch (e: Exception) {
            Log.e(TAG, "Error getting arcade sessions: ${e.message}", e) // Log error
            emptyList() // Return empty list on failure
        }
    }

    /**
     * Get all arcade sessions as a Flow, enabling reactive updates.
     *
     * @param userId ID of the user whose sessions are observed.
     * @return Flow emitting list of ArcadeSessionEntity objects whenever data changes.
     */
    fun getAllSessionsFlow(userId: String): Flow<List<ArcadeSessionEntity>> {
        return arcadeSessionDao.getAllSessionsFlow(userId) // Return Flow from DAO
    }

    /**
     * Get a limited number of the most recent arcade sessions for a user.
     *
     * @param userId ID of the user.
     * @param limit Maximum number of recent sessions to fetch. Default is 10.
     * @return List of most recent ArcadeSessionEntity objects, or empty list on error.
     */
    suspend fun getRecentSessions(userId: String, limit: Int = 10): List<ArcadeSessionEntity> {
        return try {
            arcadeSessionDao.getRecentSessions(userId, limit) // Fetch recent sessions from DAO
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent sessions: ${e.message}", e) // Log error
            emptyList() // Return empty list on failure
        }
    }

    /**
     * Get the highest score achieved by a user in arcade sessions.
     *
     * @param userId ID of the user.
     * @return Best score as Int, or 0 if no sessions exist or on error.
     */
    suspend fun getBestScore(userId: String): Int {
        return try {
            arcadeSessionDao.getBestScore(userId) ?: 0 // Fetch best score or default to 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting best score: ${e.message}", e) // Log error
            0 // Return 0 on failure
        }
    }

    /**
     * Get the total number of arcade sessions played by a user.
     *
     * @param userId ID of the user.
     * @return Total count of sessions, or 0 on error.
     */
    suspend fun getTotalSessionsCount(userId: String): Int {
        return try {
            arcadeSessionDao.getTotalSessionsCount(userId) // Fetch count from DAO
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total sessions count: ${e.message}", e) // Log error
            0 // Return 0 on failure
        }
    }

    /**
     * Get the average score across all arcade sessions for a user.
     *
     * @param userId ID of the user.
     * @return Average score as Float, or 0f if no sessions exist or on error.
     */
    suspend fun getAverageScore(userId: String): Float {
        return try {
            arcadeSessionDao.getAverageScore(userId) ?: 0f // Fetch average or default 0f
        } catch (e: Exception) {
            Log.e(TAG, "Error getting average score: ${e.message}", e) // Log error
            0f // Return 0f on failure
        }
    }

    /**
     * Get all unsynced arcade sessions for a user.
     * This is useful for syncing data with a remote API.
     *
     * @param userId ID of the user.
     * @return List of unsynced ArcadeSessionEntity objects, or empty list on error.
     */
    suspend fun getUnsyncedSessions(userId: String): List<ArcadeSessionEntity> {
        return try {
            arcadeSessionDao.getUnsyncedSessions(userId) // Fetch unsynced sessions from DAO
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced sessions: ${e.message}", e) // Log error
            emptyList() // Return empty list on failure
        }
    }

    /**
     * Mark a specific arcade session as synced.
     * Used after successfully syncing session data with remote API.
     *
     * @param sessionId ID of the session to mark as synced.
     */
    suspend fun markSessionAsSynced(sessionId: String) {
        try {
            arcadeSessionDao.markAsSynced(sessionId) // Update isSynced flag in DAO
        } catch (e: Exception) {
            Log.e(TAG, "Error marking session as synced: ${e.message}", e) // Log error
        }
    }
}
