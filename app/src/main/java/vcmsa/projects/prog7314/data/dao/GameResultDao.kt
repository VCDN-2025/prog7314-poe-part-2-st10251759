package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.entities.GameResultEntity

@Dao
interface GameResultDao {

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameResult(gameResult: GameResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameResults(gameResults: List<GameResultEntity>)

    // UPDATE
    @Update
    suspend fun updateGameResult(gameResult: GameResultEntity)

    // DELETE
    @Delete
    suspend fun deleteGameResult(gameResult: GameResultEntity)

    // GET GAME BY ID
    @Query("SELECT * FROM game_results WHERE gameId = :gameId")
    suspend fun getGameResult(gameId: String): GameResultEntity?

    // GET ALL GAMES FOR USER
    @Query("SELECT * FROM game_results WHERE userId = :userId ORDER BY completedAt DESC")
    suspend fun getAllGamesForUser(userId: String): List<GameResultEntity>

    // GET ALL GAMES FOR USER (as Flow)
    @Query("SELECT * FROM game_results WHERE userId = :userId ORDER BY completedAt DESC")
    fun getAllGamesForUserFlow(userId: String): Flow<List<GameResultEntity>>

    // GET RECENT GAMES (limit)
    @Query("SELECT * FROM game_results WHERE userId = :userId ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getRecentGames(userId: String, limit: Int = 10): List<GameResultEntity>

    // GET GAMES BY MODE
    @Query("SELECT * FROM game_results WHERE userId = :userId AND gameMode = :mode ORDER BY completedAt DESC")
    suspend fun getGamesByMode(userId: String, mode: String): List<GameResultEntity>

    // GET GAMES BY THEME
    @Query("SELECT * FROM game_results WHERE userId = :userId AND theme = :theme ORDER BY completedAt DESC")
    suspend fun getGamesByTheme(userId: String, theme: String): List<GameResultEntity>

    // GET UNSYNCED GAMES
    @Query("SELECT * FROM game_results WHERE isSynced = 0")
    suspend fun getUnsyncedGames(): List<GameResultEntity>

    // GET UNSYNCED GAMES FOR USER
    @Query("SELECT * FROM game_results WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedGamesForUser(userId: String): List<GameResultEntity>

    // MARK AS SYNCED
    @Query("UPDATE game_results SET isSynced = 1 WHERE gameId = :gameId")
    suspend fun markAsSynced(gameId: String)

    // MARK MULTIPLE AS SYNCED
    @Query("UPDATE game_results SET isSynced = 1 WHERE gameId IN (:gameIds)")
    suspend fun markMultipleAsSynced(gameIds: List<String>)

    // GET TOTAL GAMES COUNT
    @Query("SELECT COUNT(*) FROM game_results WHERE userId = :userId")
    suspend fun getTotalGamesCount(userId: String): Int

    // GET WINS COUNT
    @Query("SELECT COUNT(*) FROM game_results WHERE userId = :userId AND isWin = 1")
    suspend fun getWinsCount(userId: String): Int

    // GET AVERAGE SCORE
    @Query("SELECT AVG(score) FROM game_results WHERE userId = :userId")
    suspend fun getAverageScore(userId: String): Float?

    // GET AVERAGE TIME
    @Query("SELECT AVG(timeTaken) FROM game_results WHERE userId = :userId")
    suspend fun getAverageTime(userId: String): Float?

    // GET BEST SCORE
    @Query("SELECT MAX(score) FROM game_results WHERE userId = :userId")
    suspend fun getBestScore(userId: String): Int?

    // GET FASTEST TIME
    @Query("SELECT MIN(timeTaken) FROM game_results WHERE userId = :userId AND isWin = 1")
    suspend fun getFastestTime(userId: String): Int?

    // GET GAMES BY DATE RANGE
    @Query("SELECT * FROM game_results WHERE userId = :userId AND completedAt BETWEEN :startDate AND :endDate ORDER BY completedAt DESC")
    suspend fun getGamesByDateRange(userId: String, startDate: Long, endDate: Long): List<GameResultEntity>

    // DELETE ALL GAMES FOR USER
    @Query("DELETE FROM game_results WHERE userId = :userId")
    suspend fun deleteAllGamesForUser(userId: String)

    // DELETE ALL (for testing/reset)
    @Query("DELETE FROM game_results")
    suspend fun deleteAll()
}