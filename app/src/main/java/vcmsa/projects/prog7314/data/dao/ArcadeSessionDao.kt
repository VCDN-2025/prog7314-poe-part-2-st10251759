package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.entities.ArcadeSessionEntity

/*
    Code Attribution for: Creating Data Access Objects
    ===================================================
    Android Developers, 2019. Accessing data using Room DAOs | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/accessing-data>
    [Accessed 17 November 2025].
*/

@Dao
interface ArcadeSessionDao {

    // ===== INSERT OPERATIONS =====

    // Insert a single arcade session into the database.
    // If the session already exists, it will be replaced.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ArcadeSessionEntity)

    // ===== RETRIEVE / QUERY OPERATIONS =====

    // Get a specific session by its unique ID
    @Query("SELECT * FROM arcade_sessions WHERE sessionId = :sessionId")
    suspend fun getSession(sessionId: String): ArcadeSessionEntity?

    // Get all sessions for a user, ordered from newest to oldest
    @Query("SELECT * FROM arcade_sessions WHERE userId = :userId ORDER BY completedAt DESC")
    suspend fun getAllSessions(userId: String): List<ArcadeSessionEntity>

    // Same as getAllSessions but returns a Flow to observe live updates
    @Query("SELECT * FROM arcade_sessions WHERE userId = :userId ORDER BY completedAt DESC")
    fun getAllSessionsFlow(userId: String): Flow<List<ArcadeSessionEntity>>

    // Get the most recent N sessions for a user (default 10)
    @Query("SELECT * FROM arcade_sessions WHERE userId = :userId ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getRecentSessions(userId: String, limit: Int = 10): List<ArcadeSessionEntity>

    // Get the highest score the user has achieved in any session
    @Query("SELECT MAX(score) FROM arcade_sessions WHERE userId = :userId")
    suspend fun getBestScore(userId: String): Int?

    // Get the total number of sessions played by a user
    @Query("SELECT COUNT(*) FROM arcade_sessions WHERE userId = :userId")
    suspend fun getTotalSessionsCount(userId: String): Int

    // Get all sessions for a user filtered by a specific theme
    @Query("SELECT * FROM arcade_sessions WHERE userId = :userId AND theme = :theme ORDER BY completedAt DESC")
    suspend fun getSessionsByTheme(userId: String, theme: String): List<ArcadeSessionEntity>

    // Get the average score across all sessions for a user
    @Query("SELECT AVG(score) FROM arcade_sessions WHERE userId = :userId")
    suspend fun getAverageScore(userId: String): Float?

    // Get all sessions that have not yet been synced with the server
    @Query("SELECT * FROM arcade_sessions WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedSessions(userId: String): List<ArcadeSessionEntity>

    // ===== SYNC OPERATIONS =====

    // Mark a single session as synced after sending to the server
    @Query("UPDATE arcade_sessions SET isSynced = 1 WHERE sessionId = :sessionId")
    suspend fun markAsSynced(sessionId: String)

    // ===== DELETE OPERATIONS =====

    // Delete a single session from the database
    @Delete
    suspend fun deleteSession(session: ArcadeSessionEntity)

    // Delete all sessions for a specific user (useful for testing or reset)
    @Query("DELETE FROM arcade_sessions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
