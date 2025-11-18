package vcmsa.projects.prog7314.data.models

/*
    Code Attribution for: Data Classes
    ===================================================
    Kotlin, 2025a. Data classes | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://kotlinlang.org/docs/data-classes.html>
    [Accessed 18 November 2025].
*/

/**
 * Represents the current state of a multiplayer memory card game.
 *
 * @property cards The list of all game cards in the current match.
 * @property player1 The first player and their current state.
 * @property player2 The second player and their current state.
 * @property matchedPairs The number of pairs that have been matched so far.
 * @property totalPairs The total number of pairs in the game (default 12).
 * @property isGameComplete Flag indicating whether the game has finished.
 * @property theme The theme being used for this multiplayer game.
 */
data class MultiplayerGameState(
    val cards: List<GameCard> = emptyList(),
    val player1: Player = Player(PlayerColor.RED, isCurrentTurn = true),
    val player2: Player = Player(PlayerColor.BLUE, isCurrentTurn = false),
    val matchedPairs: Int = 0,
    val totalPairs: Int = 12,
    val isGameComplete: Boolean = false,
    val theme: GameTheme = GameTheme.POKEMON
)

/**
 * Represents the result of a completed multiplayer memory card game.
 *
 * @property theme The theme used in the game.
 * @property player1Score Score achieved by player 1.
 * @property player2Score Score achieved by player 2.
 * @property winner The winning player (null if tie).
 * @property timeTaken Total time taken to complete the game (in seconds).
 * @property totalMoves Total moves made by both players.
 */
data class MultiplayerGameResult(
    val theme: GameTheme,
    val player1Score: Int,
    val player2Score: Int,
    val winner: PlayerColor?,
    val timeTaken: Int,
    val totalMoves: Int
)
