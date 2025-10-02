package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.entities.LevelProgressEntity

@Dao
interface LevelProgressDao {

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevelProgress(progress: LevelProgressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLevelProgress(progressList: List<LevelProgressEntity>)

    // UPDATE
    @Update
    suspend fun updateLevelProgress(progress: LevelProgressEntity)

    // GET LEVEL PROGRESS
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND levelNumber = :levelNumber")
    suspend fun getLevelProgress(userId: String, levelNumber: Int): LevelProgressEntity?

    @Query("SELECT * FROM level_progress WHERE userId = :userId AND levelNumber = :levelNumber")
    fun getLevelProgressFlow(userId: String, levelNumber: Int): Flow<LevelProgressEntity?>

    // GET ALL LEVELS FOR USER
    @Query("SELECT * FROM level_progress WHERE userId = :userId ORDER BY levelNumber ASC")
    suspend fun getAllLevelsProgress(userId: String): List<LevelProgressEntity>

    @Query("SELECT * FROM level_progress WHERE userId = :userId ORDER BY levelNumber ASC")
    fun getAllLevelsProgressFlow(userId: String): Flow<List<LevelProgressEntity>>

    // GET UNLOCKED LEVELS
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND isUnlocked = 1 ORDER BY levelNumber ASC")
    suspend fun getUnlockedLevels(userId: String): List<LevelProgressEntity>

    // GET COMPLETED LEVELS
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedLevels(userId: String): List<LevelProgressEntity>

    // GET COMPLETED LEVELS COUNT
    @Query("SELECT COUNT(*) FROM level_progress WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedLevelsCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM level_progress WHERE userId = :userId AND isCompleted = 1")
    fun getCompletedLevelsCountFlow(userId: String): Flow<Int>

    // UNLOCK LEVEL
    @Query("UPDATE level_progress SET isUnlocked = 1, updatedAt = :timestamp WHERE userId = :userId AND levelNumber = :levelNumber")
    suspend fun unlockLevel(userId: String, levelNumber: Int, timestamp: Long = System.currentTimeMillis())

    // UPDATE LEVEL RESULT
    @Query("""
        UPDATE level_progress 
        SET stars = :stars,
            bestScore = CASE WHEN :score > bestScore THEN :score ELSE bestScore END,
            bestTime = CASE WHEN bestTime = 0 THEN :time 
                            WHEN :time < bestTime THEN :time 
                            ELSE bestTime END,
            bestMoves = CASE WHEN bestMoves = 0 THEN :moves
                             WHEN :moves < bestMoves THEN :moves
                             ELSE bestMoves END,
            isCompleted = 1,
            lastPlayed = :timestamp,
            timesPlayed = timesPlayed + 1,
            isSynced = 0,
            updatedAt = :timestamp
        WHERE userId = :userId AND levelNumber = :levelNumber
    """)
    suspend fun updateLevelResult(
        userId: String,
        levelNumber: Int,
        stars: Int,
        score: Int,
        time: Int,
        moves: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    // GET TOTAL STARS
    @Query("SELECT SUM(stars) FROM level_progress WHERE userId = :userId")
    suspend fun getTotalStars(userId: String): Int?

    // GET UNSYNCED LEVELS
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedLevels(userId: String): List<LevelProgressEntity>

    // MARK AS SYNCED
    @Query("UPDATE level_progress SET isSynced = 1 WHERE userId = :userId AND levelNumber = :levelNumber")
    suspend fun markAsSynced(userId: String, levelNumber: Int)

    // DELETE ALL FOR USER (for testing/reset)
    @Query("DELETE FROM level_progress WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}