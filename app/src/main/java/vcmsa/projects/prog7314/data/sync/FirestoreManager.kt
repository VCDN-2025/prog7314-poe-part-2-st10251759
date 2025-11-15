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

class FirestoreManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val TAG = "FirestoreManager"

    // ===== USER PROFILE SYNC =====

    /**
     * Upload user profile to Firestore
     */
    suspend fun uploadUserProfile(profile: UserProfileEntity): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

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
                "lastUpdated" to System.currentTimeMillis()
            )

            firestore.collection("userProfiles")
                .document(userId)
                .set(profileData, SetOptions.merge())
                .await()

            Log.d(TAG, "✅ User profile uploaded to Firestore")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error uploading user profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Download user profile from Firestore
     */
    suspend fun downloadUserProfile(): Result<UserProfileEntity?> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val document = firestore.collection("userProfiles")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val profile = UserProfileEntity(
                    userId = document.getString("userId") ?: userId,
                    username = document.getString("username") ?: "",
                    email = document.getString("email") ?: "",
                    avatarBase64 = document.getString("avatarBase64"),
                    totalXP = document.getLong("totalXP")?.toInt() ?: 0,
                    level = document.getLong("level")?.toInt() ?: 1,
                    totalGamesPlayed = document.getLong("totalGamesPlayed")?.toInt() ?: 0,
                    gamesWon = document.getLong("gamesWon")?.toInt() ?: 0,
                    currentStreak = document.getLong("currentStreak")?.toInt() ?: 0,
                    bestStreak = document.getLong("bestStreak")?.toInt() ?: 0,
                    lastPlayDate = document.getLong("lastPlayDate") ?: 0L,
                    averageCompletionTime = document.getDouble("averageCompletionTime")?.toFloat() ?: 0f,
                    accuracyRate = document.getDouble("accuracyRate")?.toFloat() ?: 0f,
                    createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
                    lastUpdated = document.getLong("lastUpdated") ?: System.currentTimeMillis(),
                    isSynced = true
                )

                Log.d(TAG, "✅ User profile downloaded: Streak ${profile.currentStreak}, Level ${profile.level}")
                Result.success(profile)
            } else {
                Log.d(TAG, "No user profile found in Firestore")
                Result.success(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error downloading user profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ===== ACHIEVEMENTS SYNC =====

    /**
     * Upload achievements to Firestore
     */
    suspend fun uploadAchievements(achievements: List<AchievementEntity>): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val batch = firestore.batch()

            achievements.forEach { achievement ->
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

                val docRef = firestore.collection("achievements")
                    .document(userId)
                    .collection("userAchievements")
                    .document(achievement.achievementId)

                batch.set(docRef, achievementData, SetOptions.merge())
            }

            batch.commit().await()

            Log.d(TAG, "✅ ${achievements.size} achievements uploaded to Firestore")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error uploading achievements: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Download achievements from Firestore
     */
    suspend fun downloadAchievements(): Result<List<AchievementEntity>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val snapshot = firestore.collection("achievements")
                .document(userId)
                .collection("userAchievements")
                .get()
                .await()

            val achievements = snapshot.documents.mapNotNull { document ->
                try {
                    AchievementEntity(
                        achievementId = document.getString("achievementId") ?: return@mapNotNull null,
                        userId = document.getString("userId") ?: userId,
                        achievementType = document.getString("achievementType") ?: "",
                        name = document.getString("name") ?: "",
                        description = document.getString("description") ?: "",
                        iconName = document.getString("iconName") ?: "",
                        unlockedAt = document.getLong("unlockedAt") ?: 0L,
                        progress = document.getLong("progress")?.toInt() ?: 0,
                        isUnlocked = document.getBoolean("isUnlocked") ?: false,
                        isSynced = true
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing achievement: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "✅ ${achievements.size} achievements downloaded from Firestore")
            Result.success(achievements)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error downloading achievements: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ===== GAME RESULTS SYNC =====

    /**
     * Upload recent game results to Firestore (last 100)
     */
    suspend fun uploadGameResults(results: List<GameResultEntity>): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val batch = firestore.batch()

            // Only upload recent results (last 100)
            results.take(100).forEach { result ->
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

                val docRef = firestore.collection("gameResults")
                    .document(userId)
                    .collection("results")
                    .document(result.gameId)

                batch.set(docRef, resultData, SetOptions.merge())
            }

            batch.commit().await()

            Log.d(TAG, "✅ ${results.take(100).size} game results uploaded to Firestore")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error uploading game results: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Download game results from Firestore
     */
    suspend fun downloadGameResults(limit: Int = 100): Result<List<GameResultEntity>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val snapshot = firestore.collection("gameResults")
                .document(userId)
                .collection("results")
                .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val results = snapshot.documents.mapNotNull { document ->
                try {
                    GameResultEntity(
                        gameId = document.getString("gameId") ?: return@mapNotNull null,
                        userId = document.getString("userId") ?: userId,
                        gameMode = document.getString("gameMode") ?: "",
                        theme = document.getString("theme") ?: "",
                        gridSize = document.getString("gridSize") ?: "",
                        difficulty = document.getString("difficulty") ?: "",
                        score = document.getLong("score")?.toInt() ?: 0,
                        timeTaken = document.getLong("timeTaken")?.toInt() ?: 0,
                        moves = document.getLong("moves")?.toInt() ?: 0,
                        accuracy = document.getDouble("accuracy")?.toFloat() ?: 0f,
                        isWin = document.getBoolean("isWin") ?: false,
                        completedAt = document.getLong("completedAt") ?: System.currentTimeMillis(),
                        isSynced = true
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing game result: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "✅ ${results.size} game results downloaded from Firestore")
            Result.success(results)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error downloading game results: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ===== GAME PROGRESS SYNC (EXISTING) =====

    /**
     * Save game progress to Firestore
     */
    suspend fun saveGameProgress(gameProgress: GameProgress): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val levelProgressMap = gameProgress.levelProgress.mapKeys { it.key.toString() }
                .mapValues { (_, levelData) ->
                    hashMapOf(
                        "levelNumber" to levelData.levelNumber,
                        "stars" to levelData.stars,
                        "bestScore" to levelData.bestScore,
                        "bestTime" to levelData.bestTime,
                        "bestMoves" to levelData.bestMoves,
                        "isUnlocked" to levelData.isUnlocked,
                        "isCompleted" to levelData.isCompleted,
                        "timesPlayed" to levelData.timesPlayed
                    )
                }

            val progressData = hashMapOf(
                "userId" to userId,
                "currentLevel" to gameProgress.currentLevel,
                "levelProgress" to levelProgressMap,
                "unlockedCategories" to gameProgress.unlockedCategories,
                "totalGamesPlayed" to gameProgress.totalGamesPlayed,
                "gamesWon" to gameProgress.gamesWon,
                "lastUpdated" to System.currentTimeMillis()
            )

            firestore.collection("gameProgress")
                .document(userId)
                .set(progressData, SetOptions.merge())
                .await()

            Log.d(TAG, "✅ Game progress saved to Firestore")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving game progress: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Load game progress from Firestore
     */
    suspend fun loadGameProgress(): Result<GameProgress?> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val document = firestore.collection("gameProgress")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val levelProgressFirestore = document.get("levelProgress") as? Map<String, Map<String, Any>> ?: emptyMap()
                val levelProgress = levelProgressFirestore.mapKeys { it.key.toInt() }
                    .mapValues { (_, data) ->
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

                val progress = GameProgress(
                    userId = document.getString("userId") ?: userId,
                    currentLevel = document.getLong("currentLevel")?.toInt() ?: 1,
                    levelProgress = levelProgress,
                    unlockedCategories = document.get("unlockedCategories") as? List<String> ?: listOf("Animals"),
                    totalGamesPlayed = document.getLong("totalGamesPlayed")?.toInt() ?: 0,
                    gamesWon = document.getLong("gamesWon")?.toInt() ?: 0,
                    lastUpdated = document.getLong("lastUpdated") ?: System.currentTimeMillis()
                )
                Log.d(TAG, "✅ Game progress loaded: Level ${progress.currentLevel}")
                Result.success(progress)
            } else {
                Log.d(TAG, "No saved progress found")
                Result.success(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading game progress: ${e.message}", e)
            Result.failure(e)
        }
    }
}