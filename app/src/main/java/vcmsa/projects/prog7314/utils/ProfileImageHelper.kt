package vcmsa.projects.prog7314.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Utility object for managing user profile images.
 * Handles compressing images to Base64, uploading to Firestore, and local caching.
 * Uses Base64 encoding to stay within Firestore's document size limits.
 */
object ProfileImageHelper {
    private const val TAG = "ProfileImageHelper"

    // Firestore collection and field names
    private const val COLLECTION_USERS = "users"
    private const val FIELD_AVATAR_BASE64 = "avatarBase64"

    // SharedPreferences key for local caching
    private const val PREFS_KEY = "profile_image_base64"

    /**
     * Saves a user's profile image to both Firestore and local storage.
     * The process:
     * 1. Compresses the image to fit within Firestore's size limits
     * 2. Converts to Base64 string for text-based storage
     * 3. Uploads to Firestore for cross-device sync
     * 4. Caches locally for faster loading
     *
     * Returns a Result containing the Base64 string on success, or an error on failure.
     */
    suspend fun saveProfileImage(context: Context, imageUri: Uri): Result<String> {
        return try {
            // Ensure user is logged in before saving
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            Log.d(TAG, "Starting image compression...")

            // Compress and convert image to Base64 format
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

            // Upload to Firestore cloud storage
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(
                    mapOf(FIELD_AVATAR_BASE64 to base64String),
                    com.google.firebase.firestore.SetOptions.merge() // Don't overwrite other user fields
                )
                .await()

            // Save a copy locally so the image loads instantly next time
            saveToLocalPrefs(context, base64String)

            Log.d(TAG, "✅ Profile image saved successfully")
            Result.success(base64String)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving profile image: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Loads the user's profile image from Firestore.
     * Returns the image as a Base64 encoded string, or null if no image is set.
     * The Base64 string can be decoded back to a Bitmap for display.
     *
     * This fetches from the cloud, so it may be slower than loading from local cache.
     */
    suspend fun loadProfileImageBase64(): Result<String?> {
        return try {
            // Ensure user is logged in
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            // Fetch user document from Firestore
            val firestore = FirebaseFirestore.getInstance()
            val document = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            // Extract the avatar field
            val avatarBase64 = document.getString(FIELD_AVATAR_BASE64)
            Log.d(TAG, "Profile image loaded: ${if (avatarBase64 != null) "Yes" else "No"}")

            Result.success(avatarBase64)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Legacy method for loading profile images.
     * Deprecated because the app no longer uses URI-based image storage.
     * All images are now stored as Base64 strings in Firestore.
     *
     * This method attempts to load Base64 format and returns null as a fallback.
     * Kept for backward compatibility with older app versions.
     */
    @Deprecated("Use loadProfileImageBase64 instead")
    suspend fun loadProfileImageUri(): Result<String?> {
        // Try the new Base64 method first
        val base64Result = loadProfileImageBase64()
        if (base64Result.isSuccess) {
            return base64Result
        }

        // No URI storage exists anymore - return null
        return Result.success(null)
    }

    /**
     * Saves the Base64 image string to local SharedPreferences.
     * This creates a local cache so the profile image loads instantly without
     * waiting for Firestore download, improving user experience.
     *
     * Private method called automatically when saving profile images.
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
     * Loads the profile image from local SharedPreferences cache.
     * This is much faster than loading from Firestore since it's stored on the device.
     * Returns null if no cached image exists.
     *
     * Use this for initial display, then sync from Firestore in the background.
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
     * Removes the user's profile image from both Firestore and local storage.
     * This resets the profile to the default avatar.
     * Called when user wants to remove their custom profile picture.
     *
     * Returns Result.success(Unit) if cleared successfully, or Result.failure on error.
     */
    suspend fun clearProfileImage(context: Context): Result<Unit> {
        return try {
            // Ensure user is logged in
            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            // Remove from Firestore cloud storage
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update(FIELD_AVATAR_BASE64, null)
                .await()

            // Remove from local cache
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