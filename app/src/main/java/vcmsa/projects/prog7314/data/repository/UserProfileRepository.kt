package vcmsa.projects.prog7314.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.dao.UserProfileDao
import vcmsa.projects.prog7314.data.entities.UserProfileEntity

/*
    Code Attribution for: Repositories
    ===================================================
    Android Developers, 2025. Data layer (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/architecture/data-layer>
    [Accessed 18 November 2025].

    Code Attribution for: Room DB Entities
    ===================================================
    Android Developers, 2020. Defining data using Room entities | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/defining-data>
    [Accessed 18 November 2025].

    Code Attribution for: Connecting to Firebase Database
    ===================================================
    Firebase, 2025. Installation & Setup on Android | Firebase Realtime Database (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/database/android/start>
    [Accessed 18 November 2025].

*/

/**
 * Repository responsible for managing user profiles locally and preparing for future cloud sync.
 * Handles CRUD operations on the UserProfileEntity, updating stats, XP/Level, streaks, and win rate.
 */
class UserProfileRepository(
    private val userProfileDao: UserProfileDao
) {
    // Tag used for logging messages in Logcat
    private val TAG = "UserProfileRepository"

    // ===== LOCAL DATABASE OPERATIONS =====

    /**
     * Retrieve a single user profile by user ID from local database.
     * @param userId The unique identifier of the user.
     * @return UserProfileEntity if found, else null.
     */
    suspend fun getUserProfile(userId: String): UserProfileEntity? {
        return try {
            Log.d(TAG, "Getting user profile for: $userId")
            userProfileDao.getUserProfile(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile: ${e.message}", e)
            null
        }
    }

    /**
     * Retrieve a user profile as a Flow for reactive updates.
     * Useful for observing changes in UI in real-time.
     * @param userId The unique identifier of the user.
     * @return Flow emitting UserProfileEntity? objects.
     */
    fun getUserProfileFlow(userId: String): Flow<UserProfileEntity?> {
        return userProfileDao.getUserProfileFlow(userId)
    }

    /**
     * Save or update a user profile in local database.
     * Marks the profile as unsynced (isSynced = false) to track cloud sync status.
     * @param profile The UserProfileEntity to save.
     * @return Boolean indicating success or failure.
     */
    suspend fun saveUserProfile(profile: UserProfileEntity): Boolean {
        return try {
            Log.d(TAG, "Saving user profile: ${profile.username}")

            // Save profile locally, mark as unsynced
            userProfileDao.insertUserProfile(profile.copy(isSynced = false))

            Log.d(TAG, "✅ User profile saved locally")
            // TODO: Sync to cloud if online in future

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving user profile: ${e.message}", e)
            false
        }
    }

    /**
     * Update statistics of a user, such as total games, wins, streaks, average completion time, and accuracy.
     * @param userId User identifier
     * @param totalGames Total games played
     * @param gamesWon Total games won
     * @param currentStreak Current consecutive win/play streak
     * @param bestStreak Highest recorded streak
     * @param avgTime Average time to complete games
     * @param accuracy Accuracy percentage
     * @return Boolean indicating success or failure
     */
    suspend fun updateUserStats(
        userId: String,
        totalGames: Int,
        gamesWon: Int,
        currentStreak: Int,
        bestStreak: Int,
        avgTime: Float,
        accuracy: Float
    ): Boolean {
        return try {
            Log.d(TAG, "Updating stats for user: $userId")

            userProfileDao.updateStats(
                userId = userId,
                totalGames = totalGames,
                gamesWon = gamesWon,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                avgTime = avgTime,
                accuracy = accuracy
            )

            Log.d(TAG, "✅ User stats updated")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating stats: ${e.message}", e)
            false
        }
    }

    /**
     * Update XP and level of a user.
     * @param userId User identifier
     * @param xp Total XP points
     * @param level Current level
     * @return Boolean indicating success or failure
     */
    suspend fun updateXPAndLevel(userId: String, xp: Int, level: Int): Boolean {
        return try {
            Log.d(TAG, "Updating XP ($xp) and Level ($level) for user: $userId")

            userProfileDao.updateXPAndLevel(userId, xp, level)

            Log.d(TAG, "✅ XP and Level updated")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating XP/Level: ${e.message}", e)
            false
        }
    }

    /**
     * Check if a user exists in the local database.
     * @param userId User identifier
     * @return true if user exists, false otherwise
     */
    suspend fun userExists(userId: String): Boolean {
        return try {
            userProfileDao.userExists(userId) > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user existence: ${e.message}", e)
            false
        }
    }

    /**
     * Delete a user profile from local database.
     * @param userId User identifier
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deleteUserProfile(userId: String): Boolean {
        return try {
            val profile = userProfileDao.getUserProfile(userId)
            if (profile != null) {
                userProfileDao.deleteUserProfile(profile)
                Log.d(TAG, "✅ User profile deleted")
                true
            } else {
                Log.w(TAG, "User profile not found for deletion")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting user profile: ${e.message}", e)
            false
        }
    }

    // ===== SYNC OPERATIONS =====

    /**
     * Retrieve all unsynced profiles for cloud sync.
     * @return List of UserProfileEntity objects marked as unsynced
     */
    suspend fun getUnsyncedProfiles(): List<UserProfileEntity> {
        return try {
            userProfileDao.getUnsyncedProfiles()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced profiles: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Mark a user profile as synced with cloud.
     * @param userId User identifier
     * @return Boolean indicating success or failure
     */
    suspend fun markAsSynced(userId: String): Boolean {
        return try {
            userProfileDao.markAsSynced(userId)
            Log.d(TAG, "✅ Profile marked as synced: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marking as synced: ${e.message}", e)
            false
        }
    }

    // ===== HELPER FUNCTIONS =====

    /**
     * Create a new user profile with default values for XP, level, stats, and sync flag.
     * @param userId User identifier
     * @param username Username
     * @param email User email
     * @param avatarBase64 Optional avatar image encoded as Base64 string
     * @return Boolean indicating if profile was successfully created
     */
    suspend fun createNewUserProfile(
        userId: String,
        username: String,
        email: String,
        avatarBase64: String? = null
    ): Boolean {
        val newProfile = UserProfileEntity(
            userId = userId,
            username = username,
            email = email,
            avatarBase64 = avatarBase64,
            totalXP = 0,
            level = 1,
            totalGamesPlayed = 0,
            gamesWon = 0,
            currentStreak = 0,
            bestStreak = 0,
            lastPlayDate = 0L,
            averageCompletionTime = 0f,
            accuracyRate = 0f,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            isSynced = false
        )

        return saveUserProfile(newProfile)
    }

    /**
     * Calculate and return the user's win rate as a percentage.
     * @param userId User identifier
     * @return Win rate as Float (0-100), or 0 if no games played
     */
    suspend fun calculateWinRate(userId: String): Float {
        return try {
            val profile = getUserProfile(userId)
            if (profile != null && profile.totalGamesPlayed > 0) {
                (profile.gamesWon.toFloat() / profile.totalGamesPlayed.toFloat()) * 100f
            } else {
                0f
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating win rate: ${e.message}", e)
            0f
        }
    }

    // ===== DAILY STREAK METHODS =====

    /**
     * Update the daily streak for a user.
     * Ensures streak only updates once per calendar day.
     * Resets streak if user hasn't played in the last 48 hours.
     * @param userId User identifier
     * @return Boolean indicating if streak update was successful
     */
    suspend fun updateDailyStreak(userId: String): Boolean {
        return try {
            val profile = getUserProfile(userId) ?: return false
            val currentTime = System.currentTimeMillis()
            val lastPlayDate = profile.lastPlayDate

            // Check if already played today
            val isSameDay = isSameCalendarDay(lastPlayDate, currentTime)

            if (isSameDay && lastPlayDate > 0) {
                // Already played today - update lastPlayDate only
                Log.d(TAG, "✅ Already played today. Streak stays at ${profile.currentStreak}")
                userProfileDao.updateStreakAndPlayDate(
                    userId = userId,
                    currentStreak = profile.currentStreak,
                    bestStreak = profile.bestStreak,
                    lastPlayDate = currentTime
                )
                return true
            }

            // Calculate hours since last play to determine streak continuation
            val hoursSinceLastPlay = if (lastPlayDate > 0) {
                (currentTime - lastPlayDate) / (1000 * 60 * 60)
            } else {
                0L
            }

            val newStreak: Int
            val newBestStreak: Int

            when {
                lastPlayDate == 0L -> {
                    // First play
                    newStreak = 1
                    newBestStreak = 1
                    Log.d(TAG, "🔥 Started new streak: Day 1")
                }
                hoursSinceLastPlay < 48 -> {
                    // Continue streak
                    newStreak = profile.currentStreak + 1
                    newBestStreak = maxOf(profile.bestStreak, newStreak)
                    Log.d(TAG, "🔥 Streak continued! Day $newStreak")
                }
                else -> {
                    // Reset streak
                    newStreak = 1
                    newBestStreak = profile.bestStreak
                    Log.d(TAG, "💔 Streak reset. Starting fresh: Day 1")
                }
            }

            // Update profile with new streak values
            userProfileDao.updateStreakAndPlayDate(
                userId = userId,
                currentStreak = newStreak,
                bestStreak = newBestStreak,
                lastPlayDate = currentTime
            )

            Log.d(TAG, "✅ Streak updated successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating daily streak: ${e.message}", e)
            false
        }
    }

    /**
     * Helper to determine if two timestamps belong to the same calendar day.
     */
    private fun isSameCalendarDay(timestamp1: Long, timestamp2: Long): Boolean {
        if (timestamp1 == 0L) return false

        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = timestamp2 }

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    /**
     * Check if the user's streak should continue (within 48 hours of last play).
     * @param userId User identifier
     * @return true if streak continues, false otherwise
     */
    suspend fun shouldContinueStreak(userId: String): Boolean {
        return try {
            val profile = getUserProfile(userId) ?: return false
            val lastPlayDate = profile.lastPlayDate

            if (lastPlayDate == 0L) return false

            val currentTime = System.currentTimeMillis()
            val hoursSinceLastPlay = (currentTime - lastPlayDate) / (1000 * 60 * 60)

            hoursSinceLastPlay < 48
        } catch (e: Exception) {
            Log.e(TAG, "Error checking streak status: ${e.message}", e)
            false
        }
    }

    /**
     * Retrieve the current streak value for a user.
     * @param userId User identifier
     * @return Current streak as Int
     */
    suspend fun getCurrentStreak(userId: String): Int {
        return try {
            val profile = getUserProfile(userId)
            profile?.currentStreak ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current streak: ${e.message}", e)
            0
        }
    }
}
