package vcmsa.projects.prog7314.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.res.stringResource
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

    val cardBackgroundDrawable by cardBackgroundViewModel.cardBackgroundDrawable.collectAsState()

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
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            gameState?.let { state ->
                MultiplayerGameHeader(
                    player1 = state.player1,
                    player2 = state.player2,
                    timeElapsed = timeElapsed,
                    onPauseClick = { viewModel.togglePause() },
                    isPaused = isPaused
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

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

        if (isPaused) {
            MultiplayerPauseDialog(
                onResume = { viewModel.togglePause() },
                onHome = onHomeClick
            )
        }

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
            .height(48.dp)
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player 1 - Compact Badge Style
        PlayerBadge(
            player = player1,
            modifier = Modifier.weight(1f)
        )

        // Center Controls - Timer & Pause
        Row(
            modifier = Modifier
                .background(
                    Color(0xFFF5F5F5),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Timer
            Text(
                text = formatTime(timeElapsed),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Pause Button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        Color(0xFFFFC107),
                        CircleShape
                    )
                    .clickable(onClick = onPauseClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Player 2 - Compact Badge Style
        PlayerBadge(
            player = player2,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PlayerBadge(
    player: vcmsa.projects.prog7314.data.models.Player,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(
                color = if (player.isCurrentTurn)
                    player.color.copy(alpha = 0.2f)
                else
                    Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Color Indicator with Glow Effect
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shadow(
                        elevation = if (player.isCurrentTurn) 4.dp else 0.dp,
                        shape = CircleShape,
                        spotColor = player.color
                    )
                    .background(
                        player.color,
                        CircleShape
                    )
            ) {
                // Pulsing indicator for current turn
                if (player.isCurrentTurn) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.White.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Score
            Text(
                text = "${player.score}",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF333333)
            )
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
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 2.dp)
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
            if (cardBackgroundDrawable != 0) {
                Image(
                    painter = painterResource(id = cardBackgroundDrawable),
                    contentDescription = "Card Back",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .shadow(2.dp, RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.blue_card_background),
                    contentDescription = "Card Back",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .shadow(2.dp, RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Image(
                painter = painterResource(id = card.imageResId),
                contentDescription = "Card",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .shadow(2.dp, RoundedCornerShape(6.dp))
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
                        RoundedCornerShape(6.dp)
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
                    text = stringResource(R.string.paused),
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
                        text = stringResource(R.string.resume),
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
                        contentDescription = stringResource(R.string.home),
                        tint = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home),
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
                        PlayerColor.RED -> stringResource(R.string.player_1_wins)
                        PlayerColor.BLUE -> stringResource(R.string.player_2_wins)
                        null -> stringResource(R.string.its_a_tie)
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
                            text = stringResource(R.string.player_1),
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
                        text = stringResource(R.string.vs),
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
                            text = stringResource(R.string.player_2),
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
                        MultiplayerStatRow(stringResource(R.string.time), formatTime(result.timeTaken))
                        MultiplayerStatRow(stringResource(R.string.total_moves), "${result.totalMoves}")
                        MultiplayerStatRow(stringResource(R.string.theme), result.theme.themeName)
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
                        text = stringResource(R.string.rematch),
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
                        contentDescription = stringResource(R.string.home),
                        tint = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home),
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
