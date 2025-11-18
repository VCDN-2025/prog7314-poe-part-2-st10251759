package vcmsa.projects.prog7314.ui.screens

import android.util.Log
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

/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/


/**
 * ArcadeGameplayScreen
 *
 * This Composable renders the main gameplay screen for the arcade or level mode.
 * It handles UI, game state, animations, and completion logic.
 *
 * Parameters:
 * - levelNumber: Int -> The current level number for the game.
 * - isArcadeMode: Boolean -> Flag for whether the game is in arcade mode.
 * - gameInstanceKey: Int -> Unique key to force ViewModel recreation (used for retries).
 * - onBackClick: () -> Unit -> Callback when the back button is pressed.
 * - onGameComplete: (stars, score, time, moves, bonus) -> Unit -> Callback triggered when the game completes.
 * - cardBackgroundViewModel: CardBackgroundViewModel -> Provides the card background drawable.
 *
 * Key Concepts:
 * 1. ViewModel Management:
 *    - Uses a dynamic key with remember() to force recreation of ArcadeGameViewModel on retry or new game instance.
 *    - Collects game state and metrics (score, time, moves, matched pairs) via collectAsState().
 *
 * 2. Game Initialization:
 *    - LaunchedEffect is used to initialize the game whenever level, mode, or gameInstanceKey changes.
 *    - Resets hasCalledCompletionCallback to prevent duplicate completion events.
 *
 * 3. Game Completion Handling:
 *    - Monitors isGameComplete and matchedPairs to trigger onGameComplete callback exactly once.
 *    - Saves last play date for streak tracking.
 *    - All other notifications and repository updates are handled by the repositories, not the Composable.
 *
 * 4. UI Structure:
 *    - Background: Vertical gradient from cyan to dark blue.
 *    - Header Bar: Displays level/arcade label, score, time, and moves using GameHeaderBar().
 *    - Progress Bar: Linear progress based on matched pairs.
 *    - Cards Grid: LazyVerticalGrid showing all cards, with flip animations.
 *
 * 5. Card Flip Logic:
 *    - Animate card rotation with animateFloatAsState() from 0 to 180 degrees.
 *    - Front side shows card image; back side shows background drawable.
 *    - Matched cards have green border and light green background.
 *
 * 6. Helper Functions:
 *    - formatTime(): Converts seconds into MM:SS format for display.
 *
 * Notes:
 * - Uses Compose state management (remember, LaunchedEffect, collectAsState) to reactively update UI.
 * - Ensures no duplicate game completion callbacks using hasCalledCompletionCallback flag.
 * - All gameplay logic (matching cards, calculating score, tracking time/moves) is handled inside ArcadeGameViewModel.
 * - UI is fully responsive and adapts to grid size and device screen size.
 */

@Composable
fun ArcadeGameplayScreen(
    levelNumber: Int,
    isArcadeMode: Boolean = false,
    gameInstanceKey: Int = 0,  // Key to force ViewModel refresh
    onBackClick: () -> Unit,
    onGameComplete: (stars: Int, score: Int, time: Int, moves: Int, bonus: Int) -> Unit,
    cardBackgroundViewModel: CardBackgroundViewModel = viewModel()
) {
    val context = LocalContext.current

    // Include gameInstanceKey in the remember key to force new ViewModel on retry
    val viewModelKey = remember(levelNumber, isArcadeMode, gameInstanceKey) {
        "arcade_${levelNumber}_${isArcadeMode}_${gameInstanceKey}_${System.currentTimeMillis()}"
    }

    // Use the stable key
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

    // Initialize game when level, mode, or gameInstanceKey changes
    LaunchedEffect(levelNumber, isArcadeMode, gameInstanceKey) {
        hasCalledCompletionCallback = false
        viewModel.initializeGame(levelNumber, isArcadeMode)
    }

    // Handle game completion - FIXED: Removed duplicate notifications
    LaunchedEffect(isGameComplete, gameState.matchedPairs) {
        if (isGameComplete &&
            gameState.matchedPairs > 0 &&
            gameState.cards.isNotEmpty() &&
            !hasCalledCompletionCallback) {

            hasCalledCompletionCallback = true
            val finalScore = viewModel.getFinalScore()

            // 🔥 Save last play date for streak tracking
            Log.d("ArcadeGameplay", "🎮 Game completed! Saving last play date...")
            LocalNotificationManager.saveLastPlayDate(context)
            Log.d("ArcadeGameplay", "✅ Last play date should be saved now")

            // All notifications are now handled by repositories
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
//Formating the unit of time into seconds
fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}