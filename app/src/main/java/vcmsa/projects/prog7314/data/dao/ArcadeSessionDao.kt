package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.entities.ArcadeSessionEntity

@Dao
interface ArcadeSessionDao {

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ArcadeSessionEntity)

    // GET SESSION BY ID
    @Query("SELECT * FROM arcade_sessions WHERE sessionId = :sessionId")
    suspend fun getSession(sessionId: String): ArcadeSessionEntity?

    // GET ALL SESSIONS FOR USER
    @Query("SELECT * FROM arcade_sessions WHERE userId = :userId ORDER BY completedAt DESC")
    suspend fun getAllSessions(userId: String): List<ArcadeSessionEntity>

    @Query("SELECT * FROM arcade_sessions WHERE userId = :userId ORDER BY completedAt DESC")
    fun getAllSessionsFlow(userId: String): Flow<List<ArcadeSessionEntity>>

    // GET RECENT SESSIONS (last N)
    @Query("SELECT * FROM arcade_sessions WHERE userId = :userId ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getRecentSessions(userId: String, limit: Int = 10): List<ArcadeSessionEntity>

    // GET BEST SCORE
    @Query("SELECT MAX(score) FROM arcade_sessions WHERE userId = :userId")
    suspend fun getBestScore(userId: String): Int?

    // GET TOTAL SESSIONS COUNT
    @Query("SELECT COUNT(*) FROM arcade_sessions WHERE userId = :userId")
    suspend fun getTotalSessionsCount(userId: String): Int

    // GET SESSIONS BY THEME
    @Query("SELECT * FROM arcade_sessions WHERE userId = :userId AND theme = :theme ORDER BY completedAt DESC")
    suspend fun getSessionsByTheme(userId: String, theme: String): List<ArcadeSessionEntity>

    // GET AVERAGE SCORE
    @Query("SELECT AVG(score) FROM arcade_sessions WHERE userId = :userId")
    suspend fun getAverageScore(userId: String): Float?

    // GET UNSYNCED SESSIONS
    @Query("SELECT * FROM arcade_sessions WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedSessions(userId: String): List<ArcadeSessionEntity>

    // MARK AS SYNCED
    @Query("UPDATE arcade_sessions SET isSynced = 1 WHERE sessionId = :sessionId")
    suspend fun markAsSynced(sessionId: String)

    // DELETE SESSION
    @Delete
    suspend fun deleteSession(session: ArcadeSessionEntity)

    // DELETE ALL FOR USER (for testing/reset)
    @Query("DELETE FROM arcade_sessions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}