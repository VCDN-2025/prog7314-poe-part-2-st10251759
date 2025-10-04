package vcmsa.projects.prog7314.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vcmsa.projects.prog7314.R

@Composable
fun MainMenuScreen(
    onArcadeModeClick: () -> Unit = {},
    onAdventureModeClick: () -> Unit = {},
    onMultiplayerClick: () -> Unit = {},
    onStatisticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    userEmail: String = "user@example.com"
) {
    var showComingSoonDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Card background image - MORE VISIBLE
        Image(
            painter = painterResource(id = R.drawable.card_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.6f
        )

        // Lighter gradient overlay so background shows
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00BCD4).copy(alpha = 0.5f),
                            Color(0xFF0288D1).copy(alpha = 0.6f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with only Settings on the right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings Button
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = "⚙",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            // Logo - BIGGER
            Image(
                painter = painterResource(id = R.drawable.transparent_logo),
                contentDescription = "Memory Match Madness Logo",
                modifier = Modifier
                    .size(220.dp)
                    .padding(bottom = 32.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game Mode Buttons with 3D effect
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Arcade Mode Button
                GameModeButton3D(
                    text = "ARCADE MODE",
                    backgroundColor = Color(0xFFFFC107),
                    shadowColor = Color(0xFFCC8800),
                    onClick = onArcadeModeClick
                )

                // Adventure Mode Button
                GameModeButton3D(
                    text = "ADVENTURE MODE",
                    backgroundColor = Color(0xFF2196F3),
                    shadowColor = Color(0xFF0D47A1),
                    onClick = onAdventureModeClick
                )

                // Multiplayer Button
                GameModeButton3D(
                    text = "MULTIPLAYER",
                    backgroundColor = Color(0xFFE91E63),
                    shadowColor = Color(0xFFAD1457),
                    onClick = onMultiplayerClick
                )

                // Statistics Button
                GameModeButton3D(
                    text = "STATISTICS",
                    backgroundColor = Color(0xFF9C27B0),
                    shadowColor = Color(0xFF6A1B9A),
                    onClick = { showComingSoonDialog = true }
                )

                // Settings Button
                GameModeButton3D(
                    text = "⚙ SETTINGS",
                    backgroundColor = Color(0xFF8BC34A),
                    shadowColor = Color(0xFF558B2F),
                    onClick = onSettingsClick
                )
            }
        }
    }

    // Coming Soon Dialog
    if (showComingSoonDialog) {
        AlertDialog(
            onDismissRequest = { showComingSoonDialog = false },
            title = {
                Text(
                    text = "Coming Soon!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0288D1)
                )
            },
            text = {
                Text(
                    text = "Statistics feature is coming soon. Stay tuned for detailed game stats, achievements, and progress tracking!",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            },
            confirmButton = {
                Button(
                    onClick = { showComingSoonDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0288D1)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("OK", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun GameModeButton3D(
    text: String,
    backgroundColor: Color,
    shadowColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        // Bottom shadow layer - STRONGER 3D EFFECT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .offset(y = 8.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    color = shadowColor
                )
        )

        // Main button with softer white border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .border(
                    width = 2.5.dp,
                    color = Color.White.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(30.dp)
                )
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxSize(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor
                ),
                shape = RoundedCornerShape(30.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    MainMenuScreen()
}