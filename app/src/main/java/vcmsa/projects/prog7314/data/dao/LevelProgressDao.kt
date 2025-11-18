package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.entities.LevelProgressEntity

/*
    Code Attribution for: Creating Data Access Objects
    ===================================================
    Android Developers, 2019. Accessing data using Room DAOs | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/accessing-data>
    [Accessed 17 November 2025].
*/


@Dao
interface LevelProgressDao {

    // Insert a single level progress record. Replaces any existing record for the same level.
    // Returns the row ID of the newly inserted item.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevelProgress(progress: LevelProgressEntity): Long

    // Insert a list of level progress records in one operation.
    // Useful for initializing or restoring user progress.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLevelProgress(progressList: List<LevelProgressEntity>)

    // Update an existing level progress entry.
    @Update
    suspend fun updateLevelProgress(progress: LevelProgressEntity)

    // Fetch a single level’s progress for a user.
    // Returns null if the level has no progress yet.
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND levelNumber = :levelNumber")
    suspend fun getLevelProgress(userId: String, levelNumber: Int): LevelProgressEntity?

    // Same as above, but returns a Flow so UI can update automatically when changes occur.
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND levelNumber = :levelNumber")
    fun getLevelProgressFlow(userId: String, levelNumber: Int): Flow<LevelProgressEntity?>

    // Fetch progress for all levels of a user, sorted by level number.
    @Query("SELECT * FROM level_progress WHERE userId = :userId ORDER BY levelNumber ASC")
    suspend fun getAllLevelsProgress(userId: String): List<LevelProgressEntity>

    // Flow version for observing progress changes for all levels.
    @Query("SELECT * FROM level_progress WHERE userId = :userId ORDER BY levelNumber ASC")
    fun getAllLevelsProgressFlow(userId: String): Flow<List<LevelProgressEntity>>

    // Get all levels that the user has unlocked.
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND isUnlocked = 1 ORDER BY levelNumber ASC")
    suspend fun getUnlockedLevels(userId: String): List<LevelProgressEntity>

    // Get all levels the user has completed.
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedLevels(userId: String): List<LevelProgressEntity>

    // Count how many levels the user has completed.
    @Query("SELECT COUNT(*) FROM level_progress WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedLevelsCount(userId: String): Int

    // Flow version of the completed levels count, useful for real-time UI badges.
    @Query("SELECT COUNT(*) FROM level_progress WHERE userId = :userId AND isCompleted = 1")
    fun getCompletedLevelsCountFlow(userId: String): Flow<Int>

    // Mark a specific level as unlocked.
    // Also updates the timestamp to track when the change happened.
    @Query("UPDATE level_progress SET isUnlocked = 1, updatedAt = :timestamp WHERE userId = :userId AND levelNumber = :levelNumber")
    suspend fun unlockLevel(userId: String, levelNumber: Int, timestamp: Long = System.currentTimeMillis())

    // Update the result for a level after the user plays it.
    // Stores best score, best time, best moves, and other stats.
    // Also increases the times played counter and marks the level as completed.
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

    // Sum all stars earned across all levels for a user.
    @Query("SELECT SUM(stars) FROM level_progress WHERE userId = :userId")
    suspend fun getTotalStars(userId: String): Int?

    // Fetch all levels that have not yet been synced to the server.
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedLevels(userId: String): List<LevelProgressEntity>

    // Mark a specific level as synced.
    @Query("UPDATE level_progress SET isSynced = 1 WHERE userId = :userId AND levelNumber = :levelNumber")
    suspend fun markAsSynced(userId: String, levelNumber: Int)

    // Delete all level progress for a user.
    // Useful when resetting an account or during testing.
    @Query("DELETE FROM level_progress WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
