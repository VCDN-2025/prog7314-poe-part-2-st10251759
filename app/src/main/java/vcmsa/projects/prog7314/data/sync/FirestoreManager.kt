package vcmsa.projects.prog7314.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import vcmsa.projects.prog7314.data.models.GameProgress
import vcmsa.projects.prog7314.data.models.LevelData

class FirestoreManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val TAG = "FirestoreManager"

    // Save game progress to Firestore
    suspend fun saveGameProgress(gameProgress: GameProgress): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // Convert level progress map to a format Firestore can handle
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
                .set(progressData)
                .await()

            Log.d(TAG, "Game progress saved successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error saving game progress", e)
            Result.failure(e)
        }
    }

    // Load game progress from Firestore
    suspend fun loadGameProgress(): Result<GameProgress?> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val document = firestore.collection("gameProgress")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                // Convert Firestore map back to LevelData objects
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
                Log.d(TAG, "Game progress loaded: Level ${progress.currentLevel}, ${progress.levelProgress.size} levels tracked")
                Result.success(progress)
            } else {
                Log.d(TAG, "No saved progress found")
                Result.success(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading game progress", e)
            Result.failure(e)
        }
    }
}