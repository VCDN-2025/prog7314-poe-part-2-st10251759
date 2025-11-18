package vcmsa.projects.prog7314.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.SettingsManager

/*
    Code Attribution for: Repositories
    ===================================================
    Android Developers, 2025. Data layer (Version unknown) [Source code].
    Available at: <https://developer.android.com/topic/architecture/data-layer>
    [Accessed 18 November 2025].

    Code Attribution for: Room DB Entities
    ===================================================
    Android Developers, 2020. Defining data using Room entities | Android Developers (Version unknown) [Source code].
    Available at: <https://developer.android.com/training/data-storage/room/defining-data>
    [Accessed 18 November 2025].

    Code Attribution for: Connecting to Firebase Database
    ===================================================
    Firebase, 2025. Installation & Setup on Android | Firebase Realtime Database (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/database/android/start>
    [Accessed 18 November 2025].

*/

/**
 * Repository responsible for syncing user settings between local storage and Firebase Firestore.
 * Handles both uploading and downloading settings, marking offline changes for later sync,
 * and applying downloaded settings to the local device.
 */
class SettingsSyncRepository(private val context: Context) {

    // Tag for logging
    private val TAG = "SettingsSyncRepo"

    // Firebase Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    // Firestore collection and field names
    private val COLLECTION_USERS = "users"
    private val FIELD_SETTINGS = "settings"

    // SharedPreferences for tracking sync state
    private val PREFS_SYNC = "settings_sync"
    private val KEY_NEEDS_SYNC = "needs_sync"
    private val KEY_LAST_SYNC = "last_sync_time"

    /**
     * Data class representing user settings to be synced with Firestore.
     * All settings include default values to ensure robustness.
     */
    data class UserSettings(
        val language: String = "en",
        val notificationsEnabled: Boolean = true,
        val dailyReminderEnabled: Boolean = true,
        val achievementAlertsEnabled: Boolean = true,
        val soundEffectsEnabled: Boolean = true,
        val backgroundMusicEnabled: Boolean = true,
        val cardBackground: String = "blue",
        val highContrastMode: Boolean = false,
        val biometricPreference: Boolean = false, // User preference only
        val lastUpdated: Long = System.currentTimeMillis() // Timestamp for last update
    )

