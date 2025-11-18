package vcmsa.projects.prog7314.data.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import vcmsa.projects.prog7314.R

/*
    Code Attribution for: Enum Classes
    ===================================================
    Kotlin, 2025b. Enum classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/enum-classes.html>
    [Accessed 18 November 2025].
*/



/**
 * Represents a theme for the memory card game, including the display name,
 * preview image, and the set of images used for the cards in this theme.
 *
 * @property themeNameResId Resource ID for the theme name (for localization).
 * @property previewImage Drawable resource ID for the theme preview image.
 * @property cardImages List of drawable resource IDs for the individual card images.
 */
enum class GameTheme(
    @StringRes val themeNameResId: Int,
    @DrawableRes val previewImage: Int,
    val cardImages: List<Int>
) {
    ANIMALS(
        themeNameResId = R.string.theme_animals,
        previewImage = R.drawable.animal_theme,
        cardImages = listOf(
            R.drawable.animal_1,
            R.drawable.animal_2,
            R.drawable.animal_3,
            R.drawable.animal_4,
            R.drawable.animal_5,
            R.drawable.animal_6,
            R.drawable.animal_7,
            R.drawable.animal_8,
            R.drawable.animal_9,
            R.drawable.animal_10,
            R.drawable.animal_11,
            R.drawable.animal_12,
        )
    ),
    POKEMON(
        themeNameResId = R.string.theme_pokemon,
        previewImage = R.drawable.pokemon_theme,
        cardImages = listOf(
            R.drawable.pokemon_1,
            R.drawable.pokemon_2,
            R.drawable.pokemon_3,
            R.drawable.pokemon_4,
            R.drawable.pokemon_5,
            R.drawable.pokemon_6,
            R.drawable.pokemon_7,
            R.drawable.pokemon_8,
            R.drawable.pokemon_9,
            R.drawable.pokemon_10,
            R.drawable.pokemon_11,
            R.drawable.pokemon_12,
        )
    ),
    FRUIT(
        themeNameResId = R.string.theme_fruits,
        previewImage = R.drawable.fruit_theme,
        cardImages = listOf(
            R.drawable.fruit_1,
            R.drawable.fruit_2,
            R.drawable.fruit_3,
            R.drawable.fruit_4,
            R.drawable.fruit_5,
            R.drawable.fruit_6,
            R.drawable.fruit_7,
            R.drawable.fruit_8,
            R.drawable.fruit_9,
            R.drawable.fruit_10,
            R.drawable.fruit_11,
            R.drawable.fruit_12,
        )
    ),
    HARRY_POTTER(
        themeNameResId = R.string.theme_harry_potter,
        previewImage = R.drawable.harry_potter_theme,
        cardImages = listOf(
            R.drawable.hp_1,
            R.drawable.hp_2,
            R.drawable.hp_3,
            R.drawable.hp_4,
            R.drawable.hp_5,
            R.drawable.hp_6,
            R.drawable.hp_7,
            R.drawable.hp_8,
            R.drawable.hp_9,
            R.drawable.hp_10,
            R.drawable.hp_11,
            R.drawable.hp_12,
        )
    ),
    F1_LOGOS(
        themeNameResId = R.string.theme_f1,
        previewImage = R.drawable.f1_theme,
        cardImages = listOf(
            R.drawable.f1_1,
            R.drawable.f1_2,
            R.drawable.f1_3,
            R.drawable.f1_4,
            R.drawable.f1_5,
            R.drawable.f1_6,
            R.drawable.f1_7,
            R.drawable.f1_8,
            R.drawable.f1_9,
            R.drawable.f1_10,
            R.drawable.f1_11,
            R.drawable.f1_12,
        )
    )
}

/**
 * Represents the size of the game grid.
 *
 * @property rows Number of rows in the grid.
 * @property columns Number of columns in the grid.
 * @property difficulty Difficulty level associated with this grid.
 */
data class GridSize(
    val rows: Int,
    val columns: Int,
    val difficulty: DifficultyLevel
) {
    val totalCards: Int get() = rows * columns
    val gridLabel: String get() = "$rows X $columns"
}

/**
 * Enum representing the difficulty levels of a game.
 *
 * @property displayName Human-readable name of the difficulty level.
 */
enum class DifficultyLevel(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    HARD("Hard"),
    EXPERT("Expert")
}

/**
 * Predefined grid sizes for different difficulty levels.
 */
object GridSizes {
    val BEGINNER_3X2 = GridSize(3, 2, DifficultyLevel.BEGINNER)
    val INTERMEDIATE_3X4 = GridSize(3, 4, DifficultyLevel.INTERMEDIATE)
    val HARD_4X5 = GridSize(4, 5, DifficultyLevel.HARD)
    val EXPERT_4X6 = GridSize(4, 6, DifficultyLevel.EXPERT)

    val allGridSizes = listOf(
        BEGINNER_3X2,
        INTERMEDIATE_3X4,
        HARD_4X5,
        EXPERT_4X6
    )
}
