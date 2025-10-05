package vcmsa.projects.prog7314

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.AppDatabase
import vcmsa.projects.prog7314.data.models.GameTheme
import vcmsa.projects.prog7314.data.models.GridSize
import vcmsa.projects.prog7314.data.models.GameProgress
import vcmsa.projects.prog7314.data.models.LevelData
import vcmsa.projects.prog7314.data.repository.UserProfileRepository
import vcmsa.projects.prog7314.data.repository.GameResultRepository
import vcmsa.projects.prog7314.data.repository.AchievementRepository
import vcmsa.projects.prog7314.data.repository.ApiRepository
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.data.repository.LevelRepository
import vcmsa.projects.prog7314.data.sync.SyncManager
import vcmsa.projects.prog7314.data.sync.FirestoreManager
import vcmsa.projects.prog7314.data.sync.ProgressSyncHelper
import vcmsa.projects.prog7314.ui.screens.*
import vcmsa.projects.prog7314.ui.theme.PROG7314Theme
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.BiometricHelper
import vcmsa.projects.prog7314.utils.FirebaseHelper
import vcmsa.projects.prog7314.utils.NetworkManager

class MainActivity : FragmentActivity() {

    private lateinit var syncManager: SyncManager
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Switch from splash theme to app theme
        setTheme(android.R.style.Theme_Material_Light_NoActionBar)

        FirebaseHelper.initializeFirebase()
        AuthManager.initializeGoogleSignIn(this)
        testApiConnection()

        NetworkManager.initialize(this)
        RepositoryProvider.initialize(this)

        syncManager = SyncManager(this)
        syncManager.initialize()

        firestoreManager = FirestoreManager()

        testDatabase()

        enableEdgeToEdge()
        setContent {
            PROG7314Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MemoryMatchMadnessApp()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkManager.cleanup()
        syncManager.cleanup()
    }

    fun testFirestore() {
        lifecycleScope.launch {
            try {
                Log.d("FirestoreTest", "========== TESTING FIRESTORE ==========")

                val currentUser = AuthManager.getCurrentUser()
                val userId = currentUser?.uid ?: "unknown"
                Log.d("FirestoreTest", "Current user ID: $userId")

                val testProgress = GameProgress(
                    userId = userId,
                    currentLevel = 3,
                    levelProgress = mapOf(
                        1 to LevelData(1, stars = 3, bestScore = 1500, bestTime = 45, bestMoves = 20, isUnlocked = true, isCompleted = true, timesPlayed = 2),
                        2 to LevelData(2, stars = 2, bestScore = 1200, bestTime = 60, bestMoves = 25, isUnlocked = true, isCompleted = true, timesPlayed = 1),
                        3 to LevelData(3, stars = 0, bestScore = 0, bestTime = 0, bestMoves = 0, isUnlocked = true, isCompleted = false, timesPlayed = 0)
                    ),
                    unlockedCategories = listOf("Animals", "Fruits"),
                    totalGamesPlayed = 3,
                    gamesWon = 2
                )

                val saveResult = firestoreManager.saveGameProgress(testProgress)
                if (saveResult.isSuccess) {
                    Log.d("FirestoreTest", "✅ SAVE SUCCESS: Progress saved to Firestore!")
                    Log.d("FirestoreTest", "   Current Level: ${testProgress.currentLevel}")
                    Log.d("FirestoreTest", "   Levels Tracked: ${testProgress.levelProgress.size}")
                } else {
                    Log.e("FirestoreTest", "❌ SAVE FAILED: ${saveResult.exceptionOrNull()?.message}")
                }

                val loadResult = firestoreManager.loadGameProgress()
                if (loadResult.isSuccess) {
                    val loadedProgress = loadResult.getOrNull()
                    if (loadedProgress != null) {
                        Log.d("FirestoreTest", "✅ LOAD SUCCESS: Data retrieved!")
                        Log.d("FirestoreTest", "   Current Level: ${loadedProgress.currentLevel}")
                        Log.d("FirestoreTest", "   Levels: ${loadedProgress.levelProgress.size}")
                        loadedProgress.levelProgress.forEach { (level, data) ->
                            Log.d("FirestoreTest", "   Level $level: ${data.stars}★ Score:${data.bestScore} Completed:${data.isCompleted}")
                        }
                    } else {
                        Log.d("FirestoreTest", "ℹ️ No saved progress found")
                    }
                } else {
                    Log.e("FirestoreTest", "❌ LOAD FAILED: ${loadResult.exceptionOrNull()?.message}")
                }

                Log.d("FirestoreTest", "========== FIRESTORE TEST COMPLETE ==========")

            } catch (e: Exception) {
                Log.e("FirestoreTest", "❌ Firestore test error: ${e.message}", e)
            }
        }
    }

