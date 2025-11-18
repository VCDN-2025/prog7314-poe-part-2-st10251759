package vcmsa.projects.prog7314.data.api

import retrofit2.Response
import retrofit2.http.*

/*
    Code Attribution for: Creating RESTful APIs
    ===================================================
    W3Schools, 2025. W3Schools.com (Version unknown) [Source code].
    Available at: <https://www.w3schools.com/nodejs/nodejs_rest_api.asp>
    [Accessed 17 November 2025].
*/


// ===== EXISTING DATA MODELS =====

// Holds all the profile information sent when creating or updating a user
data class UserProfileRequest(
    val userId: String,              // Unique ID for the user
    val username: String,            // Player's chosen display name
    val email: String,               // User's email address
    val avatarUrl: String? = null,   // Optional profile picture
    val totalXP: Int = 0,            // Total experience collected
    val level: Int = 1,              // User's current overall level
    val totalGamesPlayed: Int = 0,   // Number of games completed
    val gamesWon: Int = 0,           // Number of wins
    val currentStreak: Int = 0,      // Current win streak
    val bestStreak: Int = 0,         // Best win streak ever reached
    val averageCompletionTime: Float = 0f, // Average time taken per game
    val accuracyRate: Float = 0f     // Accuracy percentage across games
)

// Standard response wrapper for user profile operations
data class UserProfileResponse(
    val success: Boolean,            // Indicates if the request succeeded
    val data: UserProfileData?,      // Contains the actual profile data
    val message: String? = null      // Optional message from API
)

// Represents a full user profile returned by the backend
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
    val lastUpdated: Any?            // Timestamp or version marker
)

// Sent when recording a completed game session
data class GameResultRequest(
    val gameId: String,
    val userId: String,
    val gameMode: String,            // Mode played such as timed or arcade
    val theme: String,               // Theme used for the cards
    val gridSize: String,            // Grid layout like 4x4 or 6x6
    val difficulty: String,          // Difficulty chosen
    val score: Int,
    val timeTaken: Int,
    val moves: Int,
    val accuracy: Float,             // Player accuracy for that round
    val completedAt: Long,           // Timestamp of completion
    val isWin: Boolean               // Whether the user won the match
)

// Response wrapper for game results
data class GameResultResponse(
    val success: Boolean,
    val gameId: String? = null,
    val message: String? = null,
    val newBest: Boolean = false,    // Indicates if user beat a previous best
    val data: GameResultData? = null
)

// Detailed information returned after submitting or retrieving results
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
    val previousBest: Int? = null,   // User’s earlier best score/time
    val newBest: Int? = null         // Updated best value if improved
)

// Response returned when requesting a list of user games
data class GamesListResponse(
    val success: Boolean,
    val data: List<GameResultData>?  // List of recent games
)

// Data required to unlock an achievement
data class AchievementRequest(
    val userId: String,
    val achievementType: String,     // Category like streaks or score goals
    val name: String,
    val description: String
)

// Response for achievement unlock attempts
data class AchievementResponse(
    val success: Boolean,
    val achievementId: String?,
    val message: String? = null
)

// Response for retrieving all achievements tied to a user
data class AchievementsListResponse(
    val success: Boolean,
    val data: List<AchievementData>?
)

// Full achievement details
data class AchievementData(
    val achievementId: String,
    val userId: String,
    val achievementType: String,
    val name: String,
    val description: String,
    val isUnlocked: Boolean,         // Shows if user has unlocked it already
    val progress: Int,               // Progress toward unlocking
    val unlockedAt: Any?             // Timestamp of unlock if applicable
)

// Sent to verify authentication tokens
data class AuthVerifyRequest(
    val idToken: String
)

// Returned after token validation
data class AuthVerifyResponse(
    val success: Boolean,
    val userId: String?,
    val email: String?,
    val error: String? = null
)

// Response for leaderboard fetch requests
data class LeaderboardResponse(
    val success: Boolean,
    val data: List<LeaderboardEntry>?
)

// Represents a single leaderboard row
data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val score: Int,
    val timeTaken: Int,
    val completedAt: Long
)

// Registration request details
data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

// Registration response from server
data class RegisterResponse(
    val success: Boolean,
    val userId: String?,
    val email: String?,
    val username: String?,
    val customToken: String?,
    val message: String?,
    val error: String?
)

// Login request structure
data class LoginRequest(
    val email: String,
    val password: String
)

// Login response from server
data class LoginResponse(
    val success: Boolean,
    val userId: String?,
    val email: String?,
    val username: String?,
    val customToken: String?,
    val message: String?,
    val error: String?
)

// Response when retrieving user account info
data class UserInfoResponse(
    val success: Boolean,
    val data: UserInfoData?,
    val error: String?
)

// Holds key account information after login or verification
data class UserInfoData(
    val userId: String,
    val email: String,
    val username: String,
    val emailVerified: Boolean,
    val createdAt: String,
    val lastSignIn: String
)

// Structure used when requesting a password reset
data class ResetPasswordRequest(
    val email: String
)

// Response for password reset attempts
data class ResetPasswordResponse(
    val success: Boolean,
    val resetLink: String?,
    val message: String?,
    val error: String?
)

// ===== NEW ARCADE & LEVEL DATA MODELS =====

// Sent when saving level-based gameplay results
data class LevelResultRequest(
    val userId: String,
    val levelNumber: Int,
    val stars: Int,              // Star rating earned for the level
    val score: Int,
    val time: Int,
    val moves: Int,
    val difficulty: String,
    val theme: String,
    val gridSize: String,
    val completedAt: Long = System.currentTimeMillis()
)

