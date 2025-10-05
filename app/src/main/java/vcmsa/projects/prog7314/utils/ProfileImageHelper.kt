package vcmsa.projects.prog7314.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ProfileImageHelper {
    private const val TAG = "ProfileImageHelper"
    private const val COLLECTION_USERS = "users"
    private const val FIELD_AVATAR_BASE64 = "avatarBase64"
    private const val PREFS_KEY = "profile_image_base64"

    /**
     * Save profile image - compress to Base64 and save to Firestore
     */
    suspend fun saveProfileImage(context: Context, imageUri: Uri): Result<String> {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            Log.d(TAG, "Starting image compression...")

            // Compress image to Base64
            val compressionResult = ImageCompressionHelper.compressImageToBase64(context, imageUri)

            if (compressionResult.isFailure) {
                return Result.failure(
                    compressionResult.exceptionOrNull()
                        ?: Exception("Image compression failed")
                )
            }

            val base64String = compressionResult.getOrNull()
                ?: return Result.failure(Exception("Compression returned null"))

            Log.d(TAG, "Image compressed. Saving to Firestore...")

            // Save to Firestore
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(
                    mapOf(FIELD_AVATAR_BASE64 to base64String),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()

            // Save locally for faster access
            saveToLocalPrefs(context, base64String)

            Log.d(TAG, "✅ Profile image saved successfully")
            Result.success(base64String)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving profile image: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Load profile image Base64 from Firestore
     */
    suspend fun loadProfileImageBase64(): Result<String?> {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val firestore = FirebaseFirestore.getInstance()
            val document = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            val avatarBase64 = document.getString(FIELD_AVATAR_BASE64)
            Log.d(TAG, "Profile image loaded: ${if (avatarBase64 != null) "Yes" else "No"}")

            Result.success(avatarBase64)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Load profile image URI (for backward compatibility - no longer used)
     */
    @Deprecated("Use loadProfileImageBase64 instead")
    suspend fun loadProfileImageUri(): Result<String?> {
        // Try to load from Firestore first
        val base64Result = loadProfileImageBase64()
        if (base64Result.isSuccess) {
            return base64Result
        }

        // Fallback: Return null (no URI-based storage anymore)
        return Result.success(null)
    }

    /**
     * Save Base64 to local SharedPreferences for faster loading
     */
    private fun saveToLocalPrefs(context: Context, base64String: String) {
        try {
            val prefs = context.getSharedPreferences("ProfileImagePrefs", Context.MODE_PRIVATE)
            prefs.edit().putString(PREFS_KEY, base64String).apply()
            Log.d(TAG, "Saved to local prefs")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to local prefs", e)
        }
    }

    /**
     * Load Base64 from local SharedPreferences
     */
    fun loadFromLocalPrefs(context: Context): String? {
        return try {
            val prefs = context.getSharedPreferences("ProfileImagePrefs", Context.MODE_PRIVATE)
            prefs.getString(PREFS_KEY, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading from local prefs", e)
            null
        }
    }

    /**
     * Clear profile image from Firestore and local storage
     */
    suspend fun clearProfileImage(context: Context): Result<Unit> {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            // Clear from Firestore
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update(FIELD_AVATAR_BASE64, null)
                .await()

            // Clear local storage
            val prefs = context.getSharedPreferences("ProfileImagePrefs", Context.MODE_PRIVATE)
            prefs.edit().remove(PREFS_KEY).apply()

            Log.d(TAG, "Profile image cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing profile image", e)
            Result.failure(e)
        }
    }
}