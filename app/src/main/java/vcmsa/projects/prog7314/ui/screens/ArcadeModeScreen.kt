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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vcmsa.projects.prog7314.R

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
                        contentDescription = "Back",
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
                text = "ARCADE MODE",
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
                text = "PLAY ARCADE",
                subtitle = "Random quick-play session",
                backgroundColor = Color(0xFFFFC107),
                shadowColor = Color(0xFFCC8800),
                onClick = onPlayArcade
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Levels Button with 3D effect
            ArcadeButton3D(
                text = "LEVELS",
                subtitle = "Progressive challenges 1-16",
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