package vcmsa.projects.prog7314.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import vcmsa.projects.prog7314.R

enum class AchievementType(
    val achievementId: String,
    val displayNameResId: Int,  // ✅ Changed to resource ID
    val descriptionResId: Int,  // ✅ Changed to resource ID
    val icon: ImageVector,
    val color: Color,
    val gradientColors: Pair<Color, Color>
) {
    FIRST_WIN(
        achievementId = "FIRST_WIN",
        displayNameResId = R.string.achievement_first_win,
        descriptionResId = R.string.achievement_first_win_desc,
        icon = Icons.Default.EmojiEvents,
        color = Color(0xFFFFC107),
        gradientColors = Pair(Color(0xFFFFC107), Color(0xFFFF9800))
    ),

    SPEED_DEMON(
        achievementId = "SPEED_DEMON",
        displayNameResId = R.string.achievement_speed_demon,
        descriptionResId = R.string.achievement_speed_demon_desc,
        icon = Icons.Default.Speed,
        color = Color(0xFFE91E63),
        gradientColors = Pair(Color(0xFFE91E63), Color(0xFFC2185B))
    ),

    MEMORY_GURU(
        achievementId = "MEMORY_GURU",
        displayNameResId = R.string.achievement_memory_guru,
        descriptionResId = R.string.achievement_memory_guru_desc,
        icon = Icons.Default.Psychology,
        color = Color(0xFF9C27B0),
        gradientColors = Pair(Color(0xFF9C27B0), Color(0xFF7B1FA2))
    ),

    PERFECT_PERFORMANCE(
        achievementId = "PERFECT_PERFORMANCE",
        displayNameResId = R.string.achievement_perfect_performance,
        descriptionResId = R.string.achievement_perfect_performance_desc,
        icon = Icons.Default.Star,
        color = Color(0xFFFFC107),
        gradientColors = Pair(Color(0xFFFFD54F), Color(0xFFFFA000))
    ),

    CHAMPION(
        achievementId = "CHAMPION",
        displayNameResId = R.string.achievement_champion,
        descriptionResId = R.string.achievement_champion_desc,
        icon = Icons.Default.MilitaryTech,
        color = Color(0xFF4CAF50),
        gradientColors = Pair(Color(0xFF4CAF50), Color(0xFF388E3C))
    ),

    STREAK_MASTER(
        achievementId = "STREAK_MASTER",
        displayNameResId = R.string.achievement_streak_master,
        descriptionResId = R.string.achievement_streak_master_desc,
        icon = Icons.Default.LocalFireDepartment,
        color = Color(0xFFFF5722),
        gradientColors = Pair(Color(0xFFFF5722), Color(0xFFE64A19))
    ),

    HIGH_SCORER(
        achievementId = "HIGH_SCORER",
        displayNameResId = R.string.achievement_high_scorer,
        descriptionResId = R.string.achievement_high_scorer_desc,
        icon = Icons.Default.TrendingUp,
        color = Color(0xFF2196F3),
        gradientColors = Pair(Color(0xFF2196F3), Color(0xFF1976D2))
    ),

    ARCADE_MASTER(
        achievementId = "ARCADE_MASTER",
        displayNameResId = R.string.achievement_arcade_master,
        descriptionResId = R.string.achievement_arcade_master_desc,
        icon = Icons.Default.Gamepad,
        color = Color(0xFF00BCD4),
        gradientColors = Pair(Color(0xFF00BCD4), Color(0xFF0097A7))
    ),

    FLAWLESS(
        achievementId = "FLAWLESS",
        displayNameResId = R.string.achievement_flawless,
        descriptionResId = R.string.achievement_flawless_desc,
        icon = Icons.Default.CheckCircle,
        color = Color(0xFF4CAF50),
        gradientColors = Pair(Color(0xFF66BB6A), Color(0xFF43A047))
    ),

    PERSISTENT(
        achievementId = "PERSISTENT",
        displayNameResId = R.string.achievement_persistent,
        descriptionResId = R.string.achievement_persistent_desc,
        icon = Icons.Default.Schedule,
        color = Color(0xFF9E9E9E),
        gradientColors = Pair(Color(0xFF9E9E9E), Color(0xFF757575))
    ),

    THEME_EXPLORER(
        achievementId = "THEME_EXPLORER",
        displayNameResId = R.string.achievement_theme_explorer,
        descriptionResId = R.string.achievement_theme_explorer_desc,
        icon = Icons.Default.Palette,
        color = Color(0xFFE91E63),
        gradientColors = Pair(Color(0xFFEC407A), Color(0xFFD81B60))
    ),

    LEVEL_CONQUEROR(
        achievementId = "LEVEL_CONQUEROR",
        displayNameResId = R.string.achievement_level_conqueror,
        descriptionResId = R.string.achievement_level_conqueror_desc,
        icon = Icons.Default.Stars,
        color = Color(0xFFFFD700),
        gradientColors = Pair(Color(0xFFFFD700), Color(0xFFFFA500))
    );

    companion object {
        fun fromId(id: String): AchievementType? {
            return values().find { it.achievementId == id }
        }

        fun getAllAchievements(): List<AchievementType> {
            return values().toList()
        }
    }
}