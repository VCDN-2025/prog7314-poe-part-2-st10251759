package vcmsa.projects.prog7314.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import vcmsa.projects.prog7314.R

/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/

/**
 * GameCompletionDialog
 *
 * This Composable displays a modal dialog when a player completes a game.
 * It provides visual feedback, game statistics, total score, and action buttons
 * appropriate to the game mode.
 *
 * Features & Behavior:
 *
 * 1. Game Modes:
 *    - Controlled via the GameMode enum:
 *      • ARCADE_RANDOM: Quick-play mode, only shows Home button.
 *      • ARCADE_LEVELS: Level-based arcade, shows Retry, Next Level, and Home buttons.
 *      • ADVENTURE: Adventure mode, full navigation buttons.
 *
 * 2. New Record Badge:
 *    - Optional badge displayed if the player sets a new high score.
 *    - Animated rotation effect to emphasize achievement.
 *
 * 3. Stars Display:
 *    - Shows earned stars with animated scaling for visual feedback.
 *    - Uses AnimatedStar Composable to handle each star's animation.
 *
 * 4. Stats Section:
 *    - Displays moves, time (formatted as mm:ss), and bonus points in a colored card.
 *    - Uses StatRow for consistent label-value layout.
 *
 * 5. Total Score:
 *    - Highlighted in a yellow card with gradient background and prominent font.
 *
 * 6. Action Buttons:
 *    - Layout and number of buttons vary based on game mode.
 *    - CircularActionButton is a reusable, elevated circular button for Replay, Next, Home, etc.
 *
 * 7. Animations & UX:
 *    - New record and star animations provide responsive, engaging visual feedback.
 *    - Padding, spacing, and rounded corners ensure a polished, user-friendly UI.
 *
 * 8. Utility Functions:
 *    - formatTimeForDialog: Converts total time in seconds to "MM:SS" format for display.
 *
 * Overall Purpose:
 * This dialog provides a clear and visually appealing summary of game performance,
 * celebrates achievements, and guides players to next actions depending on the
 * game mode.
 */


// Game mode enum to determine which buttons to show
enum class GameMode {
    ARCADE_RANDOM,      // Random quick-play: Only Home button
    ARCADE_LEVELS,      // Level-based: Retry, Next, Home buttons
    ADVENTURE           // Adventure mode: Full navigation
}

@Composable
fun GameCompletionDialog(
    gameMode: GameMode = GameMode.ARCADE_LEVELS,
    isNewRecord: Boolean = false,
    stars: Int,
    moves: Int,
    time: Int,
    bonus: Int,
    totalScore: Int,
    onReplay: () -> Unit,
    onNextLevel: (() -> Unit)? = null,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // New Record Badge (if applicable)
                if (isNewRecord) {
                    NewRecordBadge()
                }

                // Main Card
                CompletionCard(
                    gameMode = gameMode,
                    stars = stars,
                    moves = moves,
                    time = time,
                    bonus = bonus,
                    totalScore = totalScore,
                    onReplay = onReplay,
                    onNextLevel = onNextLevel,
                    onHome = onHome
                )
            }
        }
    }
}

