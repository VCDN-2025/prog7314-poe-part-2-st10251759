package vcmsa.projects.prog7314.data.models

import androidx.annotation.DrawableRes
import vcmsa.projects.prog7314.R

enum class GameTheme(
    val themeName: String,
    @DrawableRes val previewImage: Int,
    val cardImages: List<Int>
) {
    ANIMALS(
        themeName = "Animals",
        previewImage = R.drawable.animal_theme,
        cardImages = listOf(
            // your animal images
        )
    ),
    POKEMON(
        themeName = "Pokémon",
        previewImage = R.drawable.pokemon_theme,
        cardImages = listOf(
            R.drawable.pikachu,
            R.drawable.pokemon_card_matching,
            R.drawable.pokemon_card_matching_2,
            R.drawable.pokemon_card_matching_3
        )
    ),
    FRUIT(
        themeName = "Fruit",
        previewImage = R.drawable.fruit_theme,
        cardImages = listOf(
            // your fruit images
        )
    ),
    HARRY_POTTER(
        themeName = "Harry Potter",
        previewImage = R.drawable.harry_potter_theme,
        cardImages = listOf(
            // your HP images
        )
    ),
    F1_LOGOS(
        themeName = "F1 Team Logos",
        previewImage = R.drawable.f1_theme,
        cardImages = listOf(
            // your F1 images
        )

    )
}

data class GridSize(
    val rows: Int,
    val columns: Int,
    val difficulty: DifficultyLevel
) {
    val totalCards: Int get() = rows * columns
    val gridLabel: String get() = "$rows X $columns"
}

enum class DifficultyLevel(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    HARD("Hard"),
    EXPERT("Expert")
}

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