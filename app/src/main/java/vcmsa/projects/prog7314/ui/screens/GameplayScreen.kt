package vcmsa.projects.prog7314.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.models.GameCard
import vcmsa.projects.prog7314.data.models.GameResult
import vcmsa.projects.prog7314.data.models.GameTheme
import vcmsa.projects.prog7314.data.models.GridSize
import vcmsa.projects.prog7314.ui.viewmodels.GameViewModel
import vcmsa.projects.prog7314.ui.viewmodels.CardBackgroundViewModel

@Composable
fun GameplayScreen(
    theme: GameTheme,
    gridSize: GridSize,
    onBackClick: () -> Unit,
    onGameComplete: () -> Unit,
    viewModel: GameViewModel = viewModel(),
    cardBackgroundViewModel: CardBackgroundViewModel = viewModel()
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsState()
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val moves by viewModel.moves.collectAsState()
    val points by viewModel.points.collectAsState()
    val isGameComplete by viewModel.isGameComplete.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()

    // Get card background drawable
    val cardBackgroundDrawable by cardBackgroundViewModel.cardBackgroundDrawable.collectAsState()

    // Initialize game once
    LaunchedEffect(theme, gridSize) {
        viewModel.initializeGame(theme, gridSize)
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
                .padding(16.dp)
        ) {
            // Header with stats
            GameHeader(
                points = points,
                time = formatTime(timeElapsed),
                moves = moves,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game grid - Updated to pass cardBackgroundDrawable
            gameState?.let { state ->
                GameGrid(
                    cards = state.cards,
                    gridColumns = gridSize.columns,
                    cardBackgroundDrawable = cardBackgroundDrawable,
                    onCardClick = { card ->
                        viewModel.onCardClicked(card)
                    }
                )
            }
        }

        // Show completion popup
        if (isGameComplete && gameResult != null) {
            GameCompletionPopup(
                gameResult = gameResult!!,
                onReplay = {
                    viewModel.resetGame()
                },
                onNext = {
                    // TODO: Navigate to next level
                    onGameComplete()
                },
                onHome = {
                    onGameComplete()
                },
                onDismiss = {
                    // Optional: Handle dismissal
                }
            )
        }
    }
}

@Composable
fun GameHeader(
    points: Int,
    time: String,
    moves: Int,
    onBackClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE91E63), shape = RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    color = Color.White
                )
            }

            // Stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Points
                StatItem(
                    label = stringResource(R.string.points_label),
                    value = points.toString(),
                    color = Color(0xFFFFEB3B)
                )

                // Time
                StatItem(
                    label = stringResource(R.string.time_label),
                    value = time,
                    color = Color(0xFF2196F3)
                )

                // Moves
                StatItem(
                    label = stringResource(R.string.moves_label),
                    value = moves.toString(),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun StatItem(
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
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun GameGrid(
    cards: List<GameCard>,
    gridColumns: Int,
    cardBackgroundDrawable: Int,
    onCardClick: (GameCard) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(cards) { card ->
            FlipCard(
                card = card,
                cardBackgroundDrawable = cardBackgroundDrawable,
                onClick = { onCardClick(card) }
            )
        }
    }
}

@Composable
fun FlipCard(
    card: GameCard,
    cardBackgroundDrawable: Int,
    onClick: () -> Unit
) {
    var rotated by remember { mutableStateOf(false) }

    LaunchedEffect(card.isFlipped) {
        rotated = card.isFlipped
    }

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "card_rotation"
    )

    val animateFront = rotation <= 90f

    Card(
        modifier = Modifier
            .aspectRatio(0.75f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(
                enabled = !card.isMatched && !card.isFlipped
            ) {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (animateFront) {
                // Card back - use selected background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF64B5F6),
                                    Color(0xFF2196F3)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (cardBackgroundDrawable != 0) {
                        Image(
                            painter = painterResource(id = cardBackgroundDrawable),
                            contentDescription = "Card back",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback to default
                        Image(
                            painter = painterResource(id = R.drawable.blue_card_background),
                            contentDescription = "Card back",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                // Card front (flipped)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                        }
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(id = card.imageResId),
                        contentDescription = "Card image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Matched overlay
            if (card.isMatched) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF4CAF50).copy(alpha = 0.3f))
                )
            }
        }
    }
}

/**
 * Format time in MM:SS
 */
fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}