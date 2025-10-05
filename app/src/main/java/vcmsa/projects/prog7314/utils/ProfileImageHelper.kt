package vcmsa.projects.prog7314.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ProfileImageHelper {
    private const val TAG = "ProfileImageHelper"
    private const val PREFS_KEY = "profile_image_uri"

    /**
     * Save profile image URI locally and to Firestore
     */
    suspend fun saveProfileImage(context: Context, imageUri: Uri): Result<String> {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val uriString = imageUri.toString()

            // Save locally
            SettingsManager.setProfileImageUri(context, uriString)

            // Save to Firestore
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users").document(userId)
                .set(mapOf("avatarUrl" to uriString), com.google.firebase.firestore.SetOptions.merge())
                .await()

            Log.d(TAG, "✅ Profile image saved")
            Result.success(uriString)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving profile image: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Load profile image URI from Firestore
     */
    suspend fun loadProfileImageUri(): Result<String?> {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val firestore = FirebaseFirestore.getInstance()
            val document = firestore.collection("users").document(userId).get().await()

            val avatarUrl = document.getString("avatarUrl")
            Log.d(TAG, "Profile image URI: $avatarUrl")

            Result.success(avatarUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image: ${e.message}", e)
            Result.failure(e)
        }
    }
}