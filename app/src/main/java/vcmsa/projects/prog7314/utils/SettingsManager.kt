package vcmsa.projects.prog7314.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.repository.SettingsSyncRepository

/**
 * Centralized manager for all app settings and user preferences.
 * Stores settings locally using SharedPreferences and syncs them to Firestore.
 * Provides getter and setter methods for all configurable app options.
 */
object SettingsManager {

    private const val TAG = "SettingsManager"

    // Name of the SharedPreferences file where settings are stored
    private const val PREFS_NAME = "AppSettings"

    // SharedPreferences keys for each setting
    private const val KEY_LANGUAGE = "language_preference"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_DAILY_REMINDER = "daily_reminder_enabled"
    private const val KEY_ACHIEVEMENT_ALERTS = "achievement_alerts_enabled"
    private const val KEY_SOUND_EFFECTS = "sound_effects_enabled"
    private const val KEY_BACKGROUND_MUSIC = "background_music_enabled"
    private const val KEY_CARD_BACKGROUND = "card_background_theme"
    private const val KEY_HIGH_CONTRAST = "high_contrast_mode"
    private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"

    // Default values used on first launch or after reset
    private const val DEFAULT_LANGUAGE = "en"
    private const val DEFAULT_NOTIFICATIONS = true
    private const val DEFAULT_DAILY_REMINDER = true
    private const val DEFAULT_ACHIEVEMENT_ALERTS = true
    private const val DEFAULT_SOUND_EFFECTS = true
    private const val DEFAULT_BACKGROUND_MUSIC = true
    private const val DEFAULT_CARD_BACKGROUND = "blue"
    private const val DEFAULT_HIGH_CONTRAST = false

