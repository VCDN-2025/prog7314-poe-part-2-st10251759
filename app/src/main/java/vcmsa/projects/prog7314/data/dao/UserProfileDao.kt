package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.entities.UserProfileEntity

@Dao
interface UserProfileDao {

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    // UPDATE
    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    // DELETE
    @Delete
    suspend fun deleteUserProfile(profile: UserProfileEntity)

    // GET USER BY ID
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfileEntity?

    // GET USER BY ID (as Flow for reactive updates)
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getUserProfileFlow(userId: String): Flow<UserProfileEntity?>

    // GET ALL USERS (for debugging/admin)
    @Query("SELECT * FROM user_profile")
    suspend fun getAllUsers(): List<UserProfileEntity>

    // CHECK IF USER EXISTS
    @Query("SELECT COUNT(*) FROM user_profile WHERE userId = :userId")
    suspend fun userExists(userId: String): Int

    // GET UNSYNCED PROFILES
    @Query("SELECT * FROM user_profile WHERE isSynced = 0")
    suspend fun getUnsyncedProfiles(): List<UserProfileEntity>

    // MARK AS SYNCED
    @Query("UPDATE user_profile SET isSynced = 1 WHERE userId = :userId")
    suspend fun markAsSynced(userId: String)

    // UPDATE AVATAR (NEW METHOD)
    @Query("""
        UPDATE user_profile 
        SET avatarBase64 = :avatarBase64,
            lastUpdated = :timestamp
        WHERE userId = :userId
    """)
    suspend fun updateAvatar(
        userId: String,
        avatarBase64: String?,
        timestamp: Long = System.currentTimeMillis()
    )

    // UPDATE STATS
    @Query("""
        UPDATE user_profile 
        SET totalGamesPlayed = :totalGames,
            gamesWon = :gamesWon,
            currentStreak = :currentStreak,
            bestStreak = :bestStreak,
            averageCompletionTime = :avgTime,
            accuracyRate = :accuracy,
            lastUpdated = :timestamp
        WHERE userId = :userId
    """)
    suspend fun updateStats(
        userId: String,
        totalGames: Int,
        gamesWon: Int,
        currentStreak: Int,
        bestStreak: Int,
        avgTime: Float,
        accuracy: Float,
        timestamp: Long = System.currentTimeMillis()
    )

    // UPDATE XP AND LEVEL
    @Query("""
        UPDATE user_profile 
        SET totalXP = :xp,
            level = :level,
            lastUpdated = :timestamp
        WHERE userId = :userId
    """)
    suspend fun updateXPAndLevel(
        userId: String,
        xp: Int,
        level: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    // DELETE ALL (for testing/reset)
    @Query("DELETE FROM user_profile")
    suspend fun deleteAll()
}