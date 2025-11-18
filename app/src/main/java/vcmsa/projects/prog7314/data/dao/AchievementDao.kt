package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.entities.AchievementEntity

/*
    Code Attribution for: Creating Data Access Objects
    ===================================================
    Android Developers, 2019. Accessing data using Room DAOs | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/accessing-data>
    [Accessed 17 November 2025].
*/

@Dao
interface AchievementDao {

    // ===== INSERT OPERATIONS =====

    // Insert a single achievement into the database. Replaces existing if conflict.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    // Insert multiple achievements at once. Replaces existing on conflict.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    // ===== UPDATE OPERATIONS =====

    // Update an existing achievement's information
    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // ===== DELETE OPERATIONS =====

    // Remove a single achievement from the database
    @Delete
    suspend fun deleteAchievement(achievement: AchievementEntity)

    // Delete all achievements for a specific user
    @Query("DELETE FROM achievements WHERE userId = :userId")
    suspend fun deleteAllAchievementsForUser(userId: String)

    // Delete all achievements in the database (useful for testing or reset)
    @Query("DELETE FROM achievements")
    suspend fun deleteAll()

    // ===== RETRIEVE / QUERY OPERATIONS =====

    // Get a specific achievement by its unique ID
    @Query("SELECT * FROM achievements WHERE achievementId = :achievementId")
    suspend fun getAchievement(achievementId: String): AchievementEntity?

    // Get all achievements for a user, newest first
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt DESC")
    suspend fun getAllAchievementsForUser(userId: String): List<AchievementEntity>

    // Get all achievements for a user as a Flow (useful for observing changes in real-time)
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt DESC")
    fun getAllAchievementsForUserFlow(userId: String): Flow<List<AchievementEntity>>

    // Get only achievements that are unlocked for a specific user
    @Query("SELECT * FROM achievements WHERE userId = :userId AND isUnlocked = 1 ORDER BY unlockedAt DESC")
    suspend fun getUnlockedAchievements(userId: String): List<AchievementEntity>

    // Count how many achievements are unlocked for a user
    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId AND isUnlocked = 1")
    suspend fun getUnlockedCount(userId: String): Int

    // Check if a specific type of achievement exists for a user
    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId AND achievementType = :type")
    suspend fun achievementExists(userId: String, type: String): Int

    // Get a single achievement by type for a user
    @Query("SELECT * FROM achievements WHERE userId = :userId AND achievementType = :type")
    suspend fun getAchievementByType(userId: String, type: String): AchievementEntity?

    // Get the most recent unlocked achievements for a user, with a default limit of 5
    @Query("SELECT * FROM achievements WHERE userId = :userId AND isUnlocked = 1 ORDER BY unlockedAt DESC LIMIT :limit")
    suspend fun getRecentAchievements(userId: String, limit: Int = 5): List<AchievementEntity>

    // Get all achievements that have not yet been synced to the server
    @Query("SELECT * FROM achievements WHERE isSynced = 0")
    suspend fun getUnsyncedAchievements(): List<AchievementEntity>

    // Get all unsynced achievements for a specific user
    @Query("SELECT * FROM achievements WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedAchievementsForUser(userId: String): List<AchievementEntity>

    // ===== SYNC OPERATIONS =====

    // Mark a single achievement as synced
    @Query("UPDATE achievements SET isSynced = 1 WHERE achievementId = :achievementId")
    suspend fun markAsSynced(achievementId: String)

    // Mark multiple achievements as synced at once
    @Query("UPDATE achievements SET isSynced = 1 WHERE achievementId IN (:achievementIds)")
    suspend fun markMultipleAsSynced(achievementIds: List<String>)

    // ===== PROGRESS / UNLOCK OPERATIONS =====

    // Update progress percentage for a specific achievement
    @Query("UPDATE achievements SET progress = :progress WHERE achievementId = :achievementId")
    suspend fun updateProgress(achievementId: String, progress: Int)

    // Unlock an achievement, set progress to 100%, and record the unlocked timestamp
    @Query("""
        UPDATE achievements 
        SET isUnlocked = 1, 
            progress = 100,
            unlockedAt = :timestamp
        WHERE achievementId = :achievementId
    """)
    suspend fun unlockAchievement(achievementId: String, timestamp: Long = System.currentTimeMillis())
}
