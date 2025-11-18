package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.entities.GameResultEntity

/*
    Code Attribution for: Creating Data Access Objects
    ===================================================
    Android Developers, 2019. Accessing data using Room DAOs | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/accessing-data>
    [Accessed 17 November 2025].
*/


@Dao
interface GameResultDao {

    // ===== INSERT OPERATIONS =====

    // Insert a single game result into the database. Replaces existing if there is a conflict.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameResult(gameResult: GameResultEntity)

    // Insert multiple game results at once. Replaces existing on conflict.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameResults(gameResults: List<GameResultEntity>)

    // ===== UPDATE OPERATIONS =====

    // Update an existing game result in the database
    @Update
    suspend fun updateGameResult(gameResult: GameResultEntity)

    // ===== DELETE OPERATIONS =====

    // Delete a single game result
    @Delete
    suspend fun deleteGameResult(gameResult: GameResultEntity)

    // Delete all game results for a specific user (useful for testing or reset)
    @Query("DELETE FROM game_results WHERE userId = :userId")
    suspend fun deleteAllGamesForUser(userId: String)

    // Delete all game results in the database (useful for testing or reset)
    @Query("DELETE FROM game_results")
    suspend fun deleteAll()

    // ===== RETRIEVE / QUERY OPERATIONS =====

    // Get a specific game result by its unique ID
    @Query("SELECT * FROM game_results WHERE gameId = :gameId")
    suspend fun getGameResult(gameId: String): GameResultEntity?

    // Get all game results for a user, newest first
    @Query("SELECT * FROM game_results WHERE userId = :userId ORDER BY completedAt DESC")
    suspend fun getAllGamesForUser(userId: String): List<GameResultEntity>

    // Same as getAllGamesForUser but returns a Flow to observe live updates
    @Query("SELECT * FROM game_results WHERE userId = :userId ORDER BY completedAt DESC")
    fun getAllGamesForUserFlow(userId: String): Flow<List<GameResultEntity>>

    // Get the most recent N games for a user (default 10)
    @Query("SELECT * FROM game_results WHERE userId = :userId ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getRecentGames(userId: String, limit: Int = 10): List<GameResultEntity>

    // Get all games for a user filtered by game mode
    @Query("SELECT * FROM game_results WHERE userId = :userId AND gameMode = :mode ORDER BY completedAt DESC")
    suspend fun getGamesByMode(userId: String, mode: String): List<GameResultEntity>

    // Get all games for a user filtered by theme
    @Query("SELECT * FROM game_results WHERE userId = :userId AND theme = :theme ORDER BY completedAt DESC")
    suspend fun getGamesByTheme(userId: String, theme: String): List<GameResultEntity>

    // Get all games that have not yet been synced to the server
    @Query("SELECT * FROM game_results WHERE isSynced = 0")
    suspend fun getUnsyncedGames(): List<GameResultEntity>

    // Get all unsynced games for a specific user
    @Query("SELECT * FROM game_results WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedGamesForUser(userId: String): List<GameResultEntity>

    // ===== SYNC OPERATIONS =====

    // Mark a single game result as synced after sending to the server
    @Query("UPDATE game_results SET isSynced = 1 WHERE gameId = :gameId")
    suspend fun markAsSynced(gameId: String)

    // Mark multiple game results as synced at once
    @Query("UPDATE game_results SET isSynced = 1 WHERE gameId IN (:gameIds)")
    suspend fun markMultipleAsSynced(gameIds: List<String>)

    // ===== STATS / AGGREGATE QUERIES =====

    // Count total number of games played by a user
    @Query("SELECT COUNT(*) FROM game_results WHERE userId = :userId")
    suspend fun getTotalGamesCount(userId: String): Int

    // Count total wins for a user
    @Query("SELECT COUNT(*) FROM game_results WHERE userId = :userId AND isWin = 1")
    suspend fun getWinsCount(userId: String): Int

    // Calculate average score for a user
    @Query("SELECT AVG(score) FROM game_results WHERE userId = :userId")
    suspend fun getAverageScore(userId: String): Float?

    // Calculate average time taken for a user across all games
    @Query("SELECT AVG(timeTaken) FROM game_results WHERE userId = :userId")
    suspend fun getAverageTime(userId: String): Float?

    // Get the highest score achieved by a user
    @Query("SELECT MAX(score) FROM game_results WHERE userId = :userId")
    suspend fun getBestScore(userId: String): Int?

    // Get the fastest win time for a user
    @Query("SELECT MIN(timeTaken) FROM game_results WHERE userId = :userId AND isWin = 1")
    suspend fun getFastestTime(userId: String): Int?

    // Get all games for a user within a specific date range
    @Query("SELECT * FROM game_results WHERE userId = :userId AND completedAt BETWEEN :startDate AND :endDate ORDER BY completedAt DESC")
    suspend fun getGamesByDateRange(userId: String, startDate: Long, endDate: Long): List<GameResultEntity>
}
