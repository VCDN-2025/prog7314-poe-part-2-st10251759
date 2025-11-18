package vcmsa.projects.prog7314.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vcmsa.projects.prog7314.R

/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/

/**
 * ArcadeModeScreen
 *
 * This Composable represents the main arcade mode menu screen.
 * Users can navigate back, start a random arcade game, or view progressive levels.
 *
 * Parameters:
 * - onBackClick: () -> Unit -> Callback for the back button.
 * - onPlayArcade: () -> Unit -> Callback when the "Play Arcade" button is pressed.
 * - onLevelsClick: () -> Unit -> Callback when the "Levels" button is pressed.
 *
 * Key Concepts:
 * 1. Layout:
 *    - Uses a Box as the main container with a vertical gradient background.
 *    - Content is organized in a Column with top and bottom spacing using Spacer.
 *    - Aligns all children horizontally centered.
 *
 * 2. Back Button:
 *    - Positioned at the top-left using a Row.
 *    - Styled with white semi-transparent background and rounded corners.
 *    - Calls onBackClick when pressed.
 *
 * 3. Logo & Title:
 *    - Displays a larger transparent logo image.
 *    - Title text uses large font size, bold weight, white color, centered, and drop shadow for readability.
 *
 * 4. Action Buttons:
 *    - Two buttons: "Play Arcade" and "Levels", implemented with ArcadeButton3D() for 3D visual effect.
 *    - ArcadeButton3D creates a shadow layer behind the button to simulate depth.
 *    - Buttons use customizable background and shadow colors.
 *    - Each button displays a main title (text) and a smaller subtitle describing the action.
 *    - onClick triggers respective callbacks.
 *
 * 5. Responsiveness:
 *    - Spacers with weight adjust spacing dynamically.
 *    - Buttons and images fill available width with proper padding.
 *
 * ArcadeButton3D:
 * - Creates a 3D button effect with shadow layer behind the main button.
 * - Main button has no elevation; shadow is simulated by an offset colored box.
 * - Texts are centered and styled for visibility.
 *
 * Design Notes:
 * - Colors chosen for arcade theme: cyan gradient background, yellow/orange for "Play Arcade", purple for "Levels".
 * - Rounded corners and drop shadows enhance modern, playful UI feel.
 * - Modular design allows reuse of ArcadeButton3D for other screens if needed.
 */


@Composable
fun ArcadeModeScreen(
    onBackClick: () -> Unit,
    onPlayArcade: () -> Unit,
    onLevelsClick: () -> Unit
) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button - matching multiplayer screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Logo - BIGGER like before
            Image(
                painter = painterResource(id = R.drawable.transparent_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            // Title
            Text(
                text = stringResource(R.string.arcade_mode_title),
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Play Arcade Button with 3D effect
            ArcadeButton3D(
                text = stringResource(R.string.play_arcade),
                subtitle = stringResource(R.string.random_quickplay),
                backgroundColor = Color(0xFFFFC107),
                shadowColor = Color(0xFFCC8800),
                onClick = onPlayArcade
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Levels Button with 3D effect
            ArcadeButton3D(
                text = stringResource(R.string.levels),
                subtitle = stringResource(R.string.progressive_challenges),
                backgroundColor = Color(0xFF9C27B0),
                shadowColor = Color(0xFF6A1B9A),
                onClick = onLevelsClick
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ArcadeButton3D(
    text: String,
    subtitle: String,
    backgroundColor: Color,
    shadowColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        // 3D Shadow layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .offset(y = 8.dp)
                .background(
                    color = shadowColor,
                    shape = RoundedCornerShape(16.dp)
                )
        )

        // Main button
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}