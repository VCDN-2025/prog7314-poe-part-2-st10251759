package vcmsa.projects.prog7314.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import java.util.Locale

/*
    Code Attribution for: Language Preference
    ===================================================
    Android Developers, 2025. Per-app language preferences (Version unknown) [Source code].
    Available at: <https://developer.android.com/guide/topics/resources/app-languages>
    [Accessed 18 November 2025].
*/


/**
 * Utility object for managing app language and localization.
 * Handles language selection, persistence, and dynamic language switching.
 * Supports English, isiZulu, and Afrikaans.
 */
object LanguageManager {

    private const val TAG = "LanguageManager"

    // SharedPreferences file name for storing language preference
    private const val PREFS_NAME = "language_prefs"

    // Key used to store and retrieve the selected language code
    private const val KEY_LANGUAGE = "selected_language"

    /**
     * Enum representing all languages supported by the app.
     * Each language has a two-letter code (ISO 639-1 standard) and a display name.
     */
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        ZULU("zu", "isiZulu"),
        AFRIKAANS("af", "Afrikaans")
    }

    /**
     * Retrieves the currently selected language from SharedPreferences.
     * Returns the saved language, or defaults to English if none is saved.
     */
    fun getCurrentLanguage(context: Context): Language {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_LANGUAGE, Language.ENGLISH.code) ?: Language.ENGLISH.code

        // Find the language matching the saved code, default to English if not found
        return Language.values().find { it.code == languageCode } ?: Language.ENGLISH
    }

    /**
     * Changes the app language to the specified language and saves the preference.
     * The new language persists across app restarts.
     * Immediately applies the language to the current context.
     */
    fun setLanguage(context: Context, language: Language) {
        // Save the language preference to SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()

        // Apply the language change to the app
        applyLanguage(context, language)
        Log.d(TAG, "Language changed to: ${language.displayName} (${language.code})")
    }

    /**
     * Applies the specified language to the app's current context.
     * Updates the system locale and reloads resources with the new language.
     * This affects all string resources loaded from XML files.
     */
    fun applyLanguage(context: Context, language: Language) {
        // Create a Locale object from the language code
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        // Update the app's configuration with the new locale
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        // Apply the configuration change to resources
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /**
     * Loads and applies the saved language preference when the app starts.
     * Should be called in Application.onCreate() or MainActivity.onCreate().
     * Ensures the user's language choice is applied before any UI is shown.
     */
    fun initializeLanguage(context: Context) {
        val currentLanguage = getCurrentLanguage(context)
        applyLanguage(context, currentLanguage)
        Log.d(TAG, "Language initialized: ${currentLanguage.displayName}")
    }

    /**
     * Restarts the current activity to fully apply the language change.
     * This is necessary because some UI elements don't update dynamically.
     * Uses a fade transition for a smoother user experience.
     */
    fun restartActivity(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)

        // Apply a fade animation during the restart
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /**
     * Returns a list of all languages supported by the app.
     * Useful for populating language selection menus or settings screens.
     */
    fun getAvailableLanguages(): List<Language> {
        return Language.values().toList()
    }

    /**
     * Retrieves a localized string resource.
     * The string returned will be in the currently active language.
     * This is a convenience wrapper around Context.getString().
     */
    fun getString(context: Context, resourceId: Int): String {
        return context.getString(resourceId)
    }
}