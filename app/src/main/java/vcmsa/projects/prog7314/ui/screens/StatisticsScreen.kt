package vcmsa.projects.prog7314.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.entities.AchievementEntity
import vcmsa.projects.prog7314.data.models.UserAnalytics
import vcmsa.projects.prog7314.data.models.RecentGameSummary
import vcmsa.projects.prog7314.data.models.AchievementType
import vcmsa.projects.prog7314.data.repository.PerformanceTrend
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.utils.AuthManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var analytics by remember { mutableStateOf<UserAnalytics?>(null) }
    var recentGames by remember { mutableStateOf<List<RecentGameSummary>>(emptyList()) }
    var performanceTrend by remember { mutableStateOf(PerformanceTrend.INSUFFICIENT_DATA) }
    var isLoading by remember { mutableStateOf(true) }
    var showAchievementsDialog by remember { mutableStateOf(false) }

    val currentUserId = AuthManager.getCurrentUser()?.uid ?: ""

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val analyticsRepo = RepositoryProvider.getAnalyticsRepository()
                analytics = analyticsRepo.getUserAnalytics(currentUserId)
                recentGames = analyticsRepo.getRecentGames(currentUserId, 5)
                performanceTrend = analyticsRepo.getPerformanceTrend(currentUserId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
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

                Text(
                    text = stringResource(R.string.your_statistics),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                IconButton(
                    onClick = { loadData() },
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Content
            if (isLoading) {
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
            } else {
                analytics?.let { stats ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Overview Stats Grid
                        SectionHeader(icon = Icons.Default.Home, title = stringResource(R.string.overview))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ImprovedStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.PlayArrow,
                                value = stats.totalGames.toString(),
                                label = stringResource(R.string.games),
                                color = Color(0xFF2196F3)
                            )
                            ImprovedStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.CheckCircle,
                                value = stats.totalWins.toString(),
                                label = stringResource(R.string.wins),
                                color = Color(0xFF4CAF50)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ImprovedStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.TrendingUp,
                                value = "${String.format("%.1f", stats.winRate)}%",
                                label = stringResource(R.string.win_rate),
                                color = Color(0xFFFF9800)
                            )
                            ImprovedStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Schedule,
                                value = formatPlaytime(stats.totalPlaytime),
                                label = stringResource(R.string.playtime),
                                color = Color(0xFF9C27B0)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ImprovedStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Star,
                                value = stats.currentLevel.toString(),
                                label = stringResource(R.string.level),
                                color = Color(0xFFE91E63)
                            )
                            ImprovedStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.EmojiEvents,
                                value = stats.totalXP.toString(),
                                label = stringResource(R.string.total_xp),
                                color = Color(0xFFFFC107)
                            )
                        }

                        // Performance Section
                        SectionHeader(icon = Icons.Default.Analytics, title = stringResource(R.string.performance))

                        PerformanceCard(stats = stats, trend = performanceTrend)

                        // Achievements Section
                        SectionHeader(icon = Icons.Default.EmojiEvents, title = stringResource(R.string.achievements))

                        ClickableAchievementCard(
                            stats = stats,
                            onClick = { showAchievementsDialog = true }
                        )

                        // Themes & Modes Section
                        if (stats.themeStats.isNotEmpty()) {
                            SectionHeader(icon = Icons.Default.Palette, title = stringResource(R.string.themes_modes))

                            ThemesCard(stats = stats)
                        }

                        // Recent Games Section
                        if (recentGames.isNotEmpty()) {
                            SectionHeader(icon = Icons.Default.History, title = stringResource(R.string.recent_games))

                            RecentGamesCard(games = recentGames)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.no_statistics_yet),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.play_games_to_see_stats),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Achievements Dialog
    if (showAchievementsDialog) {
        AchievementsDialog(
            userId = currentUserId,
            onDismiss = { showAchievementsDialog = false }
        )
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ImprovedStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ClickableAchievementCard(stats: UserAnalytics, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFC107),
                                Color(0xFFFF9800)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stats.achievementsUnlocked}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "/ ${stats.totalAchievements}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.achievements_unlocked),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (stats.achievementProgress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFFFF9800),
                    trackColor = Color(0xFFE0E0E0)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.tap_to_view_all),
                    fontSize = 11.sp,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun AchievementsDialog(
    userId: String,
    onDismiss: () -> Unit
) {
    var achievements by remember { mutableStateOf<List<AchievementEntity>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current  // ✅ Get context OUTSIDE the launch block

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val repo = RepositoryProvider.getAchievementRepository()
                val userAchievements = repo.getAllAchievementsForUser(userId)

                // Get all achievement types and merge with user's achievements
                val allAchievementTypes = AchievementType.getAllAchievements()
                val mergedAchievements = allAchievementTypes.map { type ->
                    val userAchievement = userAchievements.find { it.achievementType == type.achievementId }
                    userAchievement ?: AchievementEntity(
                        achievementId = type.achievementId,
                        userId = userId,
                        achievementType = type.achievementId,
                        name = context.getString(type.displayNameResId),  // ✅ Use context variable
                        description = context.getString(type.descriptionResId),  // ✅ Use context variable
                        iconName = type.achievementId,
                        unlockedAt = 0L,
                        progress = 0,
                        isUnlocked = false,
                        isSynced = false
                    )
                }
                achievements = mergedAchievements
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    val unlockedAchievements = achievements.filter { it.isUnlocked }
    val lockedAchievements = achievements.filter { !it.isUnlocked }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .shadow(16.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00BCD4),
                                    Color(0xFF0288D1)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.all_achievements),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "${unlockedAchievements.size} / ${achievements.size} ${stringResource(R.string.unlocked)}",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = Color.White
                            )
                        }
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Unlocked Achievements Section
                    if (unlockedAchievements.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${stringResource(R.string.unlocked)} (${unlockedAchievements.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }

                        unlockedAchievements.forEach { achievement ->
                            ColorfulAchievementItem(achievement = achievement, isUnlocked = true)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Locked Achievements Section
                    if (lockedAchievements.isNotEmpty()) {
                        if (unlockedAchievements.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${stringResource(R.string.locked)} (${lockedAchievements.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }

                        lockedAchievements.forEach { achievement ->
                            ColorfulAchievementItem(achievement = achievement, isUnlocked = false)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    if (achievements.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.play_games_to_see_stats),
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorfulAchievementItem(achievement: AchievementEntity, isUnlocked: Boolean) {
    val context = LocalContext.current
    val achievementType = AchievementType.fromId(achievement.achievementType)
    val icon = achievementType?.icon ?: Icons.Default.EmojiEvents

    // ✅ ALWAYS get localized strings from resources, not from database
    val displayName = achievementType?.let {
        context.getString(it.displayNameResId)
    } ?: achievement.name

    val displayDescription = achievementType?.let {
        context.getString(it.descriptionResId)
    } ?: achievement.description

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isUnlocked) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                achievementType?.color?.copy(alpha = 0.3f) ?: Color(0xFFE0E0E0)
            )
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (isUnlocked && achievementType != null) {
                            achievementType.color.copy(alpha = 0.12f)
                        } else {
                            Color(0xFFF5F5F5)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUnlocked && achievementType != null) {
                        achievementType.color
                    } else {
                        Color.Gray
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,  // ✅ Use localized name
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color(0xFF212121) else Color(0xFF9E9E9E)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = displayDescription,  // ✅ Use localized description
                    fontSize = 12.sp,
                    color = if (isUnlocked) Color(0xFF757575) else Color(0xFFBDBDBD),
                    lineHeight = 16.sp
                )

                // Progress bar for locked achievements
                if (!isUnlocked && achievement.progress > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { achievement.progress / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = achievementType?.color ?: Color(0xFF2196F3),
                            trackColor = Color(0xFFE0E0E0)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${achievement.progress}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status Badge
            if (isUnlocked) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = Color(0xFF4CAF50),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = Color(0xFFE0E0E0),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceCard(stats: UserAnalytics, trend: PerformanceTrend) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Trend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.performance_trend),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                TrendChip(trend)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider()
            Spacer(modifier = Modifier.height(20.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PerformanceStat(
                    icon = Icons.Default.Star,
                    label = stringResource(R.string.avg_score),
                    value = String.format("%.0f", stats.averageScore),
                    color = Color(0xFF2196F3)
                )
                PerformanceStat(
                    icon = Icons.Default.EmojiEvents,
                    label = stringResource(R.string.best_score),
                    value = stats.bestScore.toString(),
                    color = Color(0xFFFFC107)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PerformanceStat(
                    icon = Icons.Default.Timer,
                    label = stringResource(R.string.avg_time),
                    value = "${stats.averageTime.toInt()}s",
                    color = Color(0xFF9C27B0)
                )
                PerformanceStat(
                    icon = Icons.Default.Speed,
                    label = stringResource(R.string.best_time),
                    value = "${stats.fastestTime}s",
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider()
            Spacer(modifier = Modifier.height(20.dp))

            // Accuracy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.accuracy),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
                Text(
                    text = "${String.format("%.1f", stats.averageAccuracy)}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4CAF50)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (stats.averageAccuracy / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun RowScope.PerformanceStat(icon: ImageVector, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TrendChip(trend: PerformanceTrend) {
    val (textResId, color, icon) = when (trend) {
        PerformanceTrend.IMPROVING -> Triple(R.string.improving, Color(0xFF4CAF50), Icons.Default.TrendingUp)
        PerformanceTrend.STABLE -> Triple(R.string.stable, Color(0xFF2196F3), Icons.Default.TrendingFlat)
        PerformanceTrend.DECLINING -> Triple(R.string.declining, Color(0xFFFF9800), Icons.Default.TrendingDown)
        PerformanceTrend.INSUFFICIENT_DATA -> Triple(R.string.play_more, Color.Gray, Icons.Default.Info)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(textResId),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ThemesCard(stats: UserAnalytics) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.favorite),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stats.favoriteTheme,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VideogameAsset,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.most_played),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stats.mostPlayedMode,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }

            if (stats.themeStats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.theme_statistics),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                stats.themeStats.entries.take(5).forEach { (theme, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF2196F3), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = theme,
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                        Text(
                            text = context.getString(R.string.games_count, count),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentGamesCard(games: List<RecentGameSummary>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            games.forEach { game ->
                RecentGameRow(game)
                if (game != games.last()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun RecentGameRow(game: RecentGameSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = when (game.stars) {
                        3 -> Color(0xFFFFC107).copy(alpha = 0.2f)
                        2 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else -> Color(0xFF9E9E9E).copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VideogameAsset,
                contentDescription = null,
                tint = when (game.stars) {
                    3 -> Color(0xFFFFC107)
                    2 -> Color(0xFF4CAF50)
                    else -> Color(0xFF9E9E9E)
                },
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game.theme,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${game.gameMode} • ${formatDate(game.completedAt)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${game.timeTaken}s • ${game.moves} moves",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${game.score} pts",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF4CAF50)
            )
            Row {
                repeat(game.stars) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun formatPlaytime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview
@Composable
fun StatisticsScreenPreview() {
    StatisticsScreen()
}