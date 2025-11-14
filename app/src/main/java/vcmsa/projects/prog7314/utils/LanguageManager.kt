package vcmsa.projects.prog7314.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import java.util.Locale

object LanguageManager {

    private const val TAG = "LanguageManager"
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    // Available languages
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        ZULU("zu", "isiZulu"),
        AFRIKAANS("af", "Afrikaans")
    }

    /**
     * Get currently selected language
     */
    fun getCurrentLanguage(context: Context): Language {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_LANGUAGE, Language.ENGLISH.code) ?: Language.ENGLISH.code

        return Language.values().find { it.code == languageCode } ?: Language.ENGLISH
    }

    /**
     * Set app language and persist the choice
     */
    fun setLanguage(context: Context, language: Language) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()

        applyLanguage(context, language)
        Log.d(TAG, "Language changed to: ${language.displayName} (${language.code})")
    }

    /**
     * Apply language to the current context
     */
    fun applyLanguage(context: Context, language: Language) {
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /**
     * Initialize language on app start
     */
    fun initializeLanguage(context: Context) {
        val currentLanguage = getCurrentLanguage(context)
        applyLanguage(context, currentLanguage)
        Log.d(TAG, "Language initialized: ${currentLanguage.displayName}")
    }

    /**
     * Restart activity to apply language change
     */
    fun restartActivity(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /**
     * Get all available languages
     */
    fun getAvailableLanguages(): List<Language> {
        return Language.values().toList()
    }

    /**
     * Get localized string from resources
     */
    fun getString(context: Context, resourceId: Int): String {
        return context.getString(resourceId)
    }
}