@Composable
fun NewRecordBadge() {
    var visible by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (visible) 360f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    Box(
        modifier = Modifier
            .offset(y = 20.dp)
            .rotate(rotation)
            .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFB300), Color(0xFFFFC107))
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.new_record_exclamation),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CompletionCard(
    gameMode: GameMode,
    stars: Int,
    moves: Int,
    time: Int,
    bonus: Int,
    totalScore: Int,
    onReplay: () -> Unit,
    onNextLevel: (() -> Unit)?,
    onHome: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE91E63) // Pink like wireframe
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = stringResource(R.string.well_done_exclamation),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stars Animation
            StarsDisplay(stars = stars)

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF64B5F6)) // Light blue like wireframe
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatRow(
                        label = stringResource(R.string.moves_colon),
                        value = moves.toString(),
                        color = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(
                        label = stringResource(R.string.time_colon),
                        value = formatTimeForDialog(time),
                        color = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(
                        label = stringResource(R.string.bonus_colon),
                        value = bonus.toString(),
                        color = Color(0xFFFFC107)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Score
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFB300), Color(0xFFFFC107))
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.total_colon),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        text = totalScore.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1976D2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons - Layout depends on game mode
            when (gameMode) {
                GameMode.ARCADE_RANDOM -> {
                    // Only Home button for random quick-play
                    ArcadeRandomButtons(onHome = onHome)
                }
                GameMode.ARCADE_LEVELS -> {
                    // Retry, Next, Home for level-based arcade
                    ArcadeLevelsButtons(
                        onReplay = onReplay,
                        onNextLevel = onNextLevel,
                        onHome = onHome
                    )
                }
                GameMode.ADVENTURE -> {
                    // Full navigation for adventure mode
                    AdventureButtons(
                        onReplay = onReplay,
                        onNextLevel = onNextLevel,
                        onHome = onHome
                    )
                }
            }
        }
    }
}

// Button layouts for different game modes

@Composable
fun ArcadeRandomButtons(onHome: () -> Unit) {
    // Only Home button - centered
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularActionButton(
            icon = Icons.Default.Home,
            backgroundColor = Color(0xFF2196F3),
            onClick = onHome
        )
    }
}

@Composable
fun ArcadeLevelsButtons(
    onReplay: () -> Unit,
    onNextLevel: (() -> Unit)?,
    onHome: () -> Unit
) {
    // 3 buttons: Retry, Next, Home - always show all 3
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Retry/Replay Button
        CircularActionButton(
            icon = Icons.Default.Refresh,
            backgroundColor = Color(0xFFFF9800),
            onClick = onReplay
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Next Level Button (enabled or disabled based on availability)
        if (onNextLevel != null) {
            CircularActionButton(
                icon = Icons.Default.ArrowForward,
                backgroundColor = Color(0xFF4CAF50),
                onClick = onNextLevel
            )
        } else {
            // Disabled next button
            CircularActionButton(
                icon = Icons.Default.ArrowForward,
                backgroundColor = Color(0xFF9E9E9E).copy(alpha = 0.5f),
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Home Button
        CircularActionButton(
            icon = Icons.Default.Home,
            backgroundColor = Color(0xFF2196F3),
            onClick = onHome
        )
    }
}

@Composable
fun AdventureButtons(
    onReplay: () -> Unit,
    onNextLevel: (() -> Unit)?,
    onHome: () -> Unit
) {
    // Same as Arcade Levels - 3 buttons with all navigation
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Replay Button
        CircularActionButton(
            icon = Icons.Default.Refresh,
            backgroundColor = Color(0xFFFF9800),
            onClick = onReplay
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Next Level Button
        if (onNextLevel != null) {
            CircularActionButton(
                icon = Icons.Default.ArrowForward,
                backgroundColor = Color(0xFF4CAF50),
                onClick = onNextLevel
            )
        } else {
            CircularActionButton(
                icon = Icons.Default.ArrowForward,
                backgroundColor = Color(0xFF9E9E9E).copy(alpha = 0.5f),
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Home Button
        CircularActionButton(
            icon = Icons.Default.Home,
            backgroundColor = Color(0xFF2196F3),
            onClick = onHome
        )
    }
}

@Composable
fun StarsDisplay(stars: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(3) { index ->
            AnimatedStar(
                isEarned = index < stars,
                delay = index * 200L
            )
        }
    }
}

@Composable
fun AnimatedStar(isEarned: Boolean, delay: Long) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        delay(delay)
        visible = true
    }

    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = null,
        tint = if (isEarned) Color(0xFFFFC107) else Color.White.copy(alpha = 0.3f),
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .padding(4.dp)
    )
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
fun CircularActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .shadow(8.dp, CircleShape),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

fun formatTimeForDialog(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}