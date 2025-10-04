package vcmsa.projects.prog7314.data.models

data class GameProgress(
    val userId: String = "",
    val currentLevel: Int = 1,
    val levelProgress: Map<Int, LevelData> = emptyMap(), // Level number -> Level data
    val unlockedCategories: List<String> = listOf("Animals"),
    val totalGamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class LevelData(
    val levelNumber: Int,
    val stars: Int = 0,
    val bestScore: Int = 0,
    val bestTime: Int = 0,
    val bestMoves: Int = 0,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val timesPlayed: Int = 0
)