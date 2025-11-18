package vcmsa.projects.prog7314.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.models.GameTheme
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.LocalNotificationManager
import vcmsa.projects.prog7314.utils.NotificationTracker

/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/

/**
 * ThemeSelectionScreen
 *
 * A screen allowing users to select a card theme for the Memory Match Madness app.
 * Provides a visually rich interface with preview images and notifications on first-time theme selection.
 *
 * Features:
 * 1. Layout & Styling:
 *    - Gradient background matching other screens.
 *    - Back button for navigation consistency.
 *    - Logo and title/subtitle at the top.
 *
 * 2. Theme List:
 *    - LazyColumn displays all available GameTheme values.
 *    - Each item is a ThemeCard showing the preview image and theme name.
 *    - Subtle dark overlay ensures text readability over images.
 *
 * 3. ThemeCard:
 *    - Card with rounded corners and elevation.
 *    - Full-size background image of the theme.
 *    - Text overlay with shadow for visibility.
 *    - Clickable to select theme.
 *
 * 4. Notification Management:
 *    - Checks NotificationTracker to prevent duplicate notifications.
 *    - Sends a local notification on first-time theme unlock using LocalNotificationManager.
 *    - Logs notification activity for debugging.
 *
 * 5. State & Context:
 *    - Accesses current user via AuthManager.
 *    - Context used for sending notifications and fetching resources.
 *
 * 6. Key Improvements in this version:
 *    - Card background set to transparent for proper image display.
 *    - Removed unnecessary alpha from theme images.
 *    - Added dark gradient overlay to improve theme name readability.
 *    - Enhanced shadow on text for better contrast.
 *
 * Purpose:
 * To give users an interactive way to preview and select game card themes while rewarding first-time selections with notifications.
 */


@Composable
fun ThemeSelectionScreen(
    onThemeSelected: (GameTheme) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

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
            // Back button - matching other screens
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

            // Logo replacing the text
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.transparent_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = stringResource(R.string.choose_theme_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.select_card_style_subtitle),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Theme List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(GameTheme.values().toList()) { theme ->
                    ThemeCard(
                        theme = theme,
                        onClick = { onThemeSelected(theme) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: GameTheme,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val themeName = stringResource(theme.themeNameResId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable {
                // Check if notification already sent before sending
                val userId = AuthManager.getCurrentUser()?.uid
                if (userId != null) {
                    if (!NotificationTracker.hasThemeUnlockBeenSent(context, userId, themeName)) {
                        // First time selecting this theme - show notification
                        LocalNotificationManager.notifyThemeUnlocked(context, themeName)
                        NotificationTracker.markThemeUnlockAsSent(context, userId, themeName)
                        Log.d("ThemeCard", "🔔 Theme unlock notification sent for: $themeName")
                    } else {
                        Log.d("ThemeCard", "⏭️ Skipping theme notification (already sent for $themeName)")
                    }
                }

                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent  // 🔥 FIXED: Changed from blue to transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Theme image background - 🔥 FIXED: Removed alpha to show image clearly
            Image(
                painter = painterResource(id = theme.previewImage),
                contentDescription = themeName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
                // 🔥 REMOVED: alpha = 0.3f
            )

            // Dark overlay for text readability - 🔥 NEW: Added subtle dark gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Theme name text
            Text(
                text = themeName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),  // 🔥 Increased shadow for better readability
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}