package vcmsa.projects.prog7314.data.models

import androidx.compose.ui.graphics.Color

enum class PlayerColor(val displayName: String, val color: Color) {
    RED("Player 1 [Red]", Color(0xFFE53935)),
    BLUE("Player 2 [Blue]", Color(0xFF1E88E5))
}

data class Player(
    val playerColor: PlayerColor,
    val score: Int = 0,
    val isCurrentTurn: Boolean = false
) {
    val displayName: String get() = playerColor.displayName
    val color: Color get() = playerColor.color
}