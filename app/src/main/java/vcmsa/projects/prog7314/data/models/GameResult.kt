package vcmsa.projects.prog7314.data.models

data class GameResult(
    val theme: GameTheme,
    val gridSize: GridSize,
    val points: Int,
    val timeTaken: Long,  // Changed from timeInSeconds to timeTaken for consistency
    val moves: Int,
    val stars: Int,
    val bonus: Int
)

