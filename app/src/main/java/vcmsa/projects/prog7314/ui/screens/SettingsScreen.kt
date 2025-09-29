package vcmsa.projects.prog7314.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.BiometricHelper
import vcmsa.projects.prog7314.utils.SettingsManager

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val coroutineScope = rememberCoroutineScope()

    // State for all settings
    var language by remember { mutableStateOf(SettingsManager.getLanguage(context)) }
    var notificationsEnabled by remember { mutableStateOf(SettingsManager.isNotificationsEnabled(context)) }
    var dailyReminderEnabled by remember { mutableStateOf(SettingsManager.isDailyReminderEnabled(context)) }
    var achievementAlertsEnabled by remember { mutableStateOf(SettingsManager.isAchievementAlertsEnabled(context)) }
    var soundEffectsEnabled by remember { mutableStateOf(SettingsManager.isSoundEffectsEnabled(context)) }
    var backgroundMusicEnabled by remember { mutableStateOf(SettingsManager.isBackgroundMusicEnabled(context)) }
    var cardBackground by remember { mutableStateOf(SettingsManager.getCardBackground(context)) }
    var highContrastMode by remember { mutableStateOf(SettingsManager.isHighContrastMode(context)) }
    var biometricEnabled by remember { mutableStateOf(BiometricHelper.isBiometricEnabled(context)) }

    // Dialog states
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCardBackgroundDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }

    // Check if biometric is available
    val isBiometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

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
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text(
                        text = "←",
                        fontSize = 32.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = "SETTINGS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Scrollable Settings Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // APPEARANCE SECTION
                SectionHeader("APPEARANCE")

                SettingsCard(
                    title = "Card Background",
                    subtitle = "Change theme and colors",
                    onClick = { showCardBackgroundDialog = true }
                )

                SettingsToggleCard(
                    title = "High Contrast Mode",
                    subtitle = "Easier visibility",
                    checked = highContrastMode,
                    onCheckedChange = { enabled ->
                        highContrastMode = enabled
                        SettingsManager.setHighContrastMode(context, enabled)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // LANGUAGE SECTION
                SectionHeader("LANGUAGE")

                SettingsCard(
                    title = "Language",
                    subtitle = getLanguageName(language),
                    onClick = { showLanguageDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // NOTIFICATIONS SECTION
                SectionHeader("NOTIFICATIONS")

                SettingsToggleCard(
                    title = "Enable Notifications",
                    subtitle = "Receive app notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        notificationsEnabled = enabled
                        SettingsManager.setNotificationsEnabled(context, enabled)
                    }
                )

                if (notificationsEnabled) {
                    SettingsToggleCard(
                        title = "Daily Reminder",
                        subtitle = "Get daily play reminders",
                        checked = dailyReminderEnabled,
                        onCheckedChange = { enabled ->
                            dailyReminderEnabled = enabled
                            SettingsManager.setDailyReminderEnabled(context, enabled)
                        }
                    )

                    SettingsToggleCard(
                        title = "Achievement Alerts",
                        subtitle = "Celebrate your wins",
                        checked = achievementAlertsEnabled,
                        onCheckedChange = { enabled ->
                            achievementAlertsEnabled = enabled
                            SettingsManager.setAchievementAlertsEnabled(context, enabled)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BIOMETRIC & SECURITY SECTION
                if (isBiometricAvailable) {
                    SectionHeader("BIOMETRIC & SECURITY")

                    SettingsToggleCard(
                        title = "Biometric Login",
                        subtitle = if (biometricEnabled) "Tap to disable" else "Tap to enable",
                        checked = biometricEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && activity != null) {
                                // Enable biometric
                                BiometricHelper.showBiometricPrompt(
                                    activity = activity,
                                    title = "Enable Biometric",
                                    subtitle = "Scan to enable quick login",
                                    negativeButtonText = "Cancel",
                                    onSuccess = {
                                        BiometricHelper.setBiometricEnabled(context, true)
                                        AuthManager.saveBiometricCredentials(context)
                                        biometricEnabled = true
                                    },
                                    onError = { },
                                    onFailed = { }
                                )
                            } else {
                                // Disable biometric
                                BiometricHelper.setBiometricEnabled(context, false)
                                AuthManager.clearSavedCredentials(context)
                                biometricEnabled = false
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ACCOUNT SECTION
                SectionHeader("ACCOUNT")

                SettingsCard(
                    title = "Edit Profile",
                    subtitle = "Change name and avatar",
                    onClick = onEditProfile
                )

                SettingsCard(
                    title = "Password & Login",
                    subtitle = "Update login preferences",
                    onClick = onChangePassword
                )

                SettingsCard(
                    title = "Delete Account",
                    subtitle = "Permanently delete your account",
                    onClick = { showDeleteAccountDialog = true },
                    textColor = Color.Red
                )

                Spacer(modifier = Modifier.height(16.dp))

                // AUDIO SECTION
                SectionHeader("AUDIO")

                SettingsToggleCard(
                    title = "Sound Effects",
                    subtitle = "Game sounds",
                    checked = soundEffectsEnabled,
                    onCheckedChange = { enabled ->
                        soundEffectsEnabled = enabled
                        SettingsManager.setSoundEffectsEnabled(context, enabled)
                    }
                )

                SettingsToggleCard(
                    title = "Background Music",
                    subtitle = "In-game music",
                    checked = backgroundMusicEnabled,
                    onCheckedChange = { enabled ->
                        backgroundMusicEnabled = enabled
                        SettingsManager.setBackgroundMusicEnabled(context, enabled)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ABOUT SECTION
                SectionHeader("ABOUT")

                SettingsCard(
                    title = "About the App",
                    subtitle = "App version and information",
                    onClick = { /* TODO: Show about dialog */ }
                )

                Text(
                    text = "Version 1.0.0",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp)
                )

                // RESET BUTTON
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5722) // Deep Orange
                    )
                ) {
                    Text("RESET TO DEFAULTS")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = language,
            onLanguageSelected = { selectedLanguage ->
                language = selectedLanguage
                SettingsManager.setLanguage(context, selectedLanguage)
                showLanguageDialog = false
                showRestartDialog = true
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Card Background Selection Dialog
    if (showCardBackgroundDialog) {
        CardBackgroundDialog(
            currentBackground = cardBackground,
            onBackgroundSelected = { selected ->
                cardBackground = selected
                SettingsManager.setCardBackground(context, selected)
                showCardBackgroundDialog = false
            },
            onDismiss = { showCardBackgroundDialog = false }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onConfirm = {
                // TODO: Implement account deletion
                showDeleteAccountDialog = false
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    // Reset to Defaults Confirmation Dialog
    if (showResetDialog) {
        ResetSettingsDialog(
            onConfirm = {
                SettingsManager.resetToDefaults(context)
                BiometricHelper.setBiometricEnabled(context, false)
                // Reload all settings
                language = SettingsManager.getLanguage(context)
                notificationsEnabled = SettingsManager.isNotificationsEnabled(context)
                dailyReminderEnabled = SettingsManager.isDailyReminderEnabled(context)
                achievementAlertsEnabled = SettingsManager.isAchievementAlertsEnabled(context)
                soundEffectsEnabled = SettingsManager.isSoundEffectsEnabled(context)
                backgroundMusicEnabled = SettingsManager.isBackgroundMusicEnabled(context)
                cardBackground = SettingsManager.getCardBackground(context)
                highContrastMode = SettingsManager.isHighContrastMode(context)
                biometricEnabled = BiometricHelper.isBiometricEnabled(context)
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    // Restart Required Dialog
    if (showRestartDialog) {
        RestartRequiredDialog(
            onDismiss = { showRestartDialog = false }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.9f),
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = Color.Black
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SettingsToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

fun getLanguageName(code: String): String {
    return when (code) {
        "en" -> "English"
        "af" -> "Afrikaans"
        "zu" -> "isiZulu"
        else -> "English"
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}

// ===== DIALOG COMPONENTS =====

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Select Language")
        },
        text = {
            Column {
                LanguageOption(
                    language = "English",
                    code = "en",
                    isSelected = currentLanguage == "en",
                    onClick = { onLanguageSelected("en") }
                )
                LanguageOption(
                    language = "Afrikaans",
                    code = "af",
                    isSelected = currentLanguage == "af",
                    onClick = { onLanguageSelected("af") }
                )
                LanguageOption(
                    language = "isiZulu",
                    code = "zu",
                    isSelected = currentLanguage == "zu",
                    onClick = { onLanguageSelected("zu") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LanguageOption(
    language: String,
    code: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = language,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Text(
                text = "✓",
                fontSize = 20.sp,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun CardBackgroundDialog(
    currentBackground: String,
    onBackgroundSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Card Background Theme")
        },
        text = {
            Column {
                BackgroundOption(
                    name = "Blue Ocean",
                    code = "blue",
                    isSelected = currentBackground == "blue",
                    onClick = { onBackgroundSelected("blue") }
                )
                BackgroundOption(
                    name = "Green Forest",
                    code = "green",
                    isSelected = currentBackground == "green",
                    onClick = { onBackgroundSelected("green") }
                )
                BackgroundOption(
                    name = "Purple Galaxy",
                    code = "purple",
                    isSelected = currentBackground == "purple",
                    onClick = { onBackgroundSelected("purple") }
                )
                BackgroundOption(
                    name = "Orange Sunset",
                    code = "orange",
                    isSelected = currentBackground == "orange",
                    onClick = { onBackgroundSelected("orange") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BackgroundOption(
    name: String,
    code: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when (code) {
                            "blue" -> Color(0xFF2196F3)
                            "green" -> Color(0xFF4CAF50)
                            "purple" -> Color(0xFF9C27B0)
                            "orange" -> Color(0xFFFF9800)
                            else -> Color.Gray
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
        if (isSelected) {
            Text(
                text = "✓",
                fontSize = 20.sp,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Account?",
                color = Color.Red
            )
        },
        text = {
            Text(text = "This action cannot be undone. All your progress, achievements, and data will be permanently deleted.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ResetSettingsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Reset to Defaults?")
        },
        text = {
            Text(text = "This will reset all settings to their default values. Your account and game progress will not be affected.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RestartRequiredDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Restart Required")
        },
        text = {
            Text(text = "Please restart the app for the language change to take effect.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}