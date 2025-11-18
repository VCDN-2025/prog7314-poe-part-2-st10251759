package vcmsa.projects.prog7314.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.entities.NotificationEntity
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.utils.AuthManager
import java.text.SimpleDateFormat
import java.util.*
/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/

/**
 * NotificationsScreen
 *
 * This composable displays a full-featured notification center for the user, including
 * category filtering, badge counts, and individual notification management.
 *
 * Key Features:
 * 1. State Management:
 *    - `notifications`: List of notifications fetched from the repository.
 *    - `selectedCategory`: Current filter category (ALL, GAME, SOCIAL, SYSTEM).
 *    - `isLoading`: Indicates data loading state.
 *    - `unreadCount`, `totalCount`, `todayCount`: Notification summary badges.
 *    - `errorMessage`: Stores any error messages when fetching notifications.
 *
 * 2. Data Loading:
 *    - `loadNotifications()` fetches notifications and counts from repository.
 *    - Uses coroutine scope to handle asynchronous calls.
 *    - Updates UI state based on selected category and user ID.
 *    - Handles errors gracefully and logs key events using Logcat.
 *
 * 3. Layout & UI:
 *    - Gradient background for visual appeal.
 *    - Top Bar:
 *        - Back button triggers `onBackClick`.
 *        - Clear All button deletes all notifications for the current user.
 *    - Title Section displays main header and subtitle.
 *    - Badge Counts:
 *        - Shows UNREAD, TOTAL, and TODAY counts in separate cards.
 *    - Category Tabs:
 *        - Filter notifications by ALL, GAME, SOCIAL, or SYSTEM categories.
 *        - Highlights the selected category visually.
 *    - Notifications List:
 *        - Loading: Shows CircularProgressIndicator.
 *        - Error: Displays error message with icon.
 *        - Empty: Shows placeholder if no notifications exist.
 *        - Populated: Scrollable list of NotificationCard items.
 *
 * 4. NotificationCard:
 *    - Shows notification icon, title, message, timestamp.
 *    - Buttons for marking as read and deleting notification.
 *    - Visual cues for unread notifications (small dot indicator).
 *    - Color-codes icons based on category.
 *
 * 5. Helper Functions:
 *    - `formatTimestamp(timestamp: Long)`: Formats timestamps into human-readable relative times
 *      ("Just now", "x minutes ago", "x hours ago", etc.) or absolute dates if older than a week.
 *
 * 6. Interaction & Side Effects:
 *    - Selecting a category updates `selectedCategory` and triggers data reload.
 *    - Actions like mark as read, delete, and clear all notifications update repository
 *      and refresh UI asynchronously.
 *
 * Purpose:
 * Provides a visually appealing, interactive, and responsive notification center
 * for users to stay updated with game, social, and system events.
 */