    private fun testApiConnection() {
        lifecycleScope.launch {
            try {
                val apiRepo = ApiRepository()
                val tokenResult = apiRepo.verifyFirebaseToken()
                if (tokenResult.isSuccess) {
                    Log.d("MainActivity", "✅ API Connection Successful!")
                    Log.d("MainActivity", "User ID: ${tokenResult.getOrNull()?.userId}")
                } else {
                    Log.e("MainActivity", "❌ API Connection Failed: ${tokenResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ API Test Error: ${e.message}", e)
            }
        }
    }

    private fun testDatabase() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val userRepo = UserProfileRepository(db.userProfileDao())
                val gameRepo = GameResultRepository(db.gameResultDao())
                val achievementRepo = AchievementRepository(db.achievementDao())

                Log.d("DatabaseTest", "========== TESTING REPOSITORIES ==========")
                Log.d("DatabaseTest", "\n--- Test 1: User Profile ---")

                val userId = "test_user_123"
                val created = userRepo.createNewUserProfile(
                    userId = userId,
                    username = "TestPlayer",
                    email = "test@example.com"
                )
                Log.d("DatabaseTest", "✅ User created: $created")

                val user = userRepo.getUserProfile(userId)
                Log.d("DatabaseTest", "✅ User retrieved: ${user?.username}, Level ${user?.level}")

                userRepo.updateXPAndLevel(userId, 250, 3)
                val updatedUser = userRepo.getUserProfile(userId)
                Log.d("DatabaseTest", "✅ Updated XP: ${updatedUser?.totalXP}, Level: ${updatedUser?.level}")

                Log.d("DatabaseTest", "\n--- Test 2: Game Results ---")

                val gameId = gameRepo.createGameResult(
                    userId = userId,
                    gameMode = "ARCADE",
                    theme = "Animals",
                    gridSize = "4x3",
                    difficulty = "INTERMEDIATE",
                    score = 1500,
                    timeTaken = 45,
                    moves = 20,
                    accuracy = 85.5f,
                    isWin = true
                )
                Log.d("DatabaseTest", "✅ Game saved with ID: $gameId")

                val totalGames = gameRepo.getTotalGamesCount(userId)
                val winRate = gameRepo.getWinRate(userId)
                Log.d("DatabaseTest", "✅ Total games: $totalGames, Win rate: $winRate%")

                Log.d("DatabaseTest", "\n--- Test 3: Achievements ---")

                val firstWin = achievementRepo.checkFirstWinAchievement(userId, true)
                Log.d("DatabaseTest", "✅ First Win achievement awarded: $firstWin")

                val speedDemon = achievementRepo.checkSpeedDemonAchievement(userId, 25)
                Log.d("DatabaseTest", "✅ Speed Demon achievement awarded: $speedDemon")

                val unlockedCount = achievementRepo.getUnlockedCount(userId)
                Log.d("DatabaseTest", "✅ Total unlocked achievements: $unlockedCount")

                Log.d("DatabaseTest", "\n--- Test 4: Statistics ---")

                val avgScore = gameRepo.getAverageScore(userId)
                val bestScore = gameRepo.getBestScore(userId)
                val avgTime = gameRepo.getAverageTime(userId)

                Log.d("DatabaseTest", "✅ Average Score: $avgScore")
                Log.d("DatabaseTest", "✅ Best Score: $bestScore")
                Log.d("DatabaseTest", "✅ Average Time: ${avgTime}s")

                Log.d("DatabaseTest", "\n--- Test 5: Sync Status ---")

                val unsyncedGames = gameRepo.getUnsyncedGamesForUser(userId)
                val unsyncedAchievements = achievementRepo.getUnsyncedAchievementsForUser(userId)

                Log.d("DatabaseTest", "✅ Unsynced games: ${unsyncedGames.size}")
                Log.d("DatabaseTest", "✅ Unsynced achievements: ${unsyncedAchievements.size}")

                Log.d("DatabaseTest", "\n--- Test 6: Sync Manager ---")

                val unsyncedCounts = syncManager.getUnsyncedCounts()
                Log.d("DatabaseTest", "✅ Total unsynced items: ${unsyncedCounts.total}")
                Log.d("DatabaseTest", "✅ Network status: ${NetworkManager.getConnectionStatus()}")

                if (NetworkManager.isNetworkAvailable()) {
                    Log.d("DatabaseTest", "Testing manual sync...")
                    val syncSuccess = syncManager.performManualSync()
                    Log.d("DatabaseTest", "✅ Manual sync result: $syncSuccess")
                }

                Log.d("DatabaseTest", "\n========== ALL TESTS PASSED ✅ ==========")

            } catch (e: Exception) {
                Log.e("DatabaseTest", "❌ Repository test error: ${e.message}", e)
            }
        }
    }
}

