package vcmsa.projects.prog7314.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import vcmsa.projects.prog7314.data.api.*
import vcmsa.projects.prog7314.data.entities.AchievementEntity
import vcmsa.projects.prog7314.data.entities.GameResultEntity
import vcmsa.projects.prog7314.data.entities.UserProfileEntity

class ApiRepository {

    private val TAG = "ApiRepository"
    private val apiService = RetrofitClient.apiService
    private val auth = FirebaseAuth.getInstance()

    // ===== AUTH =====

    suspend fun verifyFirebaseToken(): Result<AuthVerifyResponse> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("No user logged in"))
            }

            val idToken = currentUser.getIdToken(false).await().token
            if (idToken == null) {
                return Result.failure(Exception("Failed to get ID token"))
            }

            Log.d(TAG, "Verifying token for user: ${currentUser.uid}")

            val response = apiService.verifyToken(AuthVerifyRequest(idToken))

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Log.d(TAG, "✅ Token verified successfully")
                    Result.success(body)
                } else {
                    Log.e(TAG, "❌ Token verification failed: ${body.error}")
                    Result.failure(Exception(body.error ?: "Token verification failed"))
                }
            } else {
                Log.e(TAG, "❌ API error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during token verification: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ===== USER PROFILE =====

    suspend fun syncUserProfile(userProfile: UserProfileEntity): Result<Boolean> {
        return try {
            Log.d(TAG, "Syncing user profile: ${userProfile.userId}")

            val request = UserProfileRequest(
                userId = userProfile.userId,
                username = userProfile.username,
                email = userProfile.email,
                avatarUrl = userProfile.avatarUrl,
                totalXP = userProfile.totalXP,
                level = userProfile.level,
                totalGamesPlayed = userProfile.totalGamesPlayed,
                gamesWon = userProfile.gamesWon,
                currentStreak = userProfile.currentStreak,
                bestStreak = userProfile.bestStreak,
                averageCompletionTime = userProfile.averageCompletionTime,
                accuracyRate = userProfile.accuracyRate
            )

            val response = apiService.createOrUpdateUser(userProfile.userId, request)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ User profile synced successfully")
                Result.success(true)
            } else {
                Log.e(TAG, "❌ Failed to sync user profile: ${response.code()}")
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception syncing user profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(userId: String): Result<UserProfileData> {
        return try {
            Log.d(TAG, "Fetching user profile: $userId")

            val response = apiService.getUserProfile(userId)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Log.d(TAG, "✅ User profile fetched successfully")
                    Result.success(body.data)
                } else {
                    Log.e(TAG, "❌ Failed to fetch user profile: ${body.message}")
                    Result.failure(Exception(body.message ?: "Failed to fetch profile"))
                }
            } else {
                Log.e(TAG, "❌ API error: ${response.code()}")
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching user profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ===== GAME RESULTS =====

    suspend fun syncGameResult(gameResult: GameResultEntity): Result<Boolean> {
        return try {
            Log.d(TAG, "Syncing game result: ${gameResult.gameId}")

            val request = GameResultRequest(
                gameId = gameResult.gameId,
                userId = gameResult.userId,
                gameMode = gameResult.gameMode,
                theme = gameResult.theme,
                gridSize = gameResult.gridSize,
                difficulty = gameResult.difficulty,
                score = gameResult.score,
                timeTaken = gameResult.timeTaken,
                moves = gameResult.moves,
                accuracy = gameResult.accuracy,
                completedAt = gameResult.completedAt,
                isWin = gameResult.isWin
            )

            val response = apiService.submitGameResult(request)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Log.d(TAG, "✅ Game result synced successfully")
                    Result.success(true)
                } else {
                    Log.e(TAG, "❌ Failed to sync game result: ${body.message}")
                    Result.failure(Exception(body.message ?: "Failed to sync game"))
                }
            } else {
                Log.e(TAG, "❌ API error: ${response.code()}")
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception syncing game result: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUserGames(userId: String, limit: Int = 10): Result<List<GameResultData>> {
        return try {
            Log.d(TAG, "Fetching user games: $userId")

            val response = apiService.getUserGames(userId, limit)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Log.d(TAG, "✅ Fetched ${body.data.size} games")
                    Result.success(body.data)
                } else {
                    Log.e(TAG, "❌ Failed to fetch games")
                    Result.failure(Exception("Failed to fetch games"))
                }
            } else {
                Log.e(TAG, "❌ API error: ${response.code()}")
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching games: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchLeaderboard(mode: String = "ARCADE", limit: Int = 10): Result<List<LeaderboardEntry>> {
        return try {
            Log.d(TAG, "Fetching leaderboard for mode: $mode")

            val response = apiService.getLeaderboard(mode, limit)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Log.d(TAG, "✅ Fetched ${body.data.size} leaderboard entries")
                    Result.success(body.data)
                } else {
                    Log.e(TAG, "❌ Failed to fetch leaderboard")
                    Result.failure(Exception("Failed to fetch leaderboard"))
                }
            } else {
                Log.e(TAG, "❌ API error: ${response.code()}")
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching leaderboard: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ===== ACHIEVEMENTS =====

    suspend fun syncAchievement(achievement: AchievementEntity): Result<Boolean> {
        return try {
            Log.d(TAG, "Syncing achievement: ${achievement.achievementId}")

            val request = AchievementRequest(
                userId = achievement.userId,
                achievementType = achievement.achievementType,
                name = achievement.name,
                description = achievement.description
            )

            val response = apiService.unlockAchievement(request)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Log.d(TAG, "✅ Achievement synced successfully")
                    Result.success(true)
                } else {
                    Log.e(TAG, "❌ Failed to sync achievement: ${body.message}")
                    Result.failure(Exception(body.message ?: "Failed to sync achievement"))
                }
            } else {
                Log.e(TAG, "❌ API error: ${response.code()}")
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception syncing achievement: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUserAchievements(userId: String): Result<List<AchievementData>> {
        return try {
            Log.d(TAG, "Fetching user achievements: $userId")

            val response = apiService.getUserAchievements(userId)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Log.d(TAG, "✅ Fetched ${body.data.size} achievements")
                    Result.success(body.data)
                } else {
                    Log.e(TAG, "❌ Failed to fetch achievements")
                    Result.failure(Exception("Failed to fetch achievements"))
                }
            } else {
                Log.e(TAG, "❌ API error: ${response.code()}")
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching achievements: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ADD THIS METHOD TO ApiRepository CLASS

    suspend fun submitMultiplayerResult(
        userId: String,
        theme: String,
        player1Score: Int,
        player2Score: Int,
        timeTaken: Int,
        totalMoves: Int
    ): Result<Boolean> {
        return try {
            Log.d(TAG, "Submitting multiplayer result for user: $userId")

            val request = MultiplayerResultRequest(
                userId = userId,
                theme = theme,
                player1Score = player1Score,
                player2Score = player2Score,
                timeTaken = timeTaken,
                totalMoves = totalMoves,
                timestamp = System.currentTimeMillis().toString()
            )

            val response = apiService.submitMultiplayerResult(request)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Log.d(TAG, "✅ Multiplayer result submitted successfully")
                    Result.success(true)
                } else {
                    Log.e(TAG, "❌ Failed to submit multiplayer result: ${body.message}")
                    Result.failure(Exception(body.message ?: "Failed to submit result"))
                }
            } else {
                Log.e(TAG, "❌ API error: ${response.code()}")
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception submitting multiplayer result: ${e.message}", e)
            Result.failure(e)
        }
    }
}