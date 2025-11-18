package vcmsa.projects.prog7314.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.AppDatabase
import vcmsa.projects.prog7314.data.repository.UserProfileRepository
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.utils.NetworkManager
import vcmsa.projects.prog7314.data.sync.SyncManager
import vcmsa.projects.prog7314.utils.LocalNotificationManager
/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/

/**
 * MainMenuScreen
 *
 * This composable represents the main menu interface of the Memory Match Madness app.
 * It provides users with quick access to game modes, profile, settings, and notifications.
 *
 * Key Features:
 *
 * 1. User Streak Display:
 *    - Shows the current and best streak of the user.
 *    - Includes a countdown timer indicating the remaining time before the streak expires.
 *
 * 2. Online / Offline and Sync Status:
 *    - Displays network status using a WiFi icon (online/offline).
 *    - Shows number of unsynced items pending synchronization.
 *    - Supports a clickable dialog showing detailed sync information.
 *
 * 3. Notifications:
 *    - Notification bell icon with badge for unread notifications.
 *    - Badge automatically updates based on unread count from the repository.
 *
 * 4. Top Bar:
 *    - Contains the streak card, network status, notifications, and settings icons.
 *    - Provides visual feedback using colors and icons for online/offline and sync state.
 *
 * 5. Logo and Branding:
 *    - App logo displayed prominently below the top bar.
 *
 * 6. Game Mode Buttons:
 *    - Arcade Mode, Adventure Mode, Multiplayer, Statistics, and Settings.
 *    - Custom 3D button style using layered Box with shadows.
 *    - Each button triggers a callback for navigation or action.
 *
 * 7. Data Loading:
 *    - Fetches user profile, streaks, and unread notification counts from Firebase and local repositories.
 *    - Retrieves unsynced counts from SyncManager to indicate pending local data to be synchronized.
 *
 * 8. Countdown Timer:
 *    - Updates every minute showing hours and minutes remaining in the current streak.
 *
 * 9. UI Styling:
 *    - Uses gradient background and semi-transparent overlay for visual depth.
 *    - Cards and buttons use rounded corners, shadows, and color accents for modern design.
 *    - Responsive layout using Column, Row, Box, and padding for alignment.
 *
 * 10. Dialogs:
 *    - Displays an AlertDialog showing network/sync status when the WiFi icon is clicked.
 *    - Dialog adapts content based on online/offline and unsynced items.
 *
 * Purpose:
 * Enhances user engagement by visually tracking streaks, showing notifications,
 * providing network and sync feedback, and giving quick access to all game modes.
 */


