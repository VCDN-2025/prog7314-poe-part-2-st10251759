package vcmsa.projects.prog7314.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import vcmsa.projects.prog7314.data.models.CardBackground
import vcmsa.projects.prog7314.utils.AuthManager

/*
    Code Attribution for: Repositories
    ===================================================
    Android Developers, 2025. Data layer (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/architecture/data-layer>
    [Accessed 18 November 2025].
*/

/**
 * Singleton repository responsible for managing the user's card background preferences.
 *
 * Handles saving and loading card backgrounds to:
 * 1. Local SharedPreferences for offline access.
 * 2. Firebase Firestore for cloud sync across devices.
 *
 * Provides helper functions to retrieve the drawable resource ID associated with a selected background.
 */
object CardBackgroundRepository {

    // Tag for logging purposes
    private const val TAG = "CardBackgroundRepo"

    // Firestore collection for user documents
    private const val COLLECTION_USERS = "users"

    // Field name in Firestore for storing card background preference
    private const val FIELD_CARD_BACKGROUND = "cardBackground"

    // SharedPreferences file name for local storage of card background
    private const val PREFS_NAME = "card_background_prefs"

    // Key for storing the selected card background in SharedPreferences
    private const val KEY_CARD_BACKGROUND = "selected_card_background"

    // Firestore instance for cloud operations
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Save the selected card background preference to both local storage and Firestore.
     *
     * Saving locally first ensures the preference is immediately available offline.
     * Then it attempts to sync with Firestore to persist across devices for the logged-in user.
     *
     * @param context Context needed for SharedPreferences access.
     * @param cardBackground The selected CardBackground object to save.
     * @return Result<Unit> indicating success or failure. On success, Unit is returned.
     */
    suspend fun saveCardBackground(context: Context, cardBackground: CardBackground): Result<Unit> {
        return try {
            // Save the preference locally in SharedPreferences
            saveToLocalStorage(context, cardBackground)

            // Retrieve the current user ID from AuthManager
            val userId = AuthManager.getCurrentUser()?.uid
            if (userId != null) {
                // Save the preference in Firestore under the user's document
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .set(
                        mapOf(FIELD_CARD_BACKGROUND to cardBackground.name),
                        com.google.firebase.firestore.SetOptions.merge() // Merge with existing fields
                    )
                    .await() // Await completion of Firestore operation
                Log.d(TAG, "Card background saved to Firestore: ${cardBackground.name}")
            }

            // Return success result
            Result.success(Unit)
        } catch (e: Exception) {
            // Log error and return failure result
            Log.e(TAG, "Error saving card background", e)
            Result.failure(e)
        }
    }

    /**
     * Load the user's card background preference.
     *
     * Attempts to fetch the preference from Firestore first. If unavailable or an error occurs,
     * it falls back to local SharedPreferences.
     *
     * Updates local storage with Firestore value if successful, keeping local copy consistent.
     *
     * @param context Context for SharedPreferences access.
     * @return The CardBackground object representing the current preference.
     */
    suspend fun loadCardBackground(context: Context): CardBackground {
        return try {
            // Retrieve current user ID
            val userId = AuthManager.getCurrentUser()?.uid
            if (userId != null) {
                // Fetch the user's document from Firestore
                val document = firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .await() // Await the asynchronous Firestore call

                // Extract card background string from the document
                val backgroundString = document.getString(FIELD_CARD_BACKGROUND)
                if (backgroundString != null) {
                    // Convert string to CardBackground enum
                    val background = CardBackground.fromString(backgroundString)
                    // Update local storage to reflect Firestore value
                    saveToLocalStorage(context, background)
                    Log.d(TAG, "Card background loaded from Firestore: $backgroundString")
                    return background
                }
            }

            // Fallback: Load from local SharedPreferences if Firestore value not available
            loadFromLocalStorage(context)
        } catch (e: Exception) {
            // Log error and fallback to local storage
            Log.e(TAG, "Error loading card background from Firestore", e)
            loadFromLocalStorage(context)
        }
    }

    /**
     * Save the card background to local SharedPreferences.
     *
     * Ensures immediate availability and offline persistence of user preference.
     *
     * @param context Context needed to access SharedPreferences.
     * @param cardBackground The CardBackground object to save locally.
     */
    private fun saveToLocalStorage(context: Context, cardBackground: CardBackground) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CARD_BACKGROUND, cardBackground.name) // Store enum name as string
            .apply() // Apply changes asynchronously
    }

    /**
     * Load the card background from local SharedPreferences.
     *
     * Provides offline access if Firestore sync fails or user is offline.
     *
     * @param context Context needed to access SharedPreferences.
     * @return The CardBackground object representing the locally saved preference.
     *         Defaults to CardBackground.DEFAULT if no preference exists.
     */
    private fun loadFromLocalStorage(context: Context): CardBackground {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedBackground = prefs.getString(KEY_CARD_BACKGROUND, CardBackground.DEFAULT.name)
        return CardBackground.fromString(savedBackground ?: CardBackground.DEFAULT.name)
    }

    /**
     * Retrieve the drawable resource ID associated with a CardBackground.
     *
     * This allows the UI to render the selected background visually.
     *
     * @param context Context required to access resources.
     * @param cardBackground The CardBackground object whose drawable is requested.
     * @return The integer resource ID for the drawable, suitable for setting in ImageViews.
     */
    fun getCardBackgroundDrawable(context: Context, cardBackground: CardBackground): Int {
        return context.resources.getIdentifier(
            cardBackground.drawableRes, // Name of the drawable resource
            "drawable", // Resource type
            context.packageName // App package
        )
    }
}
