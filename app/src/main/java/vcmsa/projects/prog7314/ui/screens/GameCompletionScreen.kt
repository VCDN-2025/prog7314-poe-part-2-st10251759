package vcmsa.projects.prog7314.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

@Composable
fun GameCompletionDialog(
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
                text = "NEW RECORD!",
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
                text = "WELL DONE!",
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
                    StatRow(label = "MOVES:", value = moves.toString(), color = Color(0xFFFFC107))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "TIME:", value = formatTime(time), color = Color(0xFFFFC107))
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "BONUS:", value = bonus.toString(), color = Color(0xFFFFC107))
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
                        text = "TOTAL:",
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

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Replay Button
                CircularActionButton(
                    icon = Icons.Default.Refresh,
                    backgroundColor = Color(0xFFFF9800),
                    onClick = onReplay
                )

                // Next Level Button (if available)
                if (onNextLevel != null) {
                    CircularActionButton(
                        icon = Icons.Default.Star,
                        backgroundColor = Color(0xFF4CAF50),
                        onClick = onNextLevel
                    )
                }

                // Home Button
                CircularActionButton(
                    icon = Icons.Default.Home,
                    backgroundColor = Color(0xFF2196F3),
                    onClick = onHome
                )
            }
        }
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
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .shadow(8.dp, CircleShape)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}