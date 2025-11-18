package vcmsa.projects.prog7314.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/*
    Code Attribution for: Push Notifications
    ===================================================
    Firebase, 2019. Firebase Cloud Messaging | Firebase (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/cloud-messaging>
    [Accessed 18 November 2025]
*/


/**
 * Notification Tracker - Prevents duplicate notifications
 *
 * This class tracks which notifications have already been sent to prevent:
 * - Repetitive notifications when reopening themes
 * - Duplicate notifications on login
 * - Multiple notifications for already completed levels
 * - Notification flooding
 *
 * Uses SharedPreferences for persistent storage across app sessions
 */
object NotificationTracker {

    private const val TAG = "NotificationTracker"
    private const val PREFS_NAME = "notification_tracker"

    // Keys for different notification types
    private const val KEY_LEVELS_UNLOCKED = "levels_unlocked_"
    private const val KEY_THEMES_PLAYED = "themes_played_"
    private const val KEY_ACHIEVEMENTS_SENT = "achievements_sent_"
    private const val KEY_FIRST_LEVEL_SENT = "first_level_sent"
    private const val KEY_LAST_STREAK_DATE = "last_streak_date"

    /**
     * Get SharedPreferences instance for a specific user
     */
    private fun getPrefs(context: Context, userId: String): SharedPreferences {
        return context.getSharedPreferences("${PREFS_NAME}_$userId", Context.MODE_PRIVATE)
    }

    // ===== LEVEL UNLOCK TRACKING =====

    /**
     * Check if level unlock notification has been sent
     */
    fun hasLevelUnlockBeenSent(context: Context, userId: String, levelNumber: Int): Boolean {
        val key = "$KEY_LEVELS_UNLOCKED$levelNumber"
        val sent = getPrefs(context, userId).getBoolean(key, false)
        Log.d(TAG, "Check level $levelNumber unlock notification sent: $sent")
        return sent
    }

    /**
     * Mark level unlock notification as sent
     */
    fun markLevelUnlockAsSent(context: Context, userId: String, levelNumber: Int) {
        val key = "$KEY_LEVELS_UNLOCKED$levelNumber"
        getPrefs(context, userId).edit().putBoolean(key, true).apply()
        Log.d(TAG, "✅ Marked level $levelNumber unlock notification as sent")
    }

    /**
     * Clear all level unlock tracking (use when resetting progress)
     */
    fun clearAllLevelUnlocks(context: Context, userId: String) {
        val prefs = getPrefs(context, userId)
        val editor = prefs.edit()

        // Remove all level unlock keys
        prefs.all.keys.filter { it.startsWith(KEY_LEVELS_UNLOCKED) }.forEach { key ->
            editor.remove(key)
        }

        editor.apply()
        Log.d(TAG, "🗑️ Cleared all level unlock notifications")
    }

    // ===== THEME UNLOCK TRACKING =====

    /**
     * Check if theme unlock notification has been sent
     */
    fun hasThemeUnlockBeenSent(context: Context, userId: String, themeName: String): Boolean {
        val key = "$KEY_THEMES_PLAYED${themeName.replace(" ", "_")}"
        val sent = getPrefs(context, userId).getBoolean(key, false)
        Log.d(TAG, "Check theme '$themeName' unlock notification sent: $sent")
        return sent
    }

    /**
     * Mark theme unlock notification as sent
     */
    fun markThemeUnlockAsSent(context: Context, userId: String, themeName: String) {
        val key = "$KEY_THEMES_PLAYED${themeName.replace(" ", "_")}"
        getPrefs(context, userId).edit().putBoolean(key, true).apply()
        Log.d(TAG, "✅ Marked theme '$themeName' unlock notification as sent")
    }

    /**
     * Clear all theme unlock tracking (use when resetting progress)
     */
    fun clearAllThemeUnlocks(context: Context, userId: String) {
        val prefs = getPrefs(context, userId)
        val editor = prefs.edit()

        // Remove all theme unlock keys
        prefs.all.keys.filter { it.startsWith(KEY_THEMES_PLAYED) }.forEach { key ->
            editor.remove(key)
        }

        editor.apply()
        Log.d(TAG, "🗑️ Cleared all theme unlock notifications")
    }

    // ===== ACHIEVEMENT TRACKING =====