// Sent when saving arcade mode results
data class ArcadeResultRequest(
    val userId: String,
    val sessionId: String,        // Unique ID for the arcade session
    val score: Int,
    val time: Int,
    val moves: Int,
    val bonus: Int,               // Additional bonus points
    val stars: Int,
    val theme: String,
    val gridSize: String,
    val difficulty: String,
    val completedAt: Long = System.currentTimeMillis()
)

// Response summarizing a user's level progress
data class LevelProgressResponse(
    val success: Boolean,
    val message: String? = null,
    val data: LevelProgressData? = null
)

// Full breakdown of level progress for a player
data class LevelProgressData(
    val levels: List<LevelProgressItem>,
    val totalStars: Int,          // Total stars earned across all levels
    val completedCount: Int       // How many levels have been completed
)

// Represents one level's status
data class LevelProgressItem(
    val levelNumber: Int,
    val stars: Int,
    val bestScore: Int,
    val bestTime: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean
)

// Response for theme list retrieval
data class ThemesResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<ThemeItem>? = null
)

// Represents one available theme
data class ThemeItem(
    val id: String,
    val displayName: String,
    val previewImageUrl: String? = null,
    val cardImageUrls: List<String>? = null
)

// ===== API SERVICE INTERFACE =====

// Main Retrofit interface that defines all endpoints used by the app
interface ApiService {

    // ===== AUTH ENDPOINTS =====

    // Verifies a login token from the client
    @POST("api/auth/verify")
    suspend fun verifyToken(@Body request: AuthVerifyRequest): Response<AuthVerifyResponse>

    // Registers a new user account
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    // Logs in an existing user
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Retrieves basic user details by ID
    @GET("api/auth/user/{userId}")
    suspend fun getUserInfo(@Path("userId") userId: String): Response<UserInfoResponse>

    // Sends request to reset password via email
    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>

    // ===== USER ENDPOINTS =====

    // Fetches a user's full profile
    @GET("api/users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<UserProfileResponse>

    // Creates or updates a user's profile information
    @POST("api/users/{userId}")
    suspend fun createOrUpdateUser(
        @Path("userId") userId: String,
        @Body user: UserProfileRequest
    ): Response<UserProfileResponse>

    // Retrieves the progress data for a user
    @GET("api/users/{userId}/progress")
    suspend fun getUserProgress(@Path("userId") userId: String): Response<UserProfileResponse>

    // Updates user-specific settings such as preferences
    @PUT("api/users/{userId}/settings")
    suspend fun updateUserSettings(
        @Path("userId") userId: String,
        @Body settings: Map<String, Any>
    ): Response<UserProfileResponse>

    // ===== GAME ENDPOINTS =====

    // Submits the result of a completed game
    @POST("api/games/result")
    suspend fun submitGameResult(@Body gameResult: GameResultRequest): Response<GameResultResponse>

    // Gets a list of recent games played by the user
    @GET("api/games/user/{userId}")
    suspend fun getUserGames(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 10
    ): Response<GamesListResponse>

    // Fetches the leaderboard for a specific mode
    @GET("api/games/leaderboard")
    suspend fun getLeaderboard(
        @Query("mode") mode: String = "ARCADE",
        @Query("limit") limit: Int = 10
    ): Response<LeaderboardResponse>

    // ===== ACHIEVEMENT ENDPOINTS =====

    // Retrieves all achievements tied to a specific user
    @GET("api/achievements/user/{userId}")
    suspend fun getUserAchievements(@Path("userId") userId: String): Response<AchievementsListResponse>

    // Submits a request to unlock an achievement
    @POST("api/achievements/unlock")
    suspend fun unlockAchievement(@Body achievement: AchievementRequest): Response<AchievementResponse>

    // ===== NEW: ARCADE & LEVEL ENDPOINTS =====

    // Submits the results of a completed level
    @POST("api/games/level-result")
    suspend fun submitLevelResult(
        @Body request: LevelResultRequest
    ): Response<GameResultResponse>

    // Submits the results of an arcade session
    @POST("api/games/arcade-result")
    suspend fun submitArcadeResult(
        @Body request: ArcadeResultRequest
    ): Response<GameResultResponse>

    // Retrieves level progress for the given user
    @GET("api/user/{userId}/level-progress")
    suspend fun getUserLevelProgress(
        @Path("userId") userId: String
    ): Response<LevelProgressResponse>

    // Fetches all available themes for the game
    @GET("api/themes")
    suspend fun getThemes(): Response<ThemesResponse>

    // ===== MULTIPLAYER ENDPOINTS =====

    // Submits the results of a multiplayer match
    @POST("api/multiplayer/result")
    suspend fun submitMultiplayerResult(
        @Body request: MultiplayerResultRequest
    ): Response<MultiplayerResultResponse>
}

// Models used for multiplayer game submissions

data class MultiplayerResultRequest(
    val userId: String,         // ID of the player submitting the result
    val theme: String,          // Theme used in the match
    val player1Score: Int,
    val player2Score: Int,
    val timeTaken: Int,
    val totalMoves: Int,
    val timestamp: String       // Completion timestamp
)

// Response for multiplayer result operations
data class MultiplayerResultResponse(
    val success: Boolean,
    val message: String? = null,
    val data: MultiplayerResultData? = null
)

// Contains details like who won the multiplayer round
data class MultiplayerResultData(
    val gameId: String,
    val winner: String?         // User ID of the winning player
)