package vcmsa.projects.prog7314.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.BiometricHelper
import vcmsa.projects.prog7314.utils.SettingsManager

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // GET REAL USER DATA
    val currentUser = AuthManager.getCurrentUser()
    val userEmail = currentUser?.email ?: ""
    val userName = currentUser?.displayName ?: userEmail.substringBefore("@")
    val userInitials = if (userEmail.isNotEmpty()) {
        userEmail.substring(0, minOf(2, userEmail.length)).uppercase()
    } else "U"

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    var cardBackground by remember { mutableStateOf(SettingsManager.getCardBackground(context)) }
    var biometricEnabled by remember { mutableStateOf(BiometricHelper.isBiometricEnabled(context)) }

    var showCardBackgroundDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("←", fontSize = 32.sp, color = Color.White)
                }
                Text(
                    text = "SETTINGS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // PROFILE SECTION - CENTERED DESIGN
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00BCD4))
                            .border(4.dp, Color.White, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUri != null) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = userInitials,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = userEmail,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Text(
                        text = "Tap photo to change",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // ACCOUNT SECTION
                SectionHeader("ACCOUNT")

                SettingsCard(
                    title = "Edit Profile",
                    subtitle = "Change name and details",
                    onClick = onEditProfile
                )

                SettingsCard(
                    title = "Change Password",
                    subtitle = "Update your password",
                    onClick = onChangePassword
                )

                SettingsCard(
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    onClick = { showLogoutDialog = true },
                    textColor = Color(0xFFFF5722)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // APPEARANCE
                SectionHeader("APPEARANCE")

                SettingsCard(
                    title = "Card Background",
                    subtitle = "Change theme and colors",
                    onClick = { showCardBackgroundDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // LANGUAGE (GREYED OUT)
                SectionHeader("LANGUAGE")

                SettingsCardDisabled(
                    title = "Multi-Language",
                    subtitle = "Coming in Part 3"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // NOTIFICATIONS (GREYED OUT)
                SectionHeader("NOTIFICATIONS")

                SettingsCardDisabled(
                    title = "Notifications",
                    subtitle = "Coming in Part 3"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // BIOMETRIC
                if (isBiometricAvailable) {
                    SectionHeader("BIOMETRIC & SECURITY")

                    SettingsToggleCard(
                        title = "Biometric Login",
                        subtitle = if (biometricEnabled) "Enabled" else "Disabled",
                        checked = biometricEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && activity != null) {
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
                                BiometricHelper.setBiometricEnabled(context, false)
                                AuthManager.clearSavedCredentials(context)
                                biometricEnabled = false
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // AUDIO (GREYED OUT)
                SectionHeader("AUDIO")

                SettingsCardDisabled(
                    title = "Sound Effects & Music",
                    subtitle = "Coming in Part 3"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ABOUT
                SectionHeader("ABOUT")

                SettingsCard(
                    title = "About the App",
                    subtitle = "Version 1.0.0",
                    onClick = { showAboutDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // RESET BUTTON
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5722)
                    )
                ) {
                    Text("RESET TO DEFAULTS")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Dialogs
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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        AuthManager.clearSavedCredentials(context)
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5722))
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetDialog) {
        ResetSettingsDialog(
            onConfirm = {
                SettingsManager.resetToDefaults(context)
                BiometricHelper.setBiometricEnabled(context, false)
                cardBackground = SettingsManager.getCardBackground(context)
                biometricEnabled = false
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Memory Match Madness", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Version: 1.0.0", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Memory Match Madness is a fun and challenging memory card game designed to test and improve your memory skills.",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("More information coming soon!", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
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
fun SettingsCard(title: String, subtitle: String, onClick: () -> Unit, textColor: Color = Color.Black) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SettingsCardDisabled(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun SettingsToggleCard(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun CardBackgroundDialog(currentBackground: String, onBackgroundSelected: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Card Background Theme") },
        text = {
            Column {
                BackgroundOption("Blue Ocean", "blue", currentBackground == "blue") { onBackgroundSelected("blue") }
                BackgroundOption("Green Forest", "green", currentBackground == "green") { onBackgroundSelected("green") }
                BackgroundOption("Purple Galaxy", "purple", currentBackground == "purple") { onBackgroundSelected("purple") }
                BackgroundOption("Orange Sunset", "orange", currentBackground == "orange") { onBackgroundSelected("orange") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun BackgroundOption(name: String, code: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp).background(
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
            Text(text = name, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
        if (isSelected) {
            Text(text = "✓", fontSize = 20.sp, color = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun ResetSettingsDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset to Defaults?") },
        text = { Text("This will reset all settings to their default values. Your account and game progress will not be affected.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Reset") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}