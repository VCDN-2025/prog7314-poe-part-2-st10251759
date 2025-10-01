package vcmsa.projects.prog7314.data.api

import retrofit2.Response
import retrofit2.http.*

// Data models for API requests/responses
data class UserProfileRequest(
    val userId: String,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val totalXP: Int = 0,
    val level: Int = 1,
    val totalGamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val averageCompletionTime: Float = 0f,
    val accuracyRate: Float = 0f
)

data class UserProfileResponse(
    val success: Boolean,
    val data: UserProfileData?,
    val message: String? = null
)

data class UserProfileData(
    val userId: String,
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val totalXP: Int,
    val level: Int,
    val totalGamesPlayed: Int,
    val gamesWon: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val averageCompletionTime: Float,
    val accuracyRate: Float,
    val lastUpdated: Any?
)

data class GameResultRequest(
    val gameId: String,
    val userId: String,
    val gameMode: String,
    val theme: String,
    val gridSize: String,
    val difficulty: String,
    val score: Int,
    val timeTaken: Int,
    val moves: Int,
    val accuracy: Float,
    val completedAt: Long,
    val isWin: Boolean
)

data class GameResultResponse(
    val success: Boolean,
    val gameId: String?,
    val message: String? = null
)

data class GamesListResponse(
    val success: Boolean,
    val data: List<GameResultData>?
)

data class GameResultData(
    val gameId: String,
    val userId: String,
    val gameMode: String,
    val theme: String,
    val gridSize: String,
    val difficulty: String,
    val score: Int,
    val timeTaken: Int,
    val moves: Int,
    val accuracy: Float,
    val completedAt: Long,
    val isWin: Boolean
)

data class AchievementRequest(
    val userId: String,
    val achievementType: String,
    val name: String,
    val description: String
)

data class AchievementResponse(
    val success: Boolean,
    val achievementId: String?,
    val message: String? = null
)

data class AchievementsListResponse(
    val success: Boolean,
    val data: List<AchievementData>?
)

data class AchievementData(
    val achievementId: String,
    val userId: String,
    val achievementType: String,
    val name: String,
    val description: String,
    val isUnlocked: Boolean,
    val progress: Int,
    val unlockedAt: Any?
)

data class AuthVerifyRequest(
    val idToken: String
)

data class AuthVerifyResponse(
    val success: Boolean,
    val userId: String?,
    val email: String?,
    val error: String? = null
)

data class LeaderboardResponse(
    val success: Boolean,
    val data: List<LeaderboardEntry>?
)

data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val score: Int,
    val timeTaken: Int,
    val completedAt: Long
)

interface ApiService {

    // ===== AUTH ENDPOINTS =====
    @POST("api/auth/verify")
    suspend fun verifyToken(@Body request: AuthVerifyRequest): Response<AuthVerifyResponse>

    // ===== USER ENDPOINTS =====
    @GET("api/users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<UserProfileResponse>

    @POST("api/users/{userId}")
    suspend fun createOrUpdateUser(
        @Path("userId") userId: String,
        @Body user: UserProfileRequest
    ): Response<UserProfileResponse>

    @GET("api/users/{userId}/progress")
    suspend fun getUserProgress(@Path("userId") userId: String): Response<UserProfileResponse>

    @PUT("api/users/{userId}/settings")
    suspend fun updateUserSettings(
        @Path("userId") userId: String,
        @Body settings: Map<String, Any>
    ): Response<UserProfileResponse>

    // ===== GAME ENDPOINTS =====
    @POST("api/games/result")
    suspend fun submitGameResult(@Body gameResult: GameResultRequest): Response<GameResultResponse>

    @GET("api/games/user/{userId}")
    suspend fun getUserGames(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 10
    ): Response<GamesListResponse>

    @GET("api/games/leaderboard")
    suspend fun getLeaderboard(
        @Query("mode") mode: String = "ARCADE",
        @Query("limit") limit: Int = 10
    ): Response<LeaderboardResponse>
// Add to existing ApiService interface

    // Registration
    data class RegisterRequest(
        val email: String,
        val password: String,
        val username: String
    )

    data class RegisterResponse(
        val success: Boolean,
        val userId: String?,
        val email: String?,
        val username: String?,
        val customToken: String?,
        val message: String?,
        val error: String?
    )

    // Login
    data class LoginRequest(
        val email: String,
        val password: String
    )

    data class LoginResponse(
        val success: Boolean,
        val userId: String?,
        val email: String?,
        val username: String?,
        val customToken: String?,
        val message: String?,
        val error: String?
    )

    interface ApiService {

        // ... existing endpoints ...

        // Registration endpoint
        @POST("api/auth/register")
        suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

        // Login endpoint (get custom token)
        @POST("api/auth/login")
        suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

        // Get user info
        @GET("api/auth/user/{userId}")
        suspend fun getUserInfo(@Path("userId") userId: String): Response<UserInfoResponse>

        // Request password reset
        @POST("api/auth/reset-password")
        suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>
    }

    data class UserInfoResponse(
        val success: Boolean,
        val data: UserInfoData?,
        val error: String?
    )

    data class UserInfoData(
        val userId: String,
        val email: String,
        val username: String,
        val emailVerified: Boolean,
        val createdAt: String,
        val lastSignIn: String
    )

    data class ResetPasswordRequest(
        val email: String
    )

    data class ResetPasswordResponse(
        val success: Boolean,
        val resetLink: String?,
        val message: String?,
        val error: String?
    )
    // ===== ACHIEVEMENT ENDPOINTS =====
    @GET("api/achievements/user/{userId}")
    suspend fun getUserAchievements(@Path("userId") userId: String): Response<AchievementsListResponse>

    @POST("api/achievements/unlock")
    suspend fun unlockAchievement(@Body achievement: AchievementRequest): Response<AchievementResponse>
}