private const val TAG = "NotificationsScreen"

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var notifications by remember { mutableStateOf<List<NotificationEntity>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var isLoading by remember { mutableStateOf(true) }
    var unreadCount by remember { mutableStateOf(0) }
    var totalCount by remember { mutableStateOf(0) }
    var todayCount by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentUserId = AuthManager.getCurrentUser()?.uid ?: ""

    fun loadNotifications() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                Log.d(TAG, "🔄 Loading notifications for user: $currentUserId")

                if (currentUserId.isEmpty()) {
                    Log.e(TAG, "❌ No user logged in!")
                    errorMessage = "Not logged in"
                    isLoading = false
                    return@launch
                }

                val notificationRepo = RepositoryProvider.getNotificationRepository()

                // Load counts
                unreadCount = notificationRepo.getUnreadCount(currentUserId)
                totalCount = notificationRepo.getTotalCount(currentUserId)
                todayCount = notificationRepo.getTodayCount(currentUserId)

                Log.d(TAG, "📊 Counts - Total: $totalCount, Unread: $unreadCount, Today: $todayCount")

                // Load notifications based on selected category
                notifications = when (selectedCategory) {
                    "ALL" -> notificationRepo.getAllNotifications(currentUserId)
                    else -> notificationRepo.getNotificationsByCategory(currentUserId, selectedCategory)
                }

                Log.d(TAG, "✅ Loaded ${notifications.size} notifications for category: $selectedCategory")
                notifications.forEach { notif ->
                    Log.d(TAG, "   📬 ${notif.title} | Read: ${notif.isRead}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading notifications: ${e.message}", e)
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedCategory, currentUserId) {
        Log.d(TAG, "🚀 NotificationsScreen launched - UserId: $currentUserId, Category: $selectedCategory")
        loadNotifications()
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Clear All Button
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val notificationRepo = RepositoryProvider.getNotificationRepository()
                                notificationRepo.deleteAllNotifications(currentUserId)
                                Log.d(TAG, "🗑️ Cleared all notifications")
                                loadNotifications()
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ Error clearing notifications: ${e.message}", e)
                            }
                        }
                    },
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .background(Color(0xFFFF6B35), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "CLEAR ALL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Title Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NOTIFICATIONS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "STAY UPDATED WITH YOUR PROGRESS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Badge Counts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BadgeCard(
                    count = unreadCount,
                    label = "UNREAD",
                    modifier = Modifier.weight(1f)
                )
                BadgeCard(
                    count = totalCount,
                    label = "TOTAL",
                    modifier = Modifier.weight(1f)
                )
                BadgeCard(
                    count = todayCount,
                    label = "TODAY",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(25.dp))
                    .background(Color.White, RoundedCornerShape(25.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CategoryTab("ALL", selectedCategory == "ALL") { selectedCategory = "ALL" }
                CategoryTab("GAME", selectedCategory == "GAME") { selectedCategory = "GAME" }
                CategoryTab("SOCIAL", selectedCategory == "SOCIAL") { selectedCategory = "SOCIAL" }
                CategoryTab("SYSTEM", selectedCategory == "SYSTEM") { selectedCategory = "SYSTEM" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications List
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(50.dp),
                            strokeWidth = 5.dp
                        )
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error loading notifications",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "Unknown error",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No notifications yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You'll see updates here",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        notifications.forEach { notification ->
                            NotificationCard(
                                notification = notification,
                                onView = {
                                    coroutineScope.launch {
                                        try {
                                            val notificationRepo = RepositoryProvider.getNotificationRepository()
                                            notificationRepo.markAsRead(notification.id)
                                            Log.d(TAG, "✅ Marked notification ${notification.id} as read")
                                            loadNotifications()
                                        } catch (e: Exception) {
                                            Log.e(TAG, "❌ Error marking as read: ${e.message}", e)
                                        }
                                    }
                                },
                                onDismiss = {
                                    coroutineScope.launch {
                                        try {
                                            val notificationRepo = RepositoryProvider.getNotificationRepository()
                                            notificationRepo.deleteNotification(notification.id)
                                            Log.d(TAG, "🗑️ Deleted notification ${notification.id}")
                                            loadNotifications()
                                        } catch (e: Exception) {
                                            Log.e(TAG, "❌ Error deleting notification: ${e.message}", e)
                                        }
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeCard(count: Int, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF6B35)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RowScope.CategoryTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Color(0xFFFF6B35) else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}

@Composable
fun NotificationCard(
    notification: NotificationEntity,
    onView: () -> Unit,
    onDismiss: () -> Unit
) {
    val icon = when (notification.iconType) {
        "trophy" -> Icons.Default.EmojiEvents
        "level" -> Icons.Default.Gamepad
        "star" -> Icons.Default.Star
        "fire" -> Icons.Default.LocalFireDepartment
        "theme" -> Icons.Default.Palette
        else -> Icons.Default.Info
    }

    val iconColor = when (notification.category) {
        "GAME" -> Color(0xFFFFC107)
        "SOCIAL" -> Color(0xFF2196F3)
        else -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatTimestamp(notification.timestamp),
                    fontSize = 10.sp,
                    color = Color(0xFF9E9E9E)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onView,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (notification.isRead)
                                Color(0xFFE0E0E0)
                            else
                                Color(0xFFFF6B35).copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (notification.isRead) "Read" else "Mark Read",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (notification.isRead)
                                Color(0xFF757575)
                            else
                                Color(0xFFFF6B35)
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Delete",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFFF6B35), CircleShape)
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}