    /**
     * Gets the SharedPreferences instance where all settings are stored.
     * SharedPreferences provides simple key-value storage that persists across app restarts.
     * Private helper method used by all setting getters and setters.
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Triggers a background sync of settings to Firestore cloud storage.
     * This allows settings to be shared across multiple devices.
     * Called automatically after any setting change to keep cloud and local in sync.
     * Runs on a background thread to avoid blocking the UI.
     */
    private fun triggerSync(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val syncRepo = SettingsSyncRepository(context)
                val isOnline = NetworkManager.isNetworkAvailable()
                syncRepo.onSettingChanged(isOnline)
            } catch (e: Exception) {
                Log.e(TAG, "Error triggering settings sync: ${e.message}", e)
            }
        }
    }

    // ===== PROFILE IMAGE SETTINGS =====

    /**
     * Saves the URI path of the user's profile image.
     * Note: Profile images are device-specific and not synced to cloud.
     * This is because images are large and stored separately in Firestore.
     */
    fun setProfileImageUri(context: Context, uriString: String?) {
        getPreferences(context).edit().putString(KEY_PROFILE_IMAGE_URI, uriString).apply()
        Log.d(TAG, "Profile image URI saved: $uriString")
        // Profile images are device-specific, don't sync
    }

    /**
     * Retrieves the saved profile image URI path.
     * Returns null if no profile image is set.
     */
    fun getProfileImageUri(context: Context): String? {
        return getPreferences(context).getString(KEY_PROFILE_IMAGE_URI, null)
    }

    /**
     * Removes the saved profile image URI, resetting to default avatar.
     */
    fun clearProfileImage(context: Context) {
        getPreferences(context).edit().remove(KEY_PROFILE_IMAGE_URI).apply()
        Log.d(TAG, "Profile image cleared")
    }

    // ===== LANGUAGE SETTINGS =====

    /**
     * Sets the app's display language.
     * Accepts language codes like "en" (English), "zu" (Zulu), "af" (Afrikaans).
     * The change syncs to cloud so it applies across all user devices.
     */
    fun setLanguage(context: Context, language: String) {
        getPreferences(context).edit().putString(KEY_LANGUAGE, language).apply()
        Log.d(TAG, "Language set to: $language")
        triggerSync(context) // Sync to cloud
    }

    /**
     * Gets the currently selected language code.
     * Returns the default language (English) if none is set.
     */
    fun getLanguage(context: Context): String {
        return getPreferences(context).getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    // ===== NOTIFICATION SETTINGS =====

    /**
     * Enables or disables all notifications in the app.
     * This is the master switch that controls all notification types.
     * When disabled, no notifications will be shown regardless of other settings.
     */
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        Log.d(TAG, "Notifications enabled: $enabled")
        triggerSync(context) // Sync to cloud
    }

    /**
     * Checks if notifications are currently enabled.
     * Returns true by default if the setting has never been changed.
     */
    fun isNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS)
    }

    /**
     * Enables or disables daily reminder notifications.
     * These reminders encourage users to maintain their play streak.
     */
    fun setDailyReminderEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_DAILY_REMINDER, enabled).apply()
        Log.d(TAG, "Daily reminder enabled: $enabled")
        triggerSync(context) // Sync to cloud
    }

    /**
     * Checks if daily reminder notifications are enabled.
     */
    fun isDailyReminderEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_DAILY_REMINDER, DEFAULT_DAILY_REMINDER)
    }

    /**
     * Enables or disables achievement unlock notifications.
     * When enabled, users get notified immediately when they unlock achievements.
     */
    fun setAchievementAlertsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_ACHIEVEMENT_ALERTS, enabled).apply()
        Log.d(TAG, "Achievement alerts enabled: $enabled")
        triggerSync(context) // Sync to cloud
    }

    /**
     * Checks if achievement alert notifications are enabled.
     */
    fun isAchievementAlertsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_ACHIEVEMENT_ALERTS, DEFAULT_ACHIEVEMENT_ALERTS)
    }

    // ===== AUDIO SETTINGS =====

    /**
     * Enables or disables sound effects during gameplay.
     * Sound effects include card flip sounds, match sounds, and button clicks.
     */
    fun setSoundEffectsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_SOUND_EFFECTS, enabled).apply()
        Log.d(TAG, "Sound effects enabled: $enabled")
        triggerSync(context) // Sync to cloud
    }

    /**
     * Checks if sound effects are enabled.
     */
    fun isSoundEffectsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_SOUND_EFFECTS, DEFAULT_SOUND_EFFECTS)
    }

    /**
     * Enables or disables background music during gameplay.
     * Background music plays continuously while the user is in the game.
     */
    fun setBackgroundMusicEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_BACKGROUND_MUSIC, enabled).apply()
        Log.d(TAG, "Background music enabled: $enabled")
        triggerSync(context) // Sync to cloud
    }

    /**
     * Checks if background music is enabled.
     */
    fun isBackgroundMusicEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_BACKGROUND_MUSIC, DEFAULT_BACKGROUND_MUSIC)
    }

    // ===== APPEARANCE SETTINGS =====

    /**
     * Sets the visual theme for the card backs.
     * Accepts theme names like "blue", "red", "green", etc.
     * This allows users to customize the look of the game cards.
     */
    fun setCardBackground(context: Context, theme: String) {
        getPreferences(context).edit().putString(KEY_CARD_BACKGROUND, theme).apply()
        Log.d(TAG, "Card background set to: $theme")
        triggerSync(context) // Sync to cloud
    }

    /**
     * Gets the currently selected card background theme.
     * Returns "blue" as the default if no theme is set.
     */
    fun getCardBackground(context: Context): String {
        return getPreferences(context).getString(KEY_CARD_BACKGROUND, DEFAULT_CARD_BACKGROUND)
            ?: DEFAULT_CARD_BACKGROUND
    }

    /**
     * Enables or disables high contrast mode for accessibility.
     * High contrast mode makes colors more distinct for users with vision difficulties.
     */
    fun setHighContrastMode(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
        Log.d(TAG, "High contrast mode: $enabled")
        triggerSync(context) // Sync to cloud
    }

    /**
     * Checks if high contrast mode is enabled.
     */
    fun isHighContrastMode(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_HIGH_CONTRAST, DEFAULT_HIGH_CONTRAST)
    }

    // ===== RESET TO DEFAULTS =====

    /**
     * Resets all settings to their default values.
     * Useful for troubleshooting or if users want to start fresh.
     * Note: Profile image is preserved and not reset.
     * The reset syncs to cloud so it applies across all devices.
     */
    fun resetToDefaults(context: Context) {
        val prefs = getPreferences(context)
        prefs.edit().apply {
            putString(KEY_LANGUAGE, DEFAULT_LANGUAGE)
            putBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS)
            putBoolean(KEY_DAILY_REMINDER, DEFAULT_DAILY_REMINDER)
            putBoolean(KEY_ACHIEVEMENT_ALERTS, DEFAULT_ACHIEVEMENT_ALERTS)
            putBoolean(KEY_SOUND_EFFECTS, DEFAULT_SOUND_EFFECTS)
            putBoolean(KEY_BACKGROUND_MUSIC, DEFAULT_BACKGROUND_MUSIC)
            putString(KEY_CARD_BACKGROUND, DEFAULT_CARD_BACKGROUND)
            putBoolean(KEY_HIGH_CONTRAST, DEFAULT_HIGH_CONTRAST)
            // Note: We don't clear profile image on reset
            apply()
        }
        Log.d(TAG, "Settings reset to defaults")
        triggerSync(context) // Sync to cloud
    }

    // ===== UTILITY FUNCTIONS =====

    /**
     * Returns all current settings as a map.
     * Useful for debugging, exporting settings, or displaying in a settings summary screen.
     * The map uses setting names as keys and current values as values.
     */
    fun getAllSettings(context: Context): Map<String, Any?> {
        return mapOf(
            "language" to getLanguage(context),
            "notifications_enabled" to isNotificationsEnabled(context),
            "daily_reminder" to isDailyReminderEnabled(context),
            "achievement_alerts" to isAchievementAlertsEnabled(context),
            "sound_effects" to isSoundEffectsEnabled(context),
            "background_music" to isBackgroundMusicEnabled(context),
            "card_background" to getCardBackground(context),
            "high_contrast" to isHighContrastMode(context),
            "biometric_enabled" to BiometricHelper.isBiometricEnabled(context),
            "profile_image_uri" to getProfileImageUri(context)
        )
    }

    /**
     * Prints all current settings to the Android log.
     * Helpful during development and debugging to see the full settings state.
     * Uses a formatted output with a header and footer for easy reading.
     */
    fun logAllSettings(context: Context) {
        Log.d(TAG, "=== Current Settings ===")
        getAllSettings(context).forEach { (key, value) ->
            Log.d(TAG, "$key: $value")
        }
        Log.d(TAG, "=======================")
    }
}