data class CompletionData(
    val stars: Int,
    val score: Int,
    val time: Int,
    val moves: Int,
    val bonus: Int
)

@Composable
fun MemoryMatchMadnessApp() {
    var showLoadingScreen by remember { mutableStateOf(true) }
    var showLoginScreen by remember { mutableStateOf(false) }
    var showRegisterScreen by remember { mutableStateOf(false) }
    var showMainMenu by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf("") }

    var showThemeSelection by remember { mutableStateOf(false) }
    var showGridSelection by remember { mutableStateOf(false) }
    var showGameplay by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf<GameTheme?>(null) }
    var selectedGridSize by remember { mutableStateOf<GridSize?>(null) }

    var showArcadeMode by remember { mutableStateOf(false) }
    var showLevelSelection by remember { mutableStateOf(false) }
    var showArcadeGameplay by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf(1) }
    var isArcadeMode by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var completionData by remember { mutableStateOf<CompletionData?>(null) }

    var showMultiplayerSetup by remember { mutableStateOf(false) }
    var showMultiplayerGame by remember { mutableStateOf(false) }

    // NEW: Edit Profile and Change Password screens
    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val coroutineScope = rememberCoroutineScope()

    val isBiometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }
    val isOnline by NetworkManager.isOnline.collectAsState()
    val connectionStatus = NetworkManager.getConnectionStatus()

    when {
        showLoadingScreen -> {
            LoadingScreen(
                onLoadingComplete = {
                    showLoadingScreen = false
                    showLoginScreen = true
                }
            )
        }

        showLoginScreen -> {
            LoginScreen(
                onLoginSuccess = {
                    userEmail = AuthManager.getCurrentUser()?.email ?: ""
                    showLoginScreen = false

                    val userId = AuthManager.getCurrentUser()?.uid
                    if (userId != null) {
                        coroutineScope.launch {
                            val db = AppDatabase.getDatabase(context)
                            val levelRepo = LevelRepository(db.levelProgressDao())
                            val syncHelper = ProgressSyncHelper(levelRepo, FirestoreManager())

                            val success = syncHelper.loadProgressFromCloud(userId)
                            if (success) {
                                Log.d("MainActivity", "✅ Progress loaded from cloud")
                            } else {
                                Log.e("MainActivity", "❌ Failed to load progress")
                            }
                        }
                    }

                    if (isBiometricAvailable &&
                        !BiometricHelper.isBiometricEnabled(context) &&
                        !AuthManager.hasSavedCredentials(context)) {
                        showBiometricDialog = true
                    } else {
                        showMainMenu = true
                    }
                },
                onNavigateToRegister = {
                    showLoginScreen = false
                    showRegisterScreen = true
                },
                onForgotPassword = {}
            )
        }

        showRegisterScreen -> {
            RegisterScreen(
                onRegisterSuccess = {
                    userEmail = AuthManager.getCurrentUser()?.email ?: ""
                    showRegisterScreen = false

                    if (isBiometricAvailable &&
                        !BiometricHelper.isBiometricEnabled(context)) {
                        showBiometricDialog = true
                    } else {
                        showMainMenu = true
                    }
                },
                onNavigateToLogin = {
                    showRegisterScreen = false
                    showLoginScreen = true
                }
            )
        }

        showSettingsScreen -> {
            SettingsScreen(
                onBackClick = {
                    showSettingsScreen = false
                    showMainMenu = true
                },
                onEditProfile = {
                    showSettingsScreen = false
                    showEditProfile = true
                },
                onChangePassword = {
                    showSettingsScreen = false
                    showChangePassword = true
                },
                onLogout = {
                    showSettingsScreen = false
                    showLoginScreen = true
                    userEmail = ""
                }
            )
        }

        showEditProfile -> {
            EditProfileScreen(
                onBackClick = {
                    showEditProfile = false
                    showSettingsScreen = true
                },
                onSaveSuccess = {
                    showEditProfile = false
                    showSettingsScreen = true
                }
            )
        }

        showChangePassword -> {
            ChangePasswordScreen(
                onBackClick = {
                    showChangePassword = false
                    showSettingsScreen = true
                },
                onChangeSuccess = {
                    showChangePassword = false
                    showSettingsScreen = true
                }
            )
        }

        showThemeSelection -> {
            ThemeSelectionScreen(
                onThemeSelected = { theme ->
                    selectedTheme = theme
                    showThemeSelection = false
                    showGridSelection = true
                },
                onBackClick = {
                    showThemeSelection = false
                    showMainMenu = true
                }
            )
        }

        showGridSelection -> {
            GridSizeSelectionScreen(
                onGridSizeSelected = { gridSize ->
                    selectedGridSize = gridSize
                    showGridSelection = false
                    showGameplay = true
                },
                onBackClick = {
                    showGridSelection = false
                    showThemeSelection = true
                }
            )
        }

        showGameplay -> {
            val theme = selectedTheme
            val gridSize = selectedGridSize

            if (theme != null && gridSize != null) {
                GameplayScreen(
                    theme = theme,
                    gridSize = gridSize,
                    onBackClick = {
                        showGameplay = false
                        showGridSelection = true
                    },
                    onGameComplete = {
                        showGameplay = false
                        showMainMenu = true
                    }
                )
            }
        }

        showArcadeMode -> {
            ArcadeModeScreen(
                onBackClick = {
                    showArcadeMode = false
                    showMainMenu = true
                },
                onPlayArcade = {
                    selectedLevel = (1..16).random()
                    isArcadeMode = true
                    showArcadeMode = false
                    showArcadeGameplay = true
                },
                onLevelsClick = {
                    showArcadeMode = false
                    showLevelSelection = true
                }
            )
        }

        showLevelSelection -> {
            LevelSelectionScreen(
                onBackClick = {
                    showLevelSelection = false
                    showArcadeMode = true
                },
                onLevelClick = { levelNumber ->
                    selectedLevel = levelNumber
                    isArcadeMode = false
                    showLevelSelection = false
                    showArcadeGameplay = true
                }
            )
        }

        showArcadeGameplay -> {
            ArcadeGameplayScreen(
                levelNumber = selectedLevel,
                isArcadeMode = isArcadeMode,
                onBackClick = {
                    showArcadeGameplay = false
                    if (isArcadeMode) {
                        showArcadeMode = true
                    } else {
                        showLevelSelection = true
                    }
                },
                onGameComplete = { stars, score, time, moves, bonus ->
                    completionData = CompletionData(stars, score, time, moves, bonus)
                    showCompletionDialog = true
                }
            )

            if (showCompletionDialog && completionData != null) {
                GameCompletionDialog(
                    isNewRecord = false,
                    stars = completionData!!.stars,
                    moves = completionData!!.moves,
                    time = completionData!!.time,
                    bonus = completionData!!.bonus,
                    totalScore = completionData!!.score,
                    onReplay = {
                        showCompletionDialog = false
                        completionData = null
                        showArcadeGameplay = false
                        showArcadeGameplay = true
                    },
                    onNextLevel = if (!isArcadeMode && selectedLevel < 16) {
                        {
                            showCompletionDialog = false
                            completionData = null
                            selectedLevel++
                            showArcadeGameplay = false
                            showArcadeGameplay = true
                        }
                    } else null,
                    onHome = {
                        showCompletionDialog = false
                        completionData = null
                        showArcadeGameplay = false
                        if (isArcadeMode) {
                            showArcadeMode = true
                        } else {
                            showLevelSelection = true
                        }
                    }
                )
            }
        }

        showMultiplayerSetup -> {
            MultiplayerSetupScreen(
                onBackClick = {
                    showMultiplayerSetup = false
                    showMainMenu = true
                },
                onThemeSelected = { theme ->
                    selectedTheme = theme
                    showMultiplayerSetup = false
                    showMultiplayerGame = true
                }
            )
        }

        showMultiplayerGame -> {
            selectedTheme?.let { theme ->
                MultiplayerGameplayScreen(
                    theme = theme,
                    onBackClick = {
                        showMultiplayerGame = false
                        showMultiplayerSetup = true
                    },
                    onHomeClick = {
                        showMultiplayerGame = false
                        showMainMenu = true
                    }
                )
            }
        }

        showMainMenu -> {
            val currentUser = AuthManager.getCurrentUser()
            val userId = currentUser?.uid ?: ""

            LaunchedEffect(userId) {
                if (userId.isNotEmpty()) {
                    val userRepo = RepositoryProvider.getUserProfileRepository()
                    val profile = userRepo.getUserProfile(userId)
                    if (profile != null) {
                        Log.d("MainActivity", "✅ User profile loaded: ${profile.username}, Level ${profile.level}, XP ${profile.totalXP}")
                        Log.d("MainActivity", "📶 Connection: $connectionStatus")
                    }
                }
            }

            MainMenuScreen(
                userEmail = userEmail,
                onArcadeModeClick = {
                    showMainMenu = false
                    showArcadeMode = true
                },
                onAdventureModeClick = {
                    showMainMenu = false
                    showThemeSelection = true
                },
                onMultiplayerClick = {
                    showMainMenu = false
                    showMultiplayerSetup = true
                },
                onStatisticsClick = {},
                onSettingsClick = {
                    showMainMenu = false
                    showSettingsScreen = true
                },
                onProfileClick = {}
            )
        }

        else -> {
            Text(
                text = "Authentication Successful! Game Screen Coming Soon...",
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showBiometricDialog && activity != null) {
        AlertDialog(
            onDismissRequest = {
                showBiometricDialog = false
                showMainMenu = true
            },
            title = {
                Text(text = "Enable Fingerprint Login?")
            },
            text = {
                Text(text = "Use your fingerprint to login faster next time. You can change this later in Settings.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        BiometricHelper.showBiometricPrompt(
                            activity = activity,
                            title = "Setup Fingerprint",
                            subtitle = "Scan your fingerprint to enable quick login",
                            negativeButtonText = "Cancel",
                            onSuccess = {
                                BiometricHelper.setBiometricEnabled(context, true)
                                AuthManager.saveBiometricCredentials(context)
                                showBiometricDialog = false
                                showMainMenu = true
                            },
                            onError = { error ->
                                showBiometricDialog = false
                                showMainMenu = true
                            },
                            onFailed = {
                                showBiometricDialog = false
                                showMainMenu = true
                            }
                        )
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBiometricDialog = false
                        showMainMenu = true
                    }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
}