package vcmsa.projects.prog7314.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.models.DifficultyLevel
import vcmsa.projects.prog7314.data.models.GridSize
/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/

/**
 * GridSizeSelectionScreen
 *
 * Composable screen that allows the user to select the grid size and difficulty level
 * for the memory card matching game.
 *
 * Key Components & Features:
 *
 * 1. Screen Layout:
 *    - Uses a Box with a vertical gradient background (cyan to blue).
 *    - Column contains the back button, logo/title, subtitle, and grid size options.
 *
 * 2. Back Button:
 *    - Top-left positioned IconButton to navigate back.
 *    - Styled with semi-transparent white background and rounded corners.
 *
 * 3. Logo and Titles:
 *    - Displays game logo at the top.
 *    - Subtitle prompts the user to choose a grid size and difficulty level.
 *    - Text styling uses font size, weight, color, and letter spacing for visual hierarchy.
 *
 * 4. Grid Size Options:
 *    - Arranged in a 2x2 layout (beginner/intermediate and hard/expert rows).
 *    - Each option represented with GridSizeCardStyled:
 *        • Displays difficulty label, card icon, and grid size with total cards.
 *        • Color-coded based on difficulty (green, yellow, orange, red).
 *        • Clickable to trigger onGridSizeSelected callback.
 *
 * 5. GridSizeCardStyled:
 *    - Card with rounded corners, elevation, and gradient/solid background.
 *    - Top: difficulty label with small colored indicator.
 *    - Center: card back icon styled with gradient blue background.
 *    - Bottom: grid size and total cards information.
 *    - All elements aligned and spaced for consistent visual design.
 *
 * 6. Accessibility & UX:
 *    - Text and colors ensure readability against background.
 *    - Card click areas large enough for easy selection.
 *    - Consistent design with theme and other screens (back button, titles, logos).
 *
 * Overall Purpose:
 * Provides an intuitive, visually appealing interface for selecting game difficulty
 * and grid size before starting a memory match game session.
 */


@Composable
fun GridSizeSelectionScreen(
    onGridSizeSelected: (GridSize) -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00BCD4), // Cyan
                        Color(0xFF0288D1)  // Blue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Back button - matching ThemeSelectionScreen style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.transparent_logo),
                    contentDescription = "Memory Match Madness Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.choose_grid_size),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Text(
                    text = stringResource(R.string.pick_challenge_level),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Grid Options - 2x2 layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: BEGINNER and INTERMEDIATE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // BEGINNER - 3x2 (Green)
                    GridSizeCardStyled(
                        gridSize = GridSize(3, 2, DifficultyLevel.BEGINNER),
                        backgroundColor = Color(0xFF8BC34A), // Light Green
                        labelColor = Color(0xFF4CAF50), // Green
                        difficultyLabel = stringResource(R.string.beginner),
                        onClick = onGridSizeSelected,
                        modifier = Modifier.weight(1f)
                    )

                    // INTERMEDIATE - 3x4 (Yellow)
                    GridSizeCardStyled(
                        gridSize = GridSize(4, 3, DifficultyLevel.INTERMEDIATE),
                        backgroundColor = Color(0xFFFFEB3B), // Yellow
                        labelColor = Color(0xFFFFC107), // Amber
                        difficultyLabel = stringResource(R.string.intermediate),
                        onClick = onGridSizeSelected,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: HARD and EXPERT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // HARD - 4x5 (Orange)
                    GridSizeCardStyled(
                        gridSize = GridSize(5, 4, DifficultyLevel.INTERMEDIATE),
                        backgroundColor = Color(0xFFFF9800), // Orange
                        labelColor = Color(0xFFF57C00), // Dark Orange
                        difficultyLabel = stringResource(R.string.hard),
                        onClick = onGridSizeSelected,
                        modifier = Modifier.weight(1f)
                    )

                    // EXPERT - 4x6 (Red)
                    GridSizeCardStyled(
                        gridSize = GridSize(6, 4, DifficultyLevel.EXPERT),
                        backgroundColor = Color(0xFFF44336), // Red
                        labelColor = Color(0xFFE91E63), // Pink-Red
                        difficultyLabel = stringResource(R.string.expert),
                        onClick = onGridSizeSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun GridSizeCardStyled(
    gridSize: GridSize,
    backgroundColor: Color,
    labelColor: Color,
    difficultyLabel: String,
    onClick: (GridSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardsText = stringResource(R.string.cards)

    Card(
        modifier = modifier
            .height(220.dp)
            .clickable { onClick(gridSize) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Difficulty Label at top
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(labelColor, shape = RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = difficultyLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                // Card Icon in center
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF64B5F6), // Light Blue
                                    Color(0xFF2196F3)  // Blue
                                )
                            )
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Blue card back design
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1976D2))
                    )
                }

                // Grid size and card count at bottom
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${gridSize.rows} X ${gridSize.columns}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${gridSize.totalCards} $cardsText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}