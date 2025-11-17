package vcmsa.projects.prog7314.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.SettingsManager

class SettingsSyncRepository(private val context: Context) {

    private val TAG = "SettingsSyncRepo"
    private val firestore = FirebaseFirestore.getInstance()
    private val COLLECTION_USERS = "users"
    private val FIELD_SETTINGS = "settings"

    // Track if settings need sync
    private val PREFS_SYNC = "settings_sync"
    private val KEY_NEEDS_SYNC = "needs_sync"
    private val KEY_LAST_SYNC = "last_sync_time"

    /**
     * Data class for settings that will be synced to Firestore
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
        val biometricPreference: Boolean = false, // Preference only, not actual biometric data
        val lastUpdated: Long = System.currentTimeMillis()
    )

    /**
     * Save all current settings to Firestore
     */
    suspend fun syncSettingsToFirestore(): Result<Unit> {
        return try {
            val userId = AuthManager.getCurrentUser()?.uid
            if (userId == null) {
                Log.w(TAG, "No user logged in, cannot sync settings")
                return Result.failure(Exception("No user logged in"))
            }

            // Gather current settings from SettingsManager
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

            // Save to Firestore
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(mapOf(FIELD_SETTINGS to settings), com.google.firebase.firestore.SetOptions.merge())
                .await()

            // Mark as synced
            markAsSynced()

            Log.d(TAG, "✅ Settings synced to Firestore successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing settings to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Load settings from Firestore and apply to local storage
     */
    suspend fun loadSettingsFromFirestore(): Result<UserSettings> {
        return try {
            val userId = AuthManager.getCurrentUser()?.uid
            if (userId == null) {
                Log.w(TAG, "No user logged in, cannot load settings")
                return Result.failure(Exception("No user logged in"))
            }

            val document = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            val settingsMap = document.get(FIELD_SETTINGS) as? Map<*, *>

            if (settingsMap != null) {
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

                // Apply settings to local storage
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
     * Apply downloaded settings to local storage
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
        // Note: We apply the biometric preference but don't enable biometric auth automatically
        // The user will need to set up biometric on the new device

        Log.d(TAG, "Settings applied locally")
    }

    /**
     * Mark that settings need to be synced (when offline)
     */
    fun markAsNeedingSync() {
        val prefs = context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NEEDS_SYNC, true).apply()
        Log.d(TAG, "⚠️ Settings marked as needing sync")
    }

    /**
     * Mark settings as synced
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
     * Check if settings need to be synced
     */
    fun needsSync(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NEEDS_SYNC, false)
    }

    /**
     * Get last sync time
     */
    fun getLastSyncTime(): Long {
        val prefs = context.getSharedPreferences(PREFS_SYNC, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_SYNC, 0L)
    }

    /**
     * Save a single setting and mark for sync
     * Call this whenever a setting changes
     */
    suspend fun onSettingChanged(online: Boolean = true) {
        if (online) {
            // If online, sync immediately
            syncSettingsToFirestore()
        } else {
            // If offline, mark for later sync
            markAsNeedingSync()
            Log.d(TAG, "📴 Offline - Settings will sync when online")
        }
    }
}