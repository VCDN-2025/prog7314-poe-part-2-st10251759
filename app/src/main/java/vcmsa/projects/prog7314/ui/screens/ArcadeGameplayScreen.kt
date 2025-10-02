package vcmsa.projects.prog7314.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.models.GameCard
import vcmsa.projects.prog7314.game.GameConfig
import vcmsa.projects.prog7314.ui.viewmodels.ArcadeGameViewModel

@Composable
fun ArcadeGameplayScreen(
    levelNumber: Int,
    isArcadeMode: Boolean = false,
    onBackClick: () -> Unit,
    onGameComplete: (stars: Int, score: Int, time: Int, moves: Int, bonus: Int) -> Unit,
    viewModel: ArcadeGameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val moves by viewModel.moves.collectAsState()
    val score by viewModel.score.collectAsState()
    val isGameComplete by viewModel.isGameComplete.collectAsState()
    val config = GameConfig.getLevelConfig(levelNumber)

    // Initialize game
    LaunchedEffect(levelNumber, isArcadeMode) {
        viewModel.initializeGame(levelNumber, isArcadeMode)
    }

    // Handle game completion
    LaunchedEffect(isGameComplete) {
        if (isGameComplete) {
            val finalScore = viewModel.getFinalScore()
            onGameComplete(
                finalScore.stars,
                finalScore.finalScore,
                timeElapsed,
                finalScore.moves,
                finalScore.timeBonus
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00BCD4),
                        Color(0xFF0288D1)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Game Header
            GameHeaderBar(
                levelNumber = if (isArcadeMode) "ARCADE" else "LEVEL $levelNumber",
                score = score,
                time = if (config.timeLimit > 0) formatTime(timeRemaining) else formatTime(timeElapsed),
                moves = moves,
                maxMoves = config.maxMoves,
                onBackClick = onBackClick,
                onPauseClick = { /* Handle pause */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            val progress = if (gameState.totalPairs > 0) {
                gameState.matchedPairs.toFloat() / gameState.totalPairs.toFloat()
            } else {
                0f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cards Grid - FIXED
            LazyVerticalGrid(
                columns = GridCells.Fixed(config.gridColumns),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // FIXED: Pass the list directly to items()
                items(
                    items = gameState.cards,
                    key = { card -> card.id }
                ) { card ->
                    GameCardItem(
                        card = card,
                        onClick = {
                            viewModel.onCardClick(card.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GameHeaderBar(
    levelNumber: String,
    score: Int,
    time: String,
    moves: Int,
    maxMoves: Int,
    onBackClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF2196F3)
            )
        }

        // Level name
        Text(
            text = levelNumber,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2196F3)
        )

        // Score
        StatBadge(
            label = "POINTS",
            value = score.toString(),
            color = Color(0xFF2196F3)
        )

        // Time
        StatBadge(
            label = "TIME",
            value = time,
            color = Color(0xFF2196F3)
        )

        // Moves
        StatBadge(
            label = "MOVES",
            value = "$moves",
            color = Color(0xFFFF9800)
        )

        // Menu button
        IconButton(
            onClick = onPauseClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
fun GameCardItem(
    card: GameCard,
    onClick: () -> Unit
) {
    var isFlipping by remember { mutableStateOf(false) }

    // Flip animation
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(300),
        finishedListener = { isFlipping = false },
        label = "card_rotation"
    )

    LaunchedEffect(card.isFlipped, card.isMatched) {
        if (card.isFlipped || card.isMatched) {
            isFlipping = true
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.75f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !card.isFlipped && !card.isMatched && !isFlipping) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // Card back
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF4A90E2), RoundedCornerShape(12.dp))
                    .graphicsLayer { rotationY = 0f }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.blue_card_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Card front
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .graphicsLayer { rotationY = 180f }
            ) {
                Image(
                    painter = painterResource(id = card.imageResId),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}