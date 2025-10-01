package vcmsa.projects.prog7314.data.models

import androidx.annotation.DrawableRes

data class GameCard(
    val id: Int,
    val pairId: Int,
    @DrawableRes val imageResId: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

data class GameState(
    val cards: List<GameCard>,
    val moves: Int = 0,
    val matchedPairs: Int = 0,
    val points: Int = 0,
    val timeElapsed: Long = 0,
    val isComplete: Boolean = false
)

data class GameResult(
    val theme: GameTheme,
    val gridSize: GridSize,
    val moves: Int,
    val timeInSeconds: Int,
    val points: Int,
    val stars: Int,
    val bonus: Int
)