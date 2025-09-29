package vcmsa.projects.prog7314.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SettingsManager {

    private const val TAG = "SettingsManager"
    private const val PREFS_NAME = "AppSettings"

    // Keys for all settings
    private const val KEY_LANGUAGE = "language_preference"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_DAILY_REMINDER = "daily_reminder_enabled"
    private const val KEY_ACHIEVEMENT_ALERTS = "achievement_alerts_enabled"
    private const val KEY_SOUND_EFFECTS = "sound_effects_enabled"
    private const val KEY_BACKGROUND_MUSIC = "background_music_enabled"
    private const val KEY_CARD_BACKGROUND = "card_background_theme"
    private const val KEY_HIGH_CONTRAST = "high_contrast_mode"

    // Default values
    private const val DEFAULT_LANGUAGE = "en"
    private const val DEFAULT_NOTIFICATIONS = true
    private const val DEFAULT_DAILY_REMINDER = true
    private const val DEFAULT_ACHIEVEMENT_ALERTS = true
    private const val DEFAULT_SOUND_EFFECTS = true
    private const val DEFAULT_BACKGROUND_MUSIC = true
    private const val DEFAULT_CARD_BACKGROUND = "blue"
    private const val DEFAULT_HIGH_CONTRAST = false

    /**
     * Get SharedPreferences instance
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ===== LANGUAGE SETTINGS =====

    fun setLanguage(context: Context, language: String) {
        getPreferences(context).edit().putString(KEY_LANGUAGE, language).apply()
        Log.d(TAG, "Language set to: $language")
    }

    fun getLanguage(context: Context): String {
        return getPreferences(context).getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    // ===== NOTIFICATION SETTINGS =====

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        Log.d(TAG, "Notifications enabled: $enabled")
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS)
    }

    fun setDailyReminderEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_DAILY_REMINDER, enabled).apply()
        Log.d(TAG, "Daily reminder enabled: $enabled")
    }

    fun isDailyReminderEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_DAILY_REMINDER, DEFAULT_DAILY_REMINDER)
    }

    fun setAchievementAlertsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_ACHIEVEMENT_ALERTS, enabled).apply()
        Log.d(TAG, "Achievement alerts enabled: $enabled")
    }

    fun isAchievementAlertsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_ACHIEVEMENT_ALERTS, DEFAULT_ACHIEVEMENT_ALERTS)
    }

    // ===== AUDIO SETTINGS =====

    fun setSoundEffectsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_SOUND_EFFECTS, enabled).apply()
        Log.d(TAG, "Sound effects enabled: $enabled")
    }

    fun isSoundEffectsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_SOUND_EFFECTS, DEFAULT_SOUND_EFFECTS)
    }

    fun setBackgroundMusicEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_BACKGROUND_MUSIC, enabled).apply()
        Log.d(TAG, "Background music enabled: $enabled")
    }

    fun isBackgroundMusicEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_BACKGROUND_MUSIC, DEFAULT_BACKGROUND_MUSIC)
    }

    // ===== APPEARANCE SETTINGS =====

    fun setCardBackground(context: Context, theme: String) {
        getPreferences(context).edit().putString(KEY_CARD_BACKGROUND, theme).apply()
        Log.d(TAG, "Card background set to: $theme")
    }

    fun getCardBackground(context: Context): String {
        return getPreferences(context).getString(KEY_CARD_BACKGROUND, DEFAULT_CARD_BACKGROUND)
            ?: DEFAULT_CARD_BACKGROUND
    }

    fun setHighContrastMode(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
        Log.d(TAG, "High contrast mode: $enabled")
    }

    fun isHighContrastMode(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_HIGH_CONTRAST, DEFAULT_HIGH_CONTRAST)
    }

    // ===== RESET TO DEFAULTS =====

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
            apply()
        }
        Log.d(TAG, "Settings reset to defaults")
    }

    // ===== UTILITY FUNCTIONS =====

    /**
     * Get all settings as a map (useful for debugging)
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
            "biometric_enabled" to BiometricHelper.isBiometricEnabled(context)
        )
    }

    /**
     * Log all current settings (for debugging)
     */
    fun logAllSettings(context: Context) {
        Log.d(TAG, "=== Current Settings ===")
        getAllSettings(context).forEach { (key, value) ->
            Log.d(TAG, "$key: $value")
        }
        Log.d(TAG, "=======================")
    }
}