package vcmsa.projects.prog7314.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import vcmsa.projects.prog7314.data.models.CardBackground
import vcmsa.projects.prog7314.utils.AuthManager

object CardBackgroundRepository {
    private const val TAG = "CardBackgroundRepo"
    private const val COLLECTION_USERS = "users"
    private const val FIELD_CARD_BACKGROUND = "cardBackground"
    private const val PREFS_NAME = "card_background_prefs"
    private const val KEY_CARD_BACKGROUND = "selected_card_background"

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Save card background preference to both Firestore and local storage
     */
    suspend fun saveCardBackground(context: Context, cardBackground: CardBackground): Result<Unit> {
        return try {
            // Save locally first
            saveToLocalStorage(context, cardBackground)

            // Then sync to Firestore
            val userId = AuthManager.getCurrentUser()?.uid
            if (userId != null) {
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .set(mapOf(FIELD_CARD_BACKGROUND to cardBackground.name),
                        com.google.firebase.firestore.SetOptions.merge())
                    .await()
                Log.d(TAG, "Card background saved to Firestore: ${cardBackground.name}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving card background", e)
            Result.failure(e)
        }
    }

    /**
     * Load card background preference from Firestore, fallback to local storage
     */
    suspend fun loadCardBackground(context: Context): CardBackground {
        return try {
            val userId = AuthManager.getCurrentUser()?.uid
            if (userId != null) {
                val document = firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .await()

                val backgroundString = document.getString(FIELD_CARD_BACKGROUND)
                if (backgroundString != null) {
                    val background = CardBackground.fromString(backgroundString)
                    // Update local storage with Firestore value
                    saveToLocalStorage(context, background)
                    Log.d(TAG, "Card background loaded from Firestore: $backgroundString")
                    return background
                }
            }

            // Fallback to local storage
            loadFromLocalStorage(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading card background from Firestore", e)
            loadFromLocalStorage(context)
        }
    }

    /**
     * Save to local SharedPreferences
     */
    private fun saveToLocalStorage(context: Context, cardBackground: CardBackground) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CARD_BACKGROUND, cardBackground.name).apply()
    }

    /**
     * Load from local SharedPreferences
     */
    private fun loadFromLocalStorage(context: Context): CardBackground {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedBackground = prefs.getString(KEY_CARD_BACKGROUND, CardBackground.DEFAULT.name)
        return CardBackground.fromString(savedBackground ?: CardBackground.DEFAULT.name)
    }

    /**
     * Get drawable resource ID for current card background
     */
    fun getCardBackgroundDrawable(context: Context, cardBackground: CardBackground): Int {
        return context.resources.getIdentifier(
            cardBackground.drawableRes,
            "drawable",
            context.packageName
        )
    }
}