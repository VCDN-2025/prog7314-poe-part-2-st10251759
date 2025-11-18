package vcmsa.projects.prog7314.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import vcmsa.projects.prog7314.R
/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/

/**
 * LoadingScreen
 *
 * Composable screen that displays a loading interface while the game assets or data are prepared.
 * Shows a progress bar with animated fill, percentage text, and branding visuals.
 *
 * Key Components & Features:
 *
 * 1. Progress Animation:
 *    - Uses a mutable state `progress` to track load progress from 0f to 1f.
 *    - LaunchedEffect runs a coroutine to increment progress with a small delay (simulates loading).
 *    - Calls `onLoadingComplete()` callback when progress reaches 100%.
 *
 * 2. Background:
 *    - Full-screen card background image for visual appeal.
 *    - Semi-transparent black overlay for better contrast of foreground elements.
 *
 * 3. Branding:
 *    - Displays app logo prominently in the center.
 *    - Shows app name and tagline below the logo.
 *
 * 4. Progress Indicator:
 *    - LinearProgressIndicator represents the loading progress.
 *    - Progress percentage is displayed below the progress bar.
 *    - Styled with rounded corners and color gradients.
 *
 * 5. Layout:
 *    - Centered Column aligns logo, texts, and progress indicator vertically.
 *    - Responsive padding and spacing for visual balance.
 *
 * Purpose:
 * Provides users with feedback that the game is loading, enhancing user experience
 * and ensuring smooth transition to the main menu or next screen.
 */


@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit = {}
) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        for (i in 0..100) {
            progress = i / 100f
            delay(30)
        }
        delay(500)
        onLoadingComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Card background image
        Image(
            painter = painterResource(id = R.drawable.card_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo - BIGGER
            Image(
                painter = painterResource(id = R.drawable.transparent_logo),
                contentDescription = "Memory Match Madness Logo",
                modifier = Modifier
                    .size(280.dp)
                    .padding(bottom = 40.dp),
                contentScale = ContentScale.Fit
            )

            // App Name - KEPT SAME
            Text(
                text = "MEMORY MATCH MADNESS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "TEST YOUR MEMORY SKILLS!",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading progress bar - BIGGER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LOADING...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(280.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    LoadingScreen()
}