package vcmsa.projects.prog7314.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.prog7314.data.dao.UserProfileDao
import vcmsa.projects.prog7314.data.entities.UserProfileEntity

class UserProfileRepository(
    private val userProfileDao: UserProfileDao
) {
    private val TAG = "UserProfileRepository"

    // ===== LOCAL DATABASE OPERATIONS =====

    /**
     * Get user profile (from local database)
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
     * Get user profile as Flow (reactive updates)
     */
    fun getUserProfileFlow(userId: String): Flow<UserProfileEntity?> {
        return userProfileDao.getUserProfileFlow(userId)
    }

    /**
     * Save or update user profile (local first)
     */
    suspend fun saveUserProfile(profile: UserProfileEntity): Boolean {
        return try {
            Log.d(TAG, "Saving user profile: ${profile.username}")

            // Save to local database
            userProfileDao.insertUserProfile(profile.copy(isSynced = false))

            Log.d(TAG, "✅ User profile saved locally")

            // TODO: Sync to cloud if online (we'll add this later)

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving user profile: ${e.message}", e)
            false
        }
    }

    /**
     * Update user statistics
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
     * Update XP and level
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
     * Check if user exists in local database
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
     * Delete user profile
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
     * Get unsynced profiles (to upload to cloud)
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
     * Mark profile as synced
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
     * Create a new user profile with defaults
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
     * Calculate and update win rate
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
     * Update daily streak - call this whenever user plays a game
     * Fixed: Only updates once per calendar day
     */
    suspend fun updateDailyStreak(userId: String): Boolean {
        return try {
            val profile = getUserProfile(userId) ?: return false
            val currentTime = System.currentTimeMillis()
            val lastPlayDate = profile.lastPlayDate

            // Check if already played today (same calendar day)
            val isSameDay = isSameCalendarDay(lastPlayDate, currentTime)

            if (isSameDay && lastPlayDate > 0) {
                // Already played today - just update lastPlayDate, don't change streak
                Log.d(TAG, "✅ Already played today. Streak stays at ${profile.currentStreak}")
                userProfileDao.updateStreakAndPlayDate(
                    userId = userId,
                    currentStreak = profile.currentStreak,
                    bestStreak = profile.bestStreak,
                    lastPlayDate = currentTime
                )
                return true
            }

            // Calculate hours since last play
            val hoursSinceLastPlay = if (lastPlayDate > 0) {
                (currentTime - lastPlayDate) / (1000 * 60 * 60)
            } else {
                0L
            }

            val newStreak: Int
            val newBestStreak: Int

            when {
                // First time playing or no previous play date
                lastPlayDate == 0L -> {
                    newStreak = 1
                    newBestStreak = 1
                    Log.d(TAG, "🔥 Started new streak: Day 1")
                }
                // Played within 48 hours - continue streak
                hoursSinceLastPlay < 48 -> {
                    newStreak = profile.currentStreak + 1
                    newBestStreak = maxOf(profile.bestStreak, newStreak)
                    Log.d(TAG, "🔥 Streak continued! Day $newStreak")
                }
                // More than 48 hours - reset streak
                else -> {
                    newStreak = 1
                    newBestStreak = profile.bestStreak
                    Log.d(TAG, "💔 Streak reset. Starting fresh: Day 1")
                }
            }

            // Update the profile with new streak data
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
     * Check if two timestamps are on the same calendar day
     */
    private fun isSameCalendarDay(timestamp1: Long, timestamp2: Long): Boolean {
        if (timestamp1 == 0L) return false

        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = timestamp2 }

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    /**
     * Check if user should continue streak (within 48 hours)
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
     * Get current streak for user
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