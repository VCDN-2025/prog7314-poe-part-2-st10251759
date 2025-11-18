package vcmsa.projects.prog7314.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.models.GameResult

/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/

/**
 * GameCompletionPopup
 *
 * This Composable displays a modal dialog when a game is completed, showing
 * the player's performance, score, and action buttons for replaying, moving
 * to the next level, or returning home.
 *
 * Parameters:
 * - gameResult: GameResult -> Data class containing moves, time taken, points, and stars earned.
 * - onReplay: () -> Unit -> Callback triggered when the player taps the replay button.
 * - onNext: () -> Unit -> Callback triggered when the player taps the next level button (optional).
 * - onHome: () -> Unit -> Callback triggered when the player taps the home button.
 * - onDismiss: () -> Unit -> Optional callback when the dialog is dismissed.
 *
 * Key Features:
 * 1. Time Display:
 *    - Converts game time from milliseconds to seconds for readability.
 *
 * 2. Dialog & Card:
 *    - Uses a Dialog Composable to overlay the popup.
 *    - Card provides rounded corners, elevation, and themed background color.
 *
 * 3. Visual Feedback:
 *    - "NEW RECORD" badge highlights if the player achieved a new high score.
 *    - Star rating shows performance (★ filled, ☆ empty) based on gameResult.stars.
 *    - "WELL DONE!" title for positive reinforcement.
 *
 * 4. Stats Section:
 *    - Displays moves, time in seconds, and bonus points inside a blue card.
 *    - StatRow helper Composable creates a clean row layout for each stat.
 *
 * 5. Total Score:
 *    - Shown in a yellow card with contrasting pink text for emphasis.
 *
 * 6. Action Buttons:
 *    - CircularActionButton helper Composable creates rounded, elevated buttons.
 *    - Includes Replay (orange) and Home (cyan) buttons, centered below stats.
 *
 * 7. UX & Design:
 *    - Uses consistent padding, spacing, and rounded corners for a polished look.
 *    - Colors are bright and motivating (pink, yellow, blue, orange, cyan).
 *    - Immediate visual feedback to celebrate player achievements.
 *
 * 8. Extensibility:
 *    - The popup can be easily extended to include a Next Level button by using
 *      the onNext callback and adding another CircularActionButton.
 */


@Composable
fun GameCompletionPopup(
    gameResult: GameResult,
    onReplay: () -> Unit,
    onNext: () -> Unit,
    onHome: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    // Convert milliseconds to seconds for display
    val timeInSeconds = (gameResult.timeTaken / 1000).toInt()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE91E63) // Pink
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "NEW RECORD" badge (optional - show based on logic)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEB3B)) // Yellow
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.new_record_badge),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    repeat(3) { index ->
                        Text(
                            text = if (index < gameResult.stars) "★" else "☆",
                            fontSize = 40.sp,
                            color = Color(0xFFFFEB3B) // Yellow
                        )
                    }
                }

                // "WELL DONE!" title
                Text(
                    text = stringResource(R.string.well_done),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Stats card (blue background)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3) // Blue
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        StatRow(
                            label = stringResource(R.string.moves_colon),
                            value = gameResult.moves.toString()
                        )
                        StatRow(
                            label = stringResource(R.string.time_colon),
                            value = stringResource(R.string.seconds_format, timeInSeconds)
                        )
                        StatRow(
                            label = stringResource(R.string.bonus_colon),
                            value = gameResult.points.toString()
                        )
                    }
                }

                // Total score (yellow background)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEB3B) // Yellow
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.total_colon),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E63)
                        )
                        Text(
                            text = gameResult.points.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE91E63)
                        )
                    }
                }

                // Action buttons - Centered layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Replay button (Orange)
                    CircularActionButton(
                        icon = "↻",
                        backgroundColor = Color(0xFFFF9800),
                        onClick = onReplay
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Home button (Cyan)
                    CircularActionButton(
                        icon = "⌂",
                        backgroundColor = Color(0xFF00BCD4),
                        onClick = onHome
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun CircularActionButton(
    icon: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 28.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}