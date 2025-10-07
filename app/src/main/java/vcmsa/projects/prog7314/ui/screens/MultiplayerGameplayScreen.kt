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
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.models.GameCard
import vcmsa.projects.prog7314.data.models.GameTheme
import vcmsa.projects.prog7314.data.models.PlayerColor
import vcmsa.projects.prog7314.ui.viewmodels.MultiplayerViewModel
import vcmsa.projects.prog7314.ui.viewmodels.CardBackgroundViewModel


@Composable
fun MultiplayerGameplayScreen(
    theme: GameTheme,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    viewModel: MultiplayerViewModel = viewModel(),
    cardBackgroundViewModel: CardBackgroundViewModel = viewModel()
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsState()
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val totalMoves by viewModel.totalMoves.collectAsState()
    val isGameComplete by viewModel.isGameComplete.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()

    // Get card background drawable
    val cardBackgroundDrawable by cardBackgroundViewModel.cardBackgroundDrawable.collectAsState()

    // Initialize game
    LaunchedEffect(theme) {
        viewModel.initializeGame(theme)
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
            // Game Header
            gameState?.let { state ->
                MultiplayerGameHeader(
                    player1 = state.player1,
                    player2 = state.player2,
                    timeElapsed = timeElapsed,
                    onPauseClick = { viewModel.togglePause() },
                    isPaused = isPaused
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Grid - Updated to pass cardBackgroundDrawable
            gameState?.let { state ->
                MultiplayerGameGrid(
                    cards = state.cards,
                    cardBackgroundDrawable = cardBackgroundDrawable,
                    onCardClick = { card ->
                        if (!isPaused) {
                            viewModel.onCardClick(card.id)
                        }
                    },
                    enabled = !isPaused
                )
            }
        }

        // Pause Dialog
        if (isPaused) {
            MultiplayerPauseDialog(
                onResume = { viewModel.togglePause() },
                onHome = onHomeClick
            )
        }

        // Game Complete Dialog
        if (isGameComplete) {
            val result = viewModel.getGameResult()
            MultiplayerWinnerDialog(
                result = result,
                onRematch = {
                    viewModel.initializeGame(theme)
                },
                onHome = onHomeClick
            )
        }
    }
}

@Composable
fun MultiplayerGameHeader(
    player1: vcmsa.projects.prog7314.data.models.Player,
    player2: vcmsa.projects.prog7314.data.models.Player,
    timeElapsed: Int,
    onPauseClick: () -> Unit,
    isPaused: Boolean
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
        // Player 1 (Red)
        PlayerScoreCard(
            player = player1,
            modifier = Modifier.weight(1f)
        )

        // VS + Timer + Pause
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "VS",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Timer
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱",
                    fontSize = 14.sp
                )
                Text(
                    text = formatTime(timeElapsed),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Pause Button
            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFFFC107), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = if (isPaused) "▶" else "⏸",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }

        // Player 2 (Blue)
        PlayerScoreCard(
            player = player2,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PlayerScoreCard(
    player: vcmsa.projects.prog7314.data.models.Player,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = if (player.isCurrentTurn) 6.dp else 2.dp,
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (player.isCurrentTurn)
                player.color.copy(alpha = 0.15f)
            else
                Color.White
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(player.color, RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (player.playerColor == PlayerColor.RED) "Red" else "Blue",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = player.color
            )

            Text(
                text = "${player.score}",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF333333)
            )

            if (player.isCurrentTurn) {
                Text(
                    text = "Your Turn",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = player.color
                )
            }
        }
    }
}

@Composable
fun MultiplayerGameGrid(
    cards: List<GameCard>,
    cardBackgroundDrawable: Int,
    onCardClick: (GameCard) -> Unit,
    enabled: Boolean
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = cards,
            key = { card -> card.id }
        ) { card ->
            MultiplayerCardItem(
                card = card,
                cardBackgroundDrawable = cardBackgroundDrawable,
                onClick = {
                    if (enabled) {
                        onCardClick(card)
                    }
                }
            )
        }
    }
}

@Composable
fun MultiplayerCardItem(
    card: GameCard,
    cardBackgroundDrawable: Int,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "card_flip"
    )

    Box(
        modifier = Modifier
            .aspectRatio(0.7f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(
                enabled = !card.isFlipped && !card.isMatched,
                onClick = onClick
            )
    ) {
        if (rotation <= 90f) {
            // Card back - use selected background
            if (cardBackgroundDrawable != 0) {
                Image(
                    painter = painterResource(id = cardBackgroundDrawable),
                    contentDescription = "Card Back",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.blue_card_background),
                    contentDescription = "Card Back",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Image(
                painter = painterResource(id = card.imageResId),
                contentDescription = "Card",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .graphicsLayer {
                        rotationY = 180f
                    },
                contentScale = ContentScale.Crop
            )
        }

        if (card.isMatched) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFF4CAF50).copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .graphicsLayer {
                        rotationY = 180f
                    }
            )
        }
    }
}

@Composable
fun MultiplayerPauseDialog(
    onResume: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSED",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "RESUME",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HOME",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }
            }
        }
    }
}

@Composable
fun MultiplayerWinnerDialog(
    result: vcmsa.projects.prog7314.data.models.MultiplayerGameResult,
    onRematch: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (result.winner) {
                        PlayerColor.RED -> "🏆 PLAYER 1 WINS! 🏆"
                        PlayerColor.BLUE -> "🏆 PLAYER 2 WINS! 🏆"
                        null -> "🤝 IT'S A TIE! 🤝"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when (result.winner) {
                        PlayerColor.RED -> Color(0xFFE53935)
                        PlayerColor.BLUE -> Color(0xFF1E88E5)
                        null -> Color(0xFF333333)
                    },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFE53935), RoundedCornerShape(24.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Player 1",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = "${result.player1Score}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE53935)
                        )
                    }

                    Text(
                        text = "VS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF666666),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF1E88E5), RoundedCornerShape(24.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Player 2",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = "${result.player2Score}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E88E5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        MultiplayerStatRow("Time", formatTime(result.timeTaken))
                        MultiplayerStatRow("Total Moves", "${result.totalMoves}")
                        MultiplayerStatRow("Theme", result.theme.themeName)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRematch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "REMATCH",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HOME",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }
            }
        }
    }
}

@Composable
fun MultiplayerStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
    }
}