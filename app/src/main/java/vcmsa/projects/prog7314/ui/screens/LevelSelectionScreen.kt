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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.entities.LevelProgressEntity
import vcmsa.projects.prog7314.ui.viewmodels.LevelSelectionViewModel
/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/

/**
 * LevelSelectionScreen
 *
 * Composable screen that allows the user to select a game level in the memory match game.
 * Displays levels in a 4-column grid, showing completion status, stars, and locked/unlocked state.
 *
 * Key Components & Features:
 *
 * 1. Screen Layout:
 *    - Box with vertical gradient background (blue shades).
 *    - Column contains back button, logo/title, levels grid, and progress section.
 *
 * 2. Back Button:
 *    - Top-left positioned IconButton for navigation back.
 *    - Styled with semi-transparent white background and rounded corners.
 *
 * 3. Logo and Titles:
 *    - Game logo displayed at top, made slightly larger for emphasis.
 *    - Title text indicates "Select Level" with shadow effect for visibility.
 *    - Subtitle prompts user to choose their adventure.
 *
 * 4. Levels Grid:
 *    - Uses LazyVerticalGrid with 4 fixed columns.
 *    - Each level represented by LevelCard composable:
 *        • Shows level number if unlocked.
 *        • Displays lock icon if locked.
 *        • Shows 1-3 stars if level completed.
 *        • Card color and shadow vary based on completion/unlocked state.
 *        • Clickable if level is unlocked, triggers onLevelClick callback.
 *
 * 5. Progress Section:
 *    - Displays completed levels out of total levels.
 *    - Includes a progress bar with horizontal gradient fill.
 *    - Provides visual feedback on player's overall progress in adventure mode.
 *
 * 6. Animations & Styling:
 *    - Level cards scale slightly when unlocked for subtle animation.
 *    - Gradient backgrounds, rounded corners, and shadows enhance UI aesthetics.
 *
 * Overall Purpose:
 * Provides a clear, engaging interface for selecting levels, showing progress,
 * and visually communicating which levels are locked, completed, or unlocked.
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectionScreen(
    onBackClick: () -> Unit,
    onLevelClick: (Int) -> Unit,
    viewModel: LevelSelectionViewModel = viewModel()
) {
    val levelsProgress by viewModel.levelsProgress.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val totalLevels = 16

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A90E2),
                        Color(0xFF5BA3F5)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with back button - MOVED DOWN
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color(0xFF4A90E2)
                    )
                }
            }

            // Logo and Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo - MADE BIGGER
                Image(
                    painter = painterResource(id = R.drawable.transparent_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(120.dp)
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.select_level),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )

                Text(
                    text = stringResource(R.string.choose_your_adventure),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Levels Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(levelsProgress) { level ->
                    LevelCard(
                        level = level,
                        onClick = { if (level.isUnlocked) onLevelClick(level.levelNumber) }
                    )
                }
            }

            // Progress Section
            ProgressSection(
                completedCount = completedCount,
                totalLevels = totalLevels,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun LevelCard(
    level: LevelProgressEntity,
    onClick: () -> Unit
) {
    val cardColor = when {
        level.isCompleted -> Color(0xFFE91E63)
        level.isUnlocked -> Color(0xFFE91E63)
        else -> Color(0xFF64748B)
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (level.isUnlocked) 1f else 0.95f,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(
                elevation = if (level.isUnlocked) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .background(cardColor, RoundedCornerShape(16.dp))
            .clickable(enabled = level.isUnlocked) { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Level number or lock icon
            if (level.isUnlocked) {
                Text(
                    text = level.levelNumber.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                // Stars display
                if (level.isCompleted && level.stars > 0) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        repeat(3) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < level.stars) Color(0xFFFFC107) else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Locked level
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.locked),
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = level.levelNumber.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ProgressSection(
    completedCount: Int,
    totalLevels: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.progress_levels, completedCount, totalLevels),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = completedCount.toFloat() / totalLevels.toFloat())
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFC107), Color(0xFFFFD54F))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}