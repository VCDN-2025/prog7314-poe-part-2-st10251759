package vcmsa.projects.prog7314.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import vcmsa.projects.prog7314.data.entities.AchievementEntity
import vcmsa.projects.prog7314.data.entities.GameResultEntity
import vcmsa.projects.prog7314.data.entities.UserProfileEntity
import vcmsa.projects.prog7314.data.models.GameProgress
import vcmsa.projects.prog7314.data.models.LevelData
/*
    Code Attribution for: Connecting to Firebase Database
    ===================================================
    Firebase, 2025. Installation & Setup on Android | Firebase Realtime Database (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/database/android/start>
    [Accessed 18 November 2025].
*/


class FirestoreManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val TAG = "FirestoreManager"

    // ===== USER PROFILE SYNC =====

    /**
     * Upload user profile to Firestore
     */
    /**
     * Uploads a given user profile to Firestore.
     * Marks the profile as updated with the current timestamp in `lastUpdated`.
     * Uses `SetOptions.merge()` to avoid overwriting existing fields that are not provided.
     *
     * @param profile The UserProfileEntity containing user data to upload.
     * @return Result<Unit> indicating success or failure of the upload operation.
     */
    suspend fun uploadUserProfile(profile: UserProfileEntity): Result<Unit> {
        return try {
            // Get the current logged-in user's UID from Firebase Auth.
            // If no user is logged in, return a failure result immediately.
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            // Prepare the data to upload as a map.
            // Each field from the local UserProfileEntity is added.
            // lastUpdated is set to the current system time to indicate the latest sync.
            val profileData = hashMapOf(
                "userId" to profile.userId,
                "username" to profile.username,
                "email" to profile.email,
                "avatarBase64" to profile.avatarBase64,
                "totalXP" to profile.totalXP,
                "level" to profile.level,
                "totalGamesPlayed" to profile.totalGamesPlayed,
                "gamesWon" to profile.gamesWon,
                "currentStreak" to profile.currentStreak,
                "bestStreak" to profile.bestStreak,
                "lastPlayDate" to profile.lastPlayDate,
                "averageCompletionTime" to profile.averageCompletionTime,
                "accuracyRate" to profile.accuracyRate,
                "createdAt" to profile.createdAt,
                "lastUpdated" to System.currentTimeMillis() // mark the profile as recently updated
            )

            // Upload the user profile to Firestore under "userProfiles" collection.
            // Use the user's UID as the document ID.
            // `SetOptions.merge()` ensures existing fields not included in this map are preserved.
            firestore.collection("userProfiles")
                .document(userId)
                .set(profileData, SetOptions.merge())
                .await() // Suspend coroutine until Firestore operation completes

            // Log success
            Log.d(TAG, "✅ User profile uploaded to Firestore")
            Result.success(Unit) // Return success

        } catch (e: Exception) {
            // Log any errors that occur during upload and return failure
            Log.e(TAG, "❌ Error uploading user profile: ${e.message}", e)
            Result.failure(e)
        }
    }


    /**
     * Download user profile from Firestore
     */
    /**
     * Downloads the current user's profile from Firestore.
     * Converts the Firestore document into a UserProfileEntity object.
     * If the profile does not exist, returns null.
     *
     * @return Result<UserProfileEntity?> A Result wrapping the UserProfileEntity if successful, or null if not found.
     */
    suspend fun downloadUserProfile(): Result<UserProfileEntity?> {
        return try {
            // Get the current logged-in user's UID from Firebase Auth.
            // If no user is logged in, return a failure result immediately.
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            // Fetch the Firestore document for the user's profile using their UID.
            val document = firestore.collection("userProfiles")
                .document(userId)
                .get()
                .await() // Suspend until Firestore operation completes

            // Check if the document exists
            if (document.exists()) {
                // Map Firestore fields to a UserProfileEntity object
                val profile = UserProfileEntity(
                    userId = document.getString("userId") ?: userId, // fallback to UID
                    username = document.getString("username") ?: "",  // default empty if missing
                    email = document.getString("email") ?: "",        // default empty if missing
                    avatarBase64 = document.getString("avatarBase64"), // optional avatar
                    totalXP = document.getLong("totalXP")?.toInt() ?: 0, // default 0 XP
                    level = document.getLong("level")?.toInt() ?: 1,      // default level 1
                    totalGamesPlayed = document.getLong("totalGamesPlayed")?.toInt() ?: 0,
                    gamesWon = document.getLong("gamesWon")?.toInt() ?: 0,
                    currentStreak = document.getLong("currentStreak")?.toInt() ?: 0,
                    bestStreak = document.getLong("bestStreak")?.toInt() ?: 0,
                    lastPlayDate = document.getLong("lastPlayDate") ?: 0L,
                    averageCompletionTime = document.getDouble("averageCompletionTime")?.toFloat() ?: 0f,
                    accuracyRate = document.getDouble("accuracyRate")?.toFloat() ?: 0f,
                    createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
                    lastUpdated = document.getLong("lastUpdated") ?: System.currentTimeMillis(),
                    isSynced = true // Mark as synced since this comes from Firestore
                )

                // Log success with key user stats
                Log.d(TAG, "✅ User profile downloaded: Streak ${profile.currentStreak}, Level ${profile.level}")
                Result.success(profile) // Return the downloaded profile

            } else {
                // Document does not exist in Firestore
                Log.d(TAG, "No user profile found in Firestore")
                Result.success(null) // Return null to indicate absence of profile
            }

        } catch (e: Exception) {
            // Catch and log any exception during Firestore read
            Log.e(TAG, "❌ Error downloading user profile: ${e.message}", e)
            Result.failure(e) // Return failure result with exception
        }
    }


    // ===== ACHIEVEMENTS SYNC =====

    /**
     * Upload achievements to Firestore
     */
    /**
     * Uploads a list of achievements for the currently logged-in user to Firestore.
     * Uses a batch operation to efficiently write multiple documents at once.
     *
     * @param achievements List of AchievementEntity objects to upload.
     * @return Result<Unit> Result indicating success or failure of the operation.
     */
    suspend fun uploadAchievements(achievements: List<AchievementEntity>): Result<Unit> {
        return try {
            // Get the current logged-in user's UID from Firebase Auth
            // If no user is logged in, return failure immediately
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // Create a Firestore batch for atomic writes
            val batch = firestore.batch()

            // Loop through each achievement and prepare it for upload
            achievements.forEach { achievement ->
                // Map AchievementEntity fields to a Firestore-friendly hash map
                val achievementData = hashMapOf(
                    "achievementId" to achievement.achievementId,
                    "userId" to achievement.userId,
                    "achievementType" to achievement.achievementType,
                    "name" to achievement.name,
                    "description" to achievement.description,
                    "iconName" to achievement.iconName,
                    "unlockedAt" to achievement.unlockedAt,
                    "progress" to achievement.progress,
                    "isUnlocked" to achievement.isUnlocked
                )

                // Get the document reference for this specific achievement
                // Stored under: "achievements/{userId}/userAchievements/{achievementId}"
                val docRef = firestore.collection("achievements")
                    .document(userId)
                    .collection("userAchievements")
                    .document(achievement.achievementId)

                // Add a set operation to the batch, using merge to update existing data
                batch.set(docRef, achievementData, SetOptions.merge())
            }

            // Commit the batch to Firestore
            // This will write all achievements in one atomic operation
            batch.commit().await()

            // Log success with the number of achievements uploaded
            Log.d(TAG, "✅ ${achievements.size} achievements uploaded to Firestore")
            Result.success(Unit) // Return success result

        } catch (e: Exception) {
            // Catch and log any exception during Firestore operations
            Log.e(TAG, "❌ Error uploading achievements: ${e.message}", e)
            Result.failure(e) // Return failure result with exception
        }
    }


    /**
     * Download achievements from Firestore
     */
    /**
     * Downloads all achievements for the currently logged-in user from Firestore.
     * Maps Firestore documents back into AchievementEntity objects.
     *
     * @return Result<List<AchievementEntity>> Result containing the list of achievements or failure.
     */
    suspend fun downloadAchievements(): Result<List<AchievementEntity>> {
        return try {
            // Get the current logged-in user's UID from Firebase Auth
            // If no user is logged in, return failure immediately
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // Fetch all documents in the user's "userAchievements" subcollection
            val snapshot = firestore.collection("achievements")
                .document(userId)
                .collection("userAchievements")
                .get() // Execute query to get documents
                .await() // Suspend until query is complete

            // Map each Firestore document into an AchievementEntity
            val achievements = snapshot.documents.mapNotNull { document ->
                try {
                    AchievementEntity(
                        achievementId = document.getString("achievementId") ?: return@mapNotNull null, // Skip if missing ID
                        userId = document.getString("userId") ?: userId, // Fallback to current userId
                        achievementType = document.getString("achievementType") ?: "", // Default empty string
                        name = document.getString("name") ?: "",
                        description = document.getString("description") ?: "",
                        iconName = document.getString("iconName") ?: "",
                        unlockedAt = document.getLong("unlockedAt") ?: 0L, // Default to 0 if not present
                        progress = document.getLong("progress")?.toInt() ?: 0, // Convert to Int, default 0
                        isUnlocked = document.getBoolean("isUnlocked") ?: false, // Default false
                        isSynced = true // Mark as synced after download
                    )
                } catch (e: Exception) {
                    // Log parsing errors for individual documents but continue with others
                    Log.e(TAG, "Error parsing achievement: ${e.message}")
                    null // Skip this document if error occurs
                }
            }

            // Log the number of achievements successfully downloaded
            Log.d(TAG, "✅ ${achievements.size} achievements downloaded from Firestore")

            // Return the list wrapped in Result.success
            Result.success(achievements)

        } catch (e: Exception) {
            // Catch any exceptions from Firestore operations
            Log.e(TAG, "❌ Error downloading achievements: ${e.message}", e)
            Result.failure(e) // Return failure with exception
        }
    }


    // ===== GAME RESULTS SYNC =====

    /**
     * Upload recent game results to Firestore (last 100)
     */
    /**
     * Uploads a list of game results for the currently logged-in user to Firestore.
     * Limits to the most recent 100 results to avoid excessive writes.
     *
     * @param results List of GameResultEntity to upload.
     * @return Result<Unit> indicating success or failure of the operation.
     */
    suspend fun uploadGameResults(results: List<GameResultEntity>): Result<Unit> {
        return try {
            // Get the currently logged-in user's UID from Firebase Auth
            // If no user is logged in, return a failure immediately
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // Create a batch operation to perform multiple writes in a single network call
            val batch = firestore.batch()

            // Take only the most recent 100 results to limit Firestore writes
            results.take(100).forEach { result ->

                // Map the GameResultEntity into a HashMap suitable for Firestore
                val resultData = hashMapOf(
                    "gameId" to result.gameId,
                    "userId" to result.userId,
                    "gameMode" to result.gameMode,
                    "theme" to result.theme,
                    "gridSize" to result.gridSize,
                    "difficulty" to result.difficulty,
                    "score" to result.score,
                    "timeTaken" to result.timeTaken,
                    "moves" to result.moves,
                    "accuracy" to result.accuracy,
                    "isWin" to result.isWin,
                    "completedAt" to result.completedAt
                )

                // Reference the Firestore document for this particular game result
                val docRef = firestore.collection("gameResults")  // Top-level collection
                    .document(userId)                             // Document per user
                    .collection("results")                        // Subcollection for user results
                    .document(result.gameId)                      // Document per game

                // Add the set operation to the batch, using merge to avoid overwriting existing data
                batch.set(docRef, resultData, SetOptions.merge())
            }

            // Commit all batched writes in a single network call
            batch.commit().await()

            // Log success with the number of results uploaded
            Log.d(TAG, "✅ ${results.take(100).size} game results uploaded to Firestore")

            // Return success
            Result.success(Unit)

        } catch (e: Exception) {
            // Log any errors that occur during the Firestore operation
            Log.e(TAG, "❌ Error uploading game results: ${e.message}", e)
            // Return failure with the exception
            Result.failure(e)
        }
    }


    /**
     * Download game results for the currently logged-in user from Firestore.
     * By default, limits to the most recent 100 results, ordered by completion time descending.
     *
     * @param limit The maximum number of results to download (default 100)
     * @return Result<List<GameResultEntity>> containing the downloaded results or an error
     */
    suspend fun downloadGameResults(limit: Int = 100): Result<List<GameResultEntity>> {
        return try {
            // Get the currently logged-in user's UID from Firebase Auth
            // If no user is logged in, return a failure immediately
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // Query Firestore for the user's game results, ordered by completion time descending
            // Apply the specified limit to avoid fetching too many documents
            val snapshot = firestore.collection("gameResults")          // Top-level collection for game results
                .document(userId)                                      // Each user has a separate document
                .collection("results")                                 // Subcollection storing individual game results
                .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING) // Most recent first
                .limit(limit.toLong())                                  // Limit number of results
                .get()                                                 // Fetch the documents
                .await()                                               // Suspend until fetch completes

            // Map the fetched Firestore documents to GameResultEntity objects
            val results = snapshot.documents.mapNotNull { document ->
                try {
                    GameResultEntity(
                        gameId = document.getString("gameId") ?: return@mapNotNull null, // Must have a gameId
                        userId = document.getString("userId") ?: userId,                 // Default to current userId
                        gameMode = document.getString("gameMode") ?: "",                  // Default empty string
                        theme = document.getString("theme") ?: "",
                        gridSize = document.getString("gridSize") ?: "",
                        difficulty = document.getString("difficulty") ?: "",
                        score = document.getLong("score")?.toInt() ?: 0,                 // Default 0
                        timeTaken = document.getLong("timeTaken")?.toInt() ?: 0,
                        moves = document.getLong("moves")?.toInt() ?: 0,
                        accuracy = document.getDouble("accuracy")?.toFloat() ?: 0f,
                        isWin = document.getBoolean("isWin") ?: false,
                        completedAt = document.getLong("completedAt") ?: System.currentTimeMillis(),
                        isSynced = true  // Mark as synced because this data comes from Firestore
                    )
                } catch (e: Exception) {
                    // Log any parsing errors but continue mapping remaining documents
                    Log.e(TAG, "Error parsing game result: ${e.message}")
                    null
                }
            }

            // Log success with the number of results downloaded
            Log.d(TAG, "✅ ${results.size} game results downloaded from Firestore")

            // Return success with the mapped results
            Result.success(results)

        } catch (e: Exception) {
            // Log any Firestore errors and return failure
            Log.e(TAG, "❌ Error downloading game results: ${e.message}", e)
            Result.failure(e)
        }
    }


    // ===== GAME PROGRESS SYNC (EXISTING) =====

    /**
     * Save the current game progress of the logged-in user to Firestore.
     * This includes the current level, level progress details, unlocked categories, and game stats.
     *
     * @param gameProgress The game progress object containing all progress data
     * @return Result<Unit> indicating success or failure
     */
    suspend fun saveGameProgress(gameProgress: GameProgress): Result<Unit> {
        return try {
            // Get the currently logged-in user's UID
            // If no user is logged in, immediately return a failure result
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // Transform the levelProgress map into a Firestore-friendly format
            // 1. Convert the keys to String (Firestore does not accept Int keys directly)
            // 2. Map each LevelProgress object to a HashMap of its fields
            val levelProgressMap = gameProgress.levelProgress.mapKeys { it.key.toString() }
                .mapValues { (_, levelData) ->
                    hashMapOf(
                        "levelNumber" to levelData.levelNumber,     // Level number
                        "stars" to levelData.stars,                 // Number of stars achieved
                        "bestScore" to levelData.bestScore,         // Best score for the level
                        "bestTime" to levelData.bestTime,           // Best time achieved
                        "bestMoves" to levelData.bestMoves,         // Fewest moves recorded
                        "isUnlocked" to levelData.isUnlocked,       // Whether the level is unlocked
                        "isCompleted" to levelData.isCompleted,     // Whether the level has been completed
                        "timesPlayed" to levelData.timesPlayed      // Number of times the level has been played
                    )
                }

            // Build the main progress data to store in Firestore
            val progressData = hashMapOf(
                "userId" to userId,                                // Store the user ID
                "currentLevel" to gameProgress.currentLevel,      // Current level the user is on
                "levelProgress" to levelProgressMap,              // Nested map of level progress
                "unlockedCategories" to gameProgress.unlockedCategories, // List of unlocked categories
                "totalGamesPlayed" to gameProgress.totalGamesPlayed,     // Total games played
                "gamesWon" to gameProgress.gamesWon,                     // Total games won
                "lastUpdated" to System.currentTimeMillis()             // Timestamp for last update
            )

            // Save the progress data to Firestore
            firestore.collection("gameProgress")     // Top-level collection for all users' progress
                .document(userId)                    // Document specific to this user
                .set(progressData, SetOptions.merge()) // Merge with existing data to avoid overwriting
                .await()                              // Suspend until operation completes

            // Log success
            Log.d(TAG, "✅ Game progress saved to Firestore")
            Result.success(Unit)                       // Return success result

        } catch (e: Exception) {
            // Log any error that occurred during saving and return failure
            Log.e(TAG, "❌ Error saving game progress: ${e.message}", e)
            Result.failure(e)
        }
    }


    /**
     * Load the current game progress of the logged-in user from Firestore.
     * This includes the current level, level progress, unlocked categories, and game stats.
     *
     * @return Result<GameProgress?> containing the loaded progress or null if not found
     */
    suspend fun loadGameProgress(): Result<GameProgress?> {
        return try {
            // Get the currently logged-in user's UID
            // If no user is logged in, immediately return a failure result
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // Fetch the user's progress document from Firestore
            val document = firestore.collection("gameProgress")
                .document(userId)  // Document specific to this user
                .get()
                .await()          // Suspend until fetch completes

            // Check if the document exists in Firestore
            if (document.exists()) {
                // Retrieve the nested levelProgress map from Firestore
                // It's stored as Map<String, Map<String, Any>> in Firestore
                val levelProgressFirestore = document.get("levelProgress") as? Map<String, Map<String, Any>> ?: emptyMap()

                // Convert Firestore keys back to Int and map values to LevelData objects
                val levelProgress = levelProgressFirestore.mapKeys { it.key.toInt() }  // Convert string keys to integers
                    .mapValues { (_, data) ->                                     // Convert nested map to LevelData
                        LevelData(
                            levelNumber = (data["levelNumber"] as? Long)?.toInt() ?: 0,
                            stars = (data["stars"] as? Long)?.toInt() ?: 0,
                            bestScore = (data["bestScore"] as? Long)?.toInt() ?: 0,
                            bestTime = (data["bestTime"] as? Long)?.toInt() ?: 0,
                            bestMoves = (data["bestMoves"] as? Long)?.toInt() ?: 0,
                            isUnlocked = data["isUnlocked"] as? Boolean ?: false,
                            isCompleted = data["isCompleted"] as? Boolean ?: false,
                            timesPlayed = (data["timesPlayed"] as? Long)?.toInt() ?: 0
                        )
                    }

                // Build the main GameProgress object from Firestore data
                val progress = GameProgress(
                    userId = document.getString("userId") ?: userId,  // Fallback to current userId
                    currentLevel = document.getLong("currentLevel")?.toInt() ?: 1,  // Default to level 1
                    levelProgress = levelProgress,                                       // Nested LevelData map
                    unlockedCategories = document.get("unlockedCategories") as? List<String> ?: listOf("Animals"),
                    totalGamesPlayed = document.getLong("totalGamesPlayed")?.toInt() ?: 0,
                    gamesWon = document.getLong("gamesWon")?.toInt() ?: 0,
                    lastUpdated = document.getLong("lastUpdated") ?: System.currentTimeMillis() // Timestamp fallback
                )

                // Log successful loading
                Log.d(TAG, "✅ Game progress loaded: Level ${progress.currentLevel}")
                Result.success(progress)  // Return the loaded progress

            } else {
                // Document does not exist: no saved progress found
                Log.d(TAG, "No saved progress found")
                Result.success(null)
            }

        } catch (e: Exception) {
            // Catch and log any exception that occurs while fetching progress
            Log.e(TAG, "❌ Error loading game progress: ${e.message}", e)
            Result.failure(e)
        }
    }
}