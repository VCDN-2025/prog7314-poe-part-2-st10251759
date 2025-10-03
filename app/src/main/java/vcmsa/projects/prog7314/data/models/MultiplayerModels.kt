package vcmsa.projects.prog7314.data.models

data class MultiplayerGameState(
    val cards: List<GameCard> = emptyList(),
    val player1: Player = Player(PlayerColor.RED, isCurrentTurn = true),
    val player2: Player = Player(PlayerColor.BLUE, isCurrentTurn = false),
    val matchedPairs: Int = 0,
    val totalPairs: Int = 12,
    val isGameComplete: Boolean = false,
    val theme: GameTheme = GameTheme.POKEMON
)

data class MultiplayerGameResult(
    val theme: GameTheme,
    val player1Score: Int,
    val player2Score: Int,
    val winner: PlayerColor?,
    val timeTaken: Int,
    val totalMoves: Int
)
