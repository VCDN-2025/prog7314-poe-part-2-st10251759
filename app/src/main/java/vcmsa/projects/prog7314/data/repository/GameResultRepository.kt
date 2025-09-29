package vcmsa.projects.prog7314.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.dao.GameResultDao
import vcmsa.projects.prog7314.data.entities.GameResultEntity
import java.util.UUID

class GameResultRepository(
    private val gameResultDao: GameResultDao
) {
    private val TAG = "GameResultRepository"

    // ===== LOCAL DATABASE OPERATIONS =====

    /**
     * Save a game result (local first)
     */
    suspend fun saveGameResult(gameResult: GameResultEntity): Boolean {
        return try {
            Log.d(TAG, "Saving game result: ${gameResult.gameId}")

            // Save to local database
            gameResultDao.insertGameResult(gameResult.copy(isSynced = false))

            Log.d(TAG, "✅ Game result saved locally")

            // TODO: Sync to cloud if online (we'll add this later)

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving game result: ${e.message}", e)
            false
        }
    }

    /**
     * Get all games for a user
     */
    suspend fun getAllGamesForUser(userId: String): List<GameResultEntity> {
        return try {
            Log.d(TAG, "Getting all games for user: $userId")
            gameResultDao.getAllGamesForUser(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting games: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get all games for user as Flow (reactive)
     */
    fun getAllGamesForUserFlow(userId: String): Flow<List<GameResultEntity>> {
        return gameResultDao.getAllGamesForUserFlow(userId)
    }

    /**
     * Get recent games (limited)
     */
    suspend fun getRecentGames(userId: String, limit: Int = 10): List<GameResultEntity> {
        return try {
            Log.d(TAG, "Getting recent $limit games for user: $userId")
            gameResultDao.getRecentGames(userId, limit)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent games: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get games by mode
     */
    suspend fun getGamesByMode(userId: String, mode: String): List<GameResultEntity> {
        return try {
            Log.d(TAG, "Getting games for mode: $mode")
            gameResultDao.getGamesByMode(userId, mode)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting games by mode: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get games by theme
     */
    suspend fun getGamesByTheme(userId: String, theme: String): List<GameResultEntity> {
        return try {
            Log.d(TAG, "Getting games for theme: $theme")
            gameResultDao.getGamesByTheme(userId, theme)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting games by theme: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get games by date range
     */
    suspend fun getGamesByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): List<GameResultEntity> {
        return try {
            Log.d(TAG, "Getting games between $startDate and $endDate")
            gameResultDao.getGamesByDateRange(userId, startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting games by date: ${e.message}", e)
            emptyList()
        }
    }

    // ===== STATISTICS =====

    /**
     * Get total games count
     */
    suspend fun getTotalGamesCount(userId: String): Int {
        return try {
            gameResultDao.getTotalGamesCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total games count: ${e.message}", e)
            0
        }
    }

    /**
     * Get wins count
     */
    suspend fun getWinsCount(userId: String): Int {
        return try {
            gameResultDao.getWinsCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting wins count: ${e.message}", e)
            0
        }
    }

    /**
     * Get average score
     */
    suspend fun getAverageScore(userId: String): Float {
        return try {
            gameResultDao.getAverageScore(userId) ?: 0f
        } catch (e: Exception) {
            Log.e(TAG, "Error getting average score: ${e.message}", e)
            0f
        }
    }

    /**
     * Get average time
     */
    suspend fun getAverageTime(userId: String): Float {
        return try {
            gameResultDao.getAverageTime(userId) ?: 0f
        } catch (e: Exception) {
            Log.e(TAG, "Error getting average time: ${e.message}", e)
            0f
        }
    }

    /**
     * Get best score
     */
    suspend fun getBestScore(userId: String): Int {
        return try {
            gameResultDao.getBestScore(userId) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting best score: ${e.message}", e)
            0
        }
    }

    /**
     * Get fastest time
     */
    suspend fun getFastestTime(userId: String): Int {
        return try {
            gameResultDao.getFastestTime(userId) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting fastest time: ${e.message}", e)
            0
        }
    }

    /**
     * Calculate win rate percentage
     */
    suspend fun getWinRate(userId: String): Float {
        return try {
            val totalGames = getTotalGamesCount(userId)
            val wins = getWinsCount(userId)

            if (totalGames > 0) {
                (wins.toFloat() / totalGames.toFloat()) * 100f
            } else {
                0f
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating win rate: ${e.message}", e)
            0f
        }
    }

    // ===== SYNC OPERATIONS =====

    /**
     * Get unsynced games (to upload to cloud)
     */
    suspend fun getUnsyncedGames(): List<GameResultEntity> {
        return try {
            gameResultDao.getUnsyncedGames()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced games: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get unsynced games for specific user
     */
    suspend fun getUnsyncedGamesForUser(userId: String): List<GameResultEntity> {
        return try {
            gameResultDao.getUnsyncedGamesForUser(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced games for user: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Mark game as synced
     */
    suspend fun markAsSynced(gameId: String): Boolean {
        return try {
            gameResultDao.markAsSynced(gameId)
            Log.d(TAG, "✅ Game marked as synced: $gameId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marking game as synced: ${e.message}", e)
            false
        }
    }

    /**
     * Mark multiple games as synced
     */
    suspend fun markMultipleAsSynced(gameIds: List<String>): Boolean {
        return try {
            gameResultDao.markMultipleAsSynced(gameIds)
            Log.d(TAG, "✅ ${gameIds.size} games marked as synced")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marking multiple games as synced: ${e.message}", e)
            false
        }
    }

    // ===== HELPER FUNCTIONS =====

    /**
     * Create and save a new game result
     */
    suspend fun createGameResult(
        userId: String,
        gameMode: String,
        theme: String,
        gridSize: String,
        difficulty: String,
        score: Int,
        timeTaken: Int,
        moves: Int,
        accuracy: Float,
        isWin: Boolean = true
    ): String? {
        return try {
            val gameId = UUID.randomUUID().toString()

            val gameResult = GameResultEntity(
                gameId = gameId,
                userId = userId,
                gameMode = gameMode,
                theme = theme,
                gridSize = gridSize,
                difficulty = difficulty,
                score = score,
                timeTaken = timeTaken,
                moves = moves,
                accuracy = accuracy,
                completedAt = System.currentTimeMillis(),
                isWin = isWin,
                isSynced = false
            )

            val success = saveGameResult(gameResult)
            if (success) gameId else null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating game result: ${e.message}", e)
            null
        }
    }

    /**
     * Delete all games for user
     */
    suspend fun deleteAllGamesForUser(userId: String): Boolean {
        return try {
            gameResultDao.deleteAllGamesForUser(userId)
            Log.d(TAG, "✅ All games deleted for user: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting games: ${e.message}", e)
            false
        }
    }
}