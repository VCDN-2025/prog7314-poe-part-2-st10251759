package vcmsa.projects.prog7314.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.dao.ArcadeSessionDao
import vcmsa.projects.prog7314.data.entities.ArcadeSessionEntity
import java.util.UUID

class ArcadeRepository(
    private val arcadeSessionDao: ArcadeSessionDao
) {
    private val TAG = "ArcadeRepository"

    /**
     * Save arcade session result
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
            val sessionId = UUID.randomUUID().toString()

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
                completedAt = System.currentTimeMillis(),
                isSynced = false
            )

            arcadeSessionDao.insertSession(session)
            Log.d(TAG, "✅ Arcade session saved: $sessionId")
            sessionId
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving arcade session: ${e.message}", e)
            null
        }
    }

    /**
     * Get all arcade sessions for user
     */
    suspend fun getAllSessions(userId: String): List<ArcadeSessionEntity> {
        return try {
            arcadeSessionDao.getAllSessions(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting arcade sessions: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get all arcade sessions as Flow
     */
    fun getAllSessionsFlow(userId: String): Flow<List<ArcadeSessionEntity>> {
        return arcadeSessionDao.getAllSessionsFlow(userId)
    }

    /**
     * Get recent arcade sessions
     */
    suspend fun getRecentSessions(userId: String, limit: Int = 10): List<ArcadeSessionEntity> {
        return try {
            arcadeSessionDao.getRecentSessions(userId, limit)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent sessions: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get best arcade score
     */
    suspend fun getBestScore(userId: String): Int {
        return try {
            arcadeSessionDao.getBestScore(userId) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting best score: ${e.message}", e)
            0
        }
    }

    /**
     * Get total arcade sessions count
     */
    suspend fun getTotalSessionsCount(userId: String): Int {
        return try {
            arcadeSessionDao.getTotalSessionsCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total sessions count: ${e.message}", e)
            0
        }
    }

    /**
     * Get average score
     */
    suspend fun getAverageScore(userId: String): Float {
        return try {
            arcadeSessionDao.getAverageScore(userId) ?: 0f
        } catch (e: Exception) {
            Log.e(TAG, "Error getting average score: ${e.message}", e)
            0f
        }
    }

    /**
     * Get unsynced sessions for API sync
     */
    suspend fun getUnsyncedSessions(userId: String): List<ArcadeSessionEntity> {
        return try {
            arcadeSessionDao.getUnsyncedSessions(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced sessions: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Mark session as synced
     */
    suspend fun markSessionAsSynced(sessionId: String) {
        try {
            arcadeSessionDao.markAsSynced(sessionId)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking session as synced: ${e.message}", e)
        }
    }
}