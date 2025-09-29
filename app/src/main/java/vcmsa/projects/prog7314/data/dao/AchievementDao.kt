package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.entities.AchievementEntity

@Dao
interface AchievementDao {

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    // UPDATE
    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // DELETE
    @Delete
    suspend fun deleteAchievement(achievement: AchievementEntity)

    // GET ACHIEVEMENT BY ID
    @Query("SELECT * FROM achievements WHERE achievementId = :achievementId")
    suspend fun getAchievement(achievementId: String): AchievementEntity?

    // GET ALL ACHIEVEMENTS FOR USER
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt DESC")
    suspend fun getAllAchievementsForUser(userId: String): List<AchievementEntity>

    // GET ALL ACHIEVEMENTS FOR USER (as Flow)
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt DESC")
    fun getAllAchievementsForUserFlow(userId: String): Flow<List<AchievementEntity>>

    // GET UNLOCKED ACHIEVEMENTS
    @Query("SELECT * FROM achievements WHERE userId = :userId AND isUnlocked = 1 ORDER BY unlockedAt DESC")
    suspend fun getUnlockedAchievements(userId: String): List<AchievementEntity>

    // GET UNLOCKED ACHIEVEMENTS COUNT
    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId AND isUnlocked = 1")
    suspend fun getUnlockedCount(userId: String): Int

    // CHECK IF ACHIEVEMENT EXISTS
    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId AND achievementType = :type")
    suspend fun achievementExists(userId: String, type: String): Int

    // GET ACHIEVEMENT BY TYPE
    @Query("SELECT * FROM achievements WHERE userId = :userId AND achievementType = :type")
    suspend fun getAchievementByType(userId: String, type: String): AchievementEntity?

    // GET RECENT ACHIEVEMENTS (limit)
    @Query("SELECT * FROM achievements WHERE userId = :userId AND isUnlocked = 1 ORDER BY unlockedAt DESC LIMIT :limit")
    suspend fun getRecentAchievements(userId: String, limit: Int = 5): List<AchievementEntity>

    // GET UNSYNCED ACHIEVEMENTS
    @Query("SELECT * FROM achievements WHERE isSynced = 0")
    suspend fun getUnsyncedAchievements(): List<AchievementEntity>

    // GET UNSYNCED ACHIEVEMENTS FOR USER
    @Query("SELECT * FROM achievements WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedAchievementsForUser(userId: String): List<AchievementEntity>

    // MARK AS SYNCED
    @Query("UPDATE achievements SET isSynced = 1 WHERE achievementId = :achievementId")
    suspend fun markAsSynced(achievementId: String)

    // MARK MULTIPLE AS SYNCED
    @Query("UPDATE achievements SET isSynced = 1 WHERE achievementId IN (:achievementIds)")
    suspend fun markMultipleAsSynced(achievementIds: List<String>)

    // UPDATE PROGRESS
    @Query("UPDATE achievements SET progress = :progress WHERE achievementId = :achievementId")
    suspend fun updateProgress(achievementId: String, progress: Int)

    // UNLOCK ACHIEVEMENT
    @Query("""
        UPDATE achievements 
        SET isUnlocked = 1, 
            progress = 100,
            unlockedAt = :timestamp
        WHERE achievementId = :achievementId
    """)
    suspend fun unlockAchievement(achievementId: String, timestamp: Long = System.currentTimeMillis())

    // DELETE ALL ACHIEVEMENTS FOR USER
    @Query("DELETE FROM achievements WHERE userId = :userId")
    suspend fun deleteAllAchievementsForUser(userId: String)

    // DELETE ALL (for testing/reset)
    @Query("DELETE FROM achievements")
    suspend fun deleteAll()
}