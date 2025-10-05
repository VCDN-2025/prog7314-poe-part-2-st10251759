package vcmsa.projects.prog7314.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.data.models.CardBackground
import vcmsa.projects.prog7314.data.repository.CardBackgroundRepository
import vcmsa.projects.prog7314.ui.viewmodels.CardBackgroundViewModel
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.BiometricHelper
import vcmsa.projects.prog7314.utils.ProfileImageHelper
import vcmsa.projects.prog7314.utils.SettingsManager

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onLogout: () -> Unit = {},
    cardBackgroundViewModel: CardBackgroundViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val coroutineScope = rememberCoroutineScope()

    val currentUser = AuthManager.getCurrentUser()
    val userEmail = currentUser?.email ?: ""
    val userName = currentUser?.displayName ?: userEmail.substringBefore("@")
    val userInitials = if (userEmail.isNotEmpty()) {
        userEmail.substring(0, minOf(2, userEmail.length)).uppercase()
    } else "U"

    var profileImageBase64 by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Card Background State
    val selectedCardBackground by cardBackgroundViewModel.selectedCardBackground.collectAsState()

    LaunchedEffect(Unit) {
        // Try to load from local prefs first (faster)
        val localImage = ProfileImageHelper.loadFromLocalPrefs(context)
        if (localImage != null) {
            profileImageBase64 = localImage
        }

        // Then load from Firestore (authoritative source)
        val result = ProfileImageHelper.loadProfileImageBase64()
        if (result.isSuccess) {
            val base64 = result.getOrNull()
            if (base64 != null) {
                profileImageBase64 = base64
            }
        }

        // Load card background
        cardBackgroundViewModel.loadCardBackground()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("SettingsScreen", "Image picker returned: $uri")
        uri?.let {
            Log.d("SettingsScreen", "Starting upload...")
            isUploadingImage = true

            coroutineScope.launch {
                Log.d("SettingsScreen", "Compressing and saving image...")
                val result = ProfileImageHelper.saveProfileImage(context, it)
                if (result.isSuccess) {
                    profileImageBase64 = result.getOrNull()
                    Log.d("SettingsScreen", "✅ Image saved")
                } else {
                    Log.e("SettingsScreen", "❌ Image save failed: ${result.exceptionOrNull()?.message}")
                }
                isUploadingImage = false
            }
        } ?: run {
            Log.d("SettingsScreen", "No image selected (uri was null)")
        }
    }

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00BCD4))
                            .border(4.dp, Color.White, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageBase64 != null) {
                            // Decode Base64 to Bitmap and display
                            val bitmap = remember(profileImageBase64) {
                                try {
                                    val decodedBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                } catch (e: Exception) {
                                    Log.e("SettingsScreen", "Error decoding image", e)
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Fallback to initials if decode fails
                                Text(
                                    text = userInitials,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Text(
                                text = userInitials,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (isUploadingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
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
                        text = if (isUploadingImage) "Saving..." else "Tap photo to change",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

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

                SectionHeader("APPEARANCE")

                SettingsCard(
                    title = "Card Background",
                    subtitle = selectedCardBackground.displayName,
                    onClick = { showCardBackgroundDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("LANGUAGE")

                SettingsCardDisabled(
                    title = "Multi-Language",
                    subtitle = "Coming in Part 3"
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("NOTIFICATIONS")

                SettingsCardDisabled(
                    title = "Notifications",
                    subtitle = "Coming in Part 3"
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                SectionHeader("AUDIO")

                SettingsCardDisabled(
                    title = "Sound Effects & Music",
                    subtitle = "Coming in Part 3"
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader("ABOUT")

                SettingsCard(
                    title = "About the App",
                    subtitle = "Version 1.0.0",
                    onClick = { showAboutDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

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

    if (showCardBackgroundDialog) {
        NewCardBackgroundDialog(
            currentBackground = selectedCardBackground,
            onBackgroundSelected = { selected ->
                cardBackgroundViewModel.setCardBackground(selected)
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
                cardBackgroundViewModel.setCardBackground(CardBackground.DEFAULT)
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

// All the other composable functions remain the same...
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
fun NewCardBackgroundDialog(
    currentBackground: CardBackground,
    onBackgroundSelected: (CardBackground) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Card Background Theme",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                CardBackground.values().forEach { background ->
                    NewBackgroundOption(
                        cardBackground = background,
                        isSelected = currentBackground == background,
                        onClick = { onBackgroundSelected(background) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun NewBackgroundOption(
    cardBackground: CardBackground,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

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
            // Preview of card background
            val drawableId = CardBackgroundRepository.getCardBackgroundDrawable(
                context,
                cardBackground
            )

            if (drawableId != 0) {
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = cardBackground.displayName,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = cardBackground.displayName,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (isSelected) {
            Text(
                text = "✓",
                fontSize = 20.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
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