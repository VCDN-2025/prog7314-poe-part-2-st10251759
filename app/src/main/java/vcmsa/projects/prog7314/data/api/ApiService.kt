package vcmsa.projects.prog7314.data.api

import retrofit2.Response
import retrofit2.http.*

// ===== EXISTING DATA MODELS =====

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
    val gameId: String? = null,
    val message: String? = null,
    val newBest: Boolean = false,
    val data: GameResultData? = null
)

data class GameResultData(
    val gameId: String,
    val userId: String? = null,
    val gameMode: String? = null,
    val theme: String? = null,
    val gridSize: String? = null,
    val difficulty: String? = null,
    val score: Int? = null,
    val timeTaken: Int? = null,
    val moves: Int? = null,
    val accuracy: Float? = null,
    val completedAt: Long? = null,
    val isWin: Boolean? = null,
    val previousBest: Int? = null,
    val newBest: Int? = null
)

data class GamesListResponse(
    val success: Boolean,
    val data: List<GameResultData>?
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

// ===== NEW ARCADE & LEVEL DATA MODELS =====

data class LevelResultRequest(
    val userId: String,
    val levelNumber: Int,
    val stars: Int,
    val score: Int,
    val time: Int,
    val moves: Int,
    val difficulty: String,
    val theme: String,
    val gridSize: String,
    val completedAt: Long = System.currentTimeMillis()
)

data class ArcadeResultRequest(
    val userId: String,
    val sessionId: String,
    val score: Int,
    val time: Int,
    val moves: Int,
    val bonus: Int,
    val stars: Int,
    val theme: String,
    val gridSize: String,
    val difficulty: String,
    val completedAt: Long = System.currentTimeMillis()
)

data class LevelProgressResponse(
    val success: Boolean,
    val message: String? = null,
    val data: LevelProgressData? = null
)

data class LevelProgressData(
    val levels: List<LevelProgressItem>,
    val totalStars: Int,
    val completedCount: Int
)

data class LevelProgressItem(
    val levelNumber: Int,
    val stars: Int,
    val bestScore: Int,
    val bestTime: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean
)

data class ThemesResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<ThemeItem>? = null
)

data class ThemeItem(
    val id: String,
    val displayName: String,
    val previewImageUrl: String? = null,
    val cardImageUrls: List<String>? = null
)

// ===== API SERVICE INTERFACE =====

interface ApiService {

    // ===== AUTH ENDPOINTS =====
    @POST("api/auth/verify")
    suspend fun verifyToken(@Body request: AuthVerifyRequest): Response<AuthVerifyResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/auth/user/{userId}")
    suspend fun getUserInfo(@Path("userId") userId: String): Response<UserInfoResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>

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

    // ===== ACHIEVEMENT ENDPOINTS =====
    @GET("api/achievements/user/{userId}")
    suspend fun getUserAchievements(@Path("userId") userId: String): Response<AchievementsListResponse>

    @POST("api/achievements/unlock")
    suspend fun unlockAchievement(@Body achievement: AchievementRequest): Response<AchievementResponse>

    // ===== NEW: ARCADE & LEVEL ENDPOINTS =====

    @POST("api/games/level-result")
    suspend fun submitLevelResult(
        @Body request: LevelResultRequest
    ): Response<GameResultResponse>

    @POST("api/games/arcade-result")
    suspend fun submitArcadeResult(
        @Body request: ArcadeResultRequest
    ): Response<GameResultResponse>

    @GET("api/user/{userId}/level-progress")
    suspend fun getUserLevelProgress(
        @Path("userId") userId: String
    ): Response<LevelProgressResponse>

    @GET("api/themes")
    suspend fun getThemes(): Response<ThemesResponse>

    // ===== MULTIPLAYER ENDPOINTS =====
    @POST("api/multiplayer/result")
    suspend fun submitMultiplayerResult(
        @Body request: MultiplayerResultRequest
    ): Response<MultiplayerResultResponse>
}

// ADD THESE DATA CLASSES AFTER EXISTING ONES

data class MultiplayerResultRequest(
    val userId: String,
    val theme: String,
    val player1Score: Int,
    val player2Score: Int,
    val timeTaken: Int,
    val totalMoves: Int,
    val timestamp: String
)

data class MultiplayerResultResponse(
    val success: Boolean,
    val message: String? = null,
    val data: MultiplayerResultData? = null
)

data class MultiplayerResultData(
    val gameId: String,
    val winner: String?
)
