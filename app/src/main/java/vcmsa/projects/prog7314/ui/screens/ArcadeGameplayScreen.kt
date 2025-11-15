package vcmsa.projects.prog7314.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.models.GameCard
import vcmsa.projects.prog7314.game.GameConfig
import vcmsa.projects.prog7314.ui.viewmodels.ArcadeGameViewModel
import vcmsa.projects.prog7314.ui.viewmodels.CardBackgroundViewModel
import vcmsa.projects.prog7314.utils.LocalNotificationManager

@Composable
fun ArcadeGameplayScreen(
    levelNumber: Int,
    isArcadeMode: Boolean = false,
    onBackClick: () -> Unit,
    onGameComplete: (stars: Int, score: Int, time: Int, moves: Int, bonus: Int) -> Unit,
    cardBackgroundViewModel: CardBackgroundViewModel = viewModel()
) {
    val context = LocalContext.current

    // CRITICAL FIX: Generate stable key only once per screen instance using remember
    val viewModelKey = remember(levelNumber, isArcadeMode) {
        "arcade_${levelNumber}_${isArcadeMode}_${System.currentTimeMillis()}"
    }

    // FIXED: Use the stable key
    val viewModel: ArcadeGameViewModel = viewModel(key = viewModelKey)

    val gameState by viewModel.gameState.collectAsState()
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val moves by viewModel.moves.collectAsState()
    val score by viewModel.score.collectAsState()
    val isGameComplete by viewModel.isGameComplete.collectAsState()
    val config = GameConfig.getLevelConfig(levelNumber)

    // Get card background drawable
    val cardBackgroundDrawable by cardBackgroundViewModel.cardBackgroundDrawable.collectAsState()

    // Track if we've already called the completion callback
    var hasCalledCompletionCallback by remember { mutableStateOf(false) }

    // Initialize game when level or mode changes
    LaunchedEffect(levelNumber, isArcadeMode) {
        hasCalledCompletionCallback = false
        viewModel.initializeGame(levelNumber, isArcadeMode)
    }

    // Handle game completion with comprehensive checks
    LaunchedEffect(isGameComplete, gameState.matchedPairs) {
        if (isGameComplete &&
            gameState.matchedPairs > 0 &&
            gameState.cards.isNotEmpty() &&
            !hasCalledCompletionCallback) {

            hasCalledCompletionCallback = true
            val finalScore = viewModel.getFinalScore()

            // Trigger notifications
            val prefs = context.getSharedPreferences("game_prefs", android.content.Context.MODE_PRIVATE)
            val hasCompletedFirstLevel = prefs.getBoolean("has_completed_first_level", false)

            // Check if this is the first level ever completed
            if (!hasCompletedFirstLevel) {
                LocalNotificationManager.notifyFirstLevelCompleted(context)
                prefs.edit().putBoolean("has_completed_first_level", true).apply()
            }

            // Check for high score
            val previousBest = prefs.getInt("best_score_level_$levelNumber", 0)
            if (finalScore.finalScore > previousBest && previousBest > 0) {
                LocalNotificationManager.notifyNewHighScore(
                    context,
                    finalScore.finalScore,
                    previousBest
                )
            }
            // Save new best score
            if (finalScore.finalScore > previousBest) {
                prefs.edit().putInt("best_score_level_$levelNumber", finalScore.finalScore).apply()
            }

            // Check if next level should be unlocked
            if (!isArcadeMode && finalScore.stars >= 1 && levelNumber < 16) {
                val nextLevelUnlocked = prefs.getBoolean("level_${levelNumber + 1}_unlocked", false)
                if (!nextLevelUnlocked) {
                    LocalNotificationManager.notifyLevelUnlocked(context, levelNumber + 1)
                    prefs.edit().putBoolean("level_${levelNumber + 1}_unlocked", true).apply()
                }
            }

            // Check for achievement: Perfect game (3 stars)
            if (finalScore.stars == 3) {
                val perfectAchievement = prefs.getBoolean("achievement_perfect_level_$levelNumber", false)
                if (!perfectAchievement) {
                    LocalNotificationManager.notifyAchievementUnlocked(
                        context,
                        "Perfect Performance",
                        "You earned 3 stars on Level $levelNumber!"
                    )
                    prefs.edit().putBoolean("achievement_perfect_level_$levelNumber", true).apply()
                }
            }

            // Save last play date for streak tracking
            LocalNotificationManager.saveLastPlayDate(context)

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
                .padding(top = 32.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            // Game Header
            GameHeaderBar(
                levelNumber = if (isArcadeMode) "ARCADE" else "LEVEL $levelNumber",
                score = score,
                time = if (config.timeLimit > 0) formatTime(timeRemaining) else formatTime(timeElapsed),
                moves = moves,
                maxMoves = config.maxMoves,
                onBackClick = onBackClick
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

            // Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(config.gridColumns),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(
                    items = gameState.cards,
                    key = { card -> card.id }
                ) { card ->
                    GameCardItem(
                        card = card,
                        cardBackgroundDrawable = cardBackgroundDrawable,
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
    onBackClick: () -> Unit
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
    cardBackgroundDrawable: Int,
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
            // FIXED: Add green border for matched cards
            .then(
                if (card.isMatched && rotation > 90f) {
                    Modifier.border(
                        width = 4.dp,
                        color = Color(0xFF4CAF50), // Green border
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            )
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
                if (cardBackgroundDrawable != 0) {
                    Image(
                        painter = painterResource(id = cardBackgroundDrawable),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.blue_card_background),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            // Card front - FIXED: Add green background for matched cards
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        // FIXED: Light green background for matched cards
                        if (card.isMatched) Color(0xFFE8F5E9) else Color.White,
                        RoundedCornerShape(12.dp)
                    )
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