@Composable
fun MainMenuScreen(
    onArcadeModeClick: () -> Unit = {},
    onAdventureModeClick: () -> Unit = {},
    onMultiplayerClick: () -> Unit = {},
    onStatisticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    userEmail: String = "user@example.com"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentStreak by remember { mutableStateOf(0) }
    var bestStreak by remember { mutableStateOf(0) }
    var unreadNotificationCount by remember { mutableStateOf(0) }

    // 🔥 Network and sync status
    val isOnline by NetworkManager.isOnline.collectAsState()
    var unsyncedCount by remember { mutableStateOf(0) }

    // 🔥 Sync status dialog
    var showSyncDialog by remember { mutableStateOf(false) }

    // 🔥 NEW: Streak countdown timer
    var hoursRemaining by remember { mutableStateOf(0) }
    var minutesRemaining by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val database = AppDatabase.getDatabase(context)
                    val userProfileRepo = UserProfileRepository(database.userProfileDao())

                    val profile = userProfileRepo.getUserProfile(userId)
                    if (profile != null) {
                        currentStreak = profile.currentStreak
                        bestStreak = profile.bestStreak
                    }

                    val notificationRepo = RepositoryProvider.getNotificationRepository()
                    unreadNotificationCount = notificationRepo.getUnreadCount(userId)

                    // Get unsynced count
                    val syncManager = SyncManager(context)
                    val counts = syncManager.getUnsyncedCounts()
                    unsyncedCount = counts.total
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    // 🔥 Refresh unsynced count periodically
    LaunchedEffect(isOnline) {
        scope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val syncManager = SyncManager(context)
                    val counts = syncManager.getUnsyncedCounts()
                    unsyncedCount = counts.total
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // 🔥 NEW: Update countdown timer every minute
    LaunchedEffect(currentStreak) {
        if (currentStreak > 0) {
            while (true) {
                val lastPlayDate = LocalNotificationManager.getLastPlayDate(context)
                if (lastPlayDate > 0) {
                    val currentTime = System.currentTimeMillis()
                    val hoursElapsed = (currentTime - lastPlayDate) / (1000 * 60 * 60)
                    val hoursUntilExpiry = 48 - hoursElapsed

                    if (hoursUntilExpiry > 0) {
                        hoursRemaining = hoursUntilExpiry.toInt()
                        val totalMinutesElapsed = (currentTime - lastPlayDate) / (1000 * 60)
                        val minutesInCurrentHour = totalMinutesElapsed % 60
                        minutesRemaining = (60 - minutesInCurrentHour).toInt()
                    } else {
                        hoursRemaining = 0
                        minutesRemaining = 0
                    }
                }
                delay(60000) // Update every minute
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.card_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.6f
        )

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
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak Card with Countdown
                if (currentStreak > 0) {
                    Card(
                        modifier = Modifier.padding(end = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🔥", fontSize = 28.sp)
                            Column {
                                Text(
                                    text = stringResource(R.string.day_streak, currentStreak),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6F00)
                                )
                                // 🔥 NEW: Countdown timer
                                if (hoursRemaining > 0) {
                                    Text(
                                        text = "⏰ ${hoursRemaining}h ${minutesRemaining}m left",
                                        fontSize = 10.sp,
                                        color = if (hoursRemaining < 24) Color(0xFFFF5722) else Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.best_days, bestStreak),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Icons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // WiFi Status Icon (Clickable)
                    IconButton(
                        onClick = { showSyncDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = if (isOnline) "Online" else "Offline",
                            tint = when {
                                !isOnline -> Color(0xFFFF3D00)
                                unsyncedCount > 0 -> Color(0xFFFFA726)
                                else -> Color(0xFF66BB6A)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Notification Bell with Badge
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .offset(x = 14.dp, y = (-14).dp)
                                    .background(Color(0xFFFF3D00), CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

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
            }

            // Logo
            Image(
                painter = painterResource(id = R.drawable.transparent_logo),
                contentDescription = "Memory Match Madness Logo",
                modifier = Modifier
                    .size(220.dp)
                    .padding(bottom = 32.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game Mode Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GameModeButton3D(
                    text = stringResource(R.string.arcade_mode).uppercase(),
                    backgroundColor = Color(0xFFFFC107),
                    shadowColor = Color(0xFFCC8800),
                    onClick = onArcadeModeClick
                )

                GameModeButton3D(
                    text = stringResource(R.string.adventure_mode).uppercase(),
                    backgroundColor = Color(0xFF2196F3),
                    shadowColor = Color(0xFF0D47A1),
                    onClick = onAdventureModeClick
                )

                GameModeButton3D(
                    text = stringResource(R.string.multiplayer).uppercase(),
                    backgroundColor = Color(0xFFE91E63),
                    shadowColor = Color(0xFFAD1457),
                    onClick = onMultiplayerClick
                )

                GameModeButton3D(
                    text = stringResource(R.string.statistics).uppercase(),
                    backgroundColor = Color(0xFF9C27B0),
                    shadowColor = Color(0xFF6A1B9A),
                    onClick = onStatisticsClick
                )

                GameModeButton3D(
                    text = "⚙ ${stringResource(R.string.settings).uppercase()}",
                    backgroundColor = Color(0xFF8BC34A),
                    shadowColor = Color(0xFF558B2F),
                    onClick = onSettingsClick
                )
            }
        }
    }

    // Sync Status Dialog
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            icon = {
                Icon(
                    imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = when {
                        !isOnline -> Color(0xFFFF3D00)
                        unsyncedCount > 0 -> Color(0xFFFFA726)
                        else -> Color(0xFF66BB6A)
                    },
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = if (isOnline) "Online" else "Offline Mode",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isOnline) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF66BB6A),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Connected to internet",
                                fontSize = 14.sp
                            )
                        }

                        if (unsyncedCount > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color(0xFFFFA726),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Syncing $unsyncedCount item${if (unsyncedCount > 1) "s" else ""}...",
                                    fontSize = 14.sp,
                                    color = Color(0xFFFFA726)
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF66BB6A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "All data synced",
                                    fontSize = 14.sp,
                                    color = Color(0xFF66BB6A)
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = Color(0xFFFF3D00),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "No internet connection",
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "You can still play! Your progress will be saved locally and synced automatically when you're back online.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )

                        if (unsyncedCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠️ $unsyncedCount item${if (unsyncedCount > 1) "s" else ""} waiting to sync",
                                fontSize = 13.sp,
                                color = Color(0xFFFFA726),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSyncDialog = false }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .offset(y = 8.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(color = shadowColor)
        )

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