    /**
     * Save all current user settings to Firestore.
     * Uses SettingsManager to read current local settings.
     * Marks settings as synced locally after successful upload.
     *
     * @return Result<Unit> indicating success or failure
     */
    suspend fun syncSettingsToFirestore(): Result<Unit> {
        return try {
            val userId = AuthManager.getCurrentUser()?.uid
            if (userId == null) {
                Log.w(TAG, "No user logged in, cannot sync settings")
                return Result.failure(Exception("No user logged in"))
            }

            // Gather current settings from local storage
            val settings = UserSettings(
                language = SettingsManager.getLanguage(context),
                notificationsEnabled = SettingsManager.isNotificationsEnabled(context),
                dailyReminderEnabled = SettingsManager.isDailyReminderEnabled(context),
                achievementAlertsEnabled = SettingsManager.isAchievementAlertsEnabled(context),
                soundEffectsEnabled = SettingsManager.isSoundEffectsEnabled(context),
                backgroundMusicEnabled = SettingsManager.isBackgroundMusicEnabled(context),
                cardBackground = SettingsManager.getCardBackground(context),
                highContrastMode = SettingsManager.isHighContrastMode(context),
                biometricPreference = vcmsa.projects.prog7314.utils.BiometricHelper.isBiometricEnabled(context),
                lastUpdated = System.currentTimeMillis()
            )

            // Upload settings to Firestore under user document
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(mapOf(FIELD_SETTINGS to settings), com.google.firebase.firestore.SetOptions.merge())
                .await()

            // Mark settings as successfully synced locally
            markAsSynced()

            Log.d(TAG, "✅ Settings synced to Firestore successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing settings to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Load settings from Firestore for the current user and apply them to local storage.
     * Falls back to local defaults if Firestore document does not exist or error occurs.
     *
     * @return Result<UserSettings> with loaded settings or error
     */
    suspend fun loadSettingsFromFirestore(): Result<UserSettings> {
        return try {
            val userId = AuthManager.getCurrentUser()?.uid
            if (userId == null) {
                Log.w(TAG, "No user logged in, cannot load settings")
                return Result.failure(Exception("No user logged in"))
            }

            // Fetch Firestore document for current user
            val document = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            // Attempt to parse settings map from Firestore
            val settingsMap = document.get(FIELD_SETTINGS) as? Map<*, *>

            if (settingsMap != null) {
                // Convert map to UserSettings data class, using defaults if missing
                val settings = UserSettings(
                    language = settingsMap["language"] as? String ?: "en",
                    notificationsEnabled = settingsMap["notificationsEnabled"] as? Boolean ?: true,
                    dailyReminderEnabled = settingsMap["dailyReminderEnabled"] as? Boolean ?: true,
                    achievementAlertsEnabled = settingsMap["achievementAlertsEnabled"] as? Boolean ?: true,
                    soundEffectsEnabled = settingsMap["soundEffectsEnabled"] as? Boolean ?: true,
                    backgroundMusicEnabled = settingsMap["backgroundMusicEnabled"] as? Boolean ?: true,
                    cardBackground = settingsMap["cardBackground"] as? String ?: "blue",
                    highContrastMode = settingsMap["highContrastMode"] as? Boolean ?: false,
                    biometricPreference = settingsMap["biometricPreference"] as? Boolean ?: false,
                    lastUpdated = (settingsMap["lastUpdated"] as? Long) ?: System.currentTimeMillis()
                )

                // Apply loaded settings locally
                applySettingsLocally(settings)

                Log.d(TAG, "✅ Settings loaded from Firestore and applied locally")
                return Result.success(settings)
            } else {
                Log.d(TAG, "No settings found in Firestore, using local defaults")
                return Result.failure(Exception("No settings in Firestore"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading settings from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Apply settings to local device storage using SettingsManager.
     * Biometric preference is applied but does not enable authentication automatically.
     */
    private fun applySettingsLocally(settings: UserSettings) {
        SettingsManager.setLanguage(context, settings.language)
        SettingsManager.setNotificationsEnabled(context, settings.notificationsEnabled)
        SettingsManager.setDailyReminderEnabled(context, settings.dailyReminderEnabled)
        SettingsManager.setAchievementAlertsEnabled(context, settings.achievementAlertsEnabled)
        SettingsManager.setSoundEffectsEnabled(context, settings.soundEffectsEnabled)
        SettingsManager.setBackgroundMusicEnabled(context, settings.backgroundMusicEnabled)
        SettingsManager.setCardBackground(context, settings.cardBackground)
        SettingsManager.setHighContrastMode(context, settings.highContrastMode)
        // Note: Biometric preference applied only
        Log.d(TAG, "Settings applied locally")
    }

    /**
     * Mark that settings need to be synced to Firestore (e.g., when offline).
     */
    fun markAsNeedingSync() {
        val prefs = context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NEEDS_SYNC, true).apply()
        Log.d(TAG, "⚠️ Settings marked as needing sync")
    }

    /**
     * Mark settings as synced successfully.
     * Updates both sync flag and last sync timestamp in SharedPreferences.
     */
    private fun markAsSynced() {
        val prefs = context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_NEEDS_SYNC, false)
            putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Check if settings need to be synced to Firestore.
     *
     * @return true if sync is required, false otherwise
     */
    fun needsSync(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NEEDS_SYNC, false)
    }

    /**
     * Get the timestamp of the last successful sync.
     *
     * @return Long representing last sync time in milliseconds
     */
    fun getLastSyncTime(): Long {
        val prefs = context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_SYNC, 0L)
    }

    /**
     * Handle a single setting change.
     * If online, immediately sync to Firestore.
     * If offline, mark for later sync.
     *
     * @param online Whether the device currently has internet connectivity
     */
    suspend fun onSettingChanged(online: Boolean = true) {
        if (online) {
            // Sync immediately if online
            syncSettingsToFirestore()
        } else {
            // Otherwise mark for later sync
            markAsNeedingSync()
            Log.d(TAG, "📴 Offline - Settings will sync when online")
        }
    }
}