    /**
     * Check if achievement notification has been sent
     */
    fun hasAchievementNotificationBeenSent(context: Context, userId: String, achievementType: String): Boolean {
        val key = "$KEY_ACHIEVEMENTS_SENT$achievementType"
        val sent = getPrefs(context, userId).getBoolean(key, false)
        Log.d(TAG, "Check achievement '$achievementType' notification sent: $sent")
        return sent
    }

    /**
     * Mark achievement notification as sent
     */
    fun markAchievementNotificationAsSent(context: Context, userId: String, achievementType: String) {
        val key = "$KEY_ACHIEVEMENTS_SENT$achievementType"
        getPrefs(context, userId).edit().putBoolean(key, true).apply()
        Log.d(TAG, "✅ Marked achievement '$achievementType' notification as sent")
    }

    /**
     * Clear all achievement notification tracking (use when resetting progress)
     */
    fun clearAllAchievementNotifications(context: Context, userId: String) {
        val prefs = getPrefs(context, userId)
        val editor = prefs.edit()

        // Remove all achievement notification keys
        prefs.all.keys.filter { it.startsWith(KEY_ACHIEVEMENTS_SENT) }.forEach { key ->
            editor.remove(key)
        }

        editor.apply()
        Log.d(TAG, "🗑️ Cleared all achievement notifications")
    }

    // ===== FIRST LEVEL TRACKING =====

    /**
     * Check if first level completion notification has been sent
     */
    fun hasFirstLevelNotificationBeenSent(context: Context, userId: String): Boolean {
        val sent = getPrefs(context, userId).getBoolean(KEY_FIRST_LEVEL_SENT, false)
        Log.d(TAG, "Check first level notification sent: $sent")
        return sent
    }

    /**
     * Mark first level completion notification as sent
     */
    fun markFirstLevelNotificationAsSent(context: Context, userId: String) {
        getPrefs(context, userId).edit().putBoolean(KEY_FIRST_LEVEL_SENT, true).apply()
        Log.d(TAG, "✅ Marked first level notification as sent")
    }

    // ===== STREAK TRACKING =====

    /**
     * Get last date when streak notification was sent
     */
    fun getLastStreakNotificationDate(context: Context, userId: String): Long {
        return getPrefs(context, userId).getLong(KEY_LAST_STREAK_DATE, 0L)
    }

    /**
     * Mark streak notification as sent today
     */
    fun markStreakNotificationAsSent(context: Context, userId: String) {
        val today = System.currentTimeMillis()
        getPrefs(context, userId).edit().putLong(KEY_LAST_STREAK_DATE, today).apply()
        Log.d(TAG, "✅ Marked streak notification as sent for today")
    }

    /**
     * Check if streak notification should be sent today
     * Returns true if notification hasn't been sent today
     */
    fun shouldSendStreakNotification(context: Context, userId: String): Boolean {
        val lastNotificationDate = getLastStreakNotificationDate(context, userId)

        if (lastNotificationDate == 0L) {
            return true // Never sent before
        }

        // Check if it's a new day
        val lastDay = lastNotificationDate / (24 * 60 * 60 * 1000)
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)

        val shouldSend = today > lastDay
        Log.d(TAG, "Should send streak notification: $shouldSend (last: $lastDay, today: $today)")
        return shouldSend
    }

    // ===== UTILITY METHODS =====

    /**
     * Clear all tracking for a user (use when logging out or resetting all progress)
     */
    fun clearAllTracking(context: Context, userId: String) {
        getPrefs(context, userId).edit().clear().apply()
        Log.d(TAG, "🗑️ Cleared all notification tracking for user: $userId")
    }

    /**
     * Get summary of tracked notifications for debugging
     */
    fun getTrackingSummary(context: Context, userId: String): String {
        val prefs = getPrefs(context, userId)
        val allKeys = prefs.all.keys

        val levelsTracked = allKeys.count { it.startsWith(KEY_LEVELS_UNLOCKED) }
        val themesTracked = allKeys.count { it.startsWith(KEY_THEMES_PLAYED) }
        val achievementsTracked = allKeys.count { it.startsWith(KEY_ACHIEVEMENTS_SENT) }
        val firstLevelSent = prefs.getBoolean(KEY_FIRST_LEVEL_SENT, false)

        return """
            Notification Tracking Summary:
            - Levels tracked: $levelsTracked
            - Themes tracked: $themesTracked
            - Achievements tracked: $achievementsTracked
            - First level sent: $firstLevelSent
        """.trimIndent()
    }
}