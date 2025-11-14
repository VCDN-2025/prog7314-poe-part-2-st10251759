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

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Replay button (Orange)
                    CircularActionButton(
                        icon = "↻",
                        backgroundColor = Color(0xFFFF9800),
                        onClick = onReplay
                    )

                    // Next button (Green)
                    CircularActionButton(
                        icon = "→",
                        backgroundColor = Color(0xFF4CAF50),
                        onClick = onNext
                    )

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
        Text(
            text = icon,
            fontSize = 28.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}