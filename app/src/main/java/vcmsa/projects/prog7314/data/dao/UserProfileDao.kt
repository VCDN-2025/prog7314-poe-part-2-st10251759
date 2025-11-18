package vcmsa.projects.prog7314.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.entities.UserProfileEntity

/*
    Code Attribution for: Creating Data Access Objects
    ===================================================
    Android Developers, 2019. Accessing data using Room DAOs | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/accessing-data>
    [Accessed 17 November 2025].
*/


@Dao
interface UserProfileDao {

    // Inserts a user profile into the table.
    // If the profile already exists, it will be replaced.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    // Updates an existing user profile.
    // Only fields in the provided object will be updated.
    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    // Deletes a specific user profile.
    @Delete
    suspend fun deleteUserProfile(profile: UserProfileEntity)

    // Gets a single profile by userId.
    // Returns null if the user does not exist.
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfileEntity?

    // Same as above, but returns a Flow so the UI can react to changes in real time.
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getUserProfileFlow(userId: String): Flow<UserProfileEntity?>

    // Returns all users in the table.
    // Mainly useful for testing or admin features.
    @Query("SELECT * FROM user_profile")
    suspend fun getAllUsers(): List<UserProfileEntity>

    // Checks if a user exists by counting matching rows.
    // Returns 0 if not found, 1 if found.
    @Query("SELECT COUNT(*) FROM user_profile WHERE userId = :userId")
    suspend fun userExists(userId: String): Int

    // Returns all user profiles that have not been synced to a server.
    @Query("SELECT * FROM user_profile WHERE isSynced = 0")
    suspend fun getUnsyncedProfiles(): List<UserProfileEntity>

    // Marks a user profile as synced once it has been uploaded.
    @Query("UPDATE user_profile SET isSynced = 1 WHERE userId = :userId")
    suspend fun markAsSynced(userId: String)

    // Updates the user's avatar.
    // Also sets a timestamp so you know when the change was made.
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

    // Updates a set of gameplay-related statistics.
    // Useful after a game session finishes.
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

    // Updates the user's XP and level.
    // This would be called when the user earns XP.
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

    // Updates the user's daily streak information and their last play date.
    @Query("""
        UPDATE user_profile 
        SET currentStreak = :currentStreak,
            bestStreak = :bestStreak,
            lastPlayDate = :lastPlayDate,
            lastUpdated = :timestamp
        WHERE userId = :userId
    """)
    suspend fun updateStreakAndPlayDate(
        userId: String,
        currentStreak: Int,
        bestStreak: Int,
        lastPlayDate: Long,
        timestamp: Long = System.currentTimeMillis()
    )

    // Deletes all rows from the table.
    // Helpful for testing or resetting the app.
    @Query("DELETE FROM user_profile")
    suspend fun deleteAll()
}
