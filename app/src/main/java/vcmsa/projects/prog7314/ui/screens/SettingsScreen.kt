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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.models.CardBackground
import vcmsa.projects.prog7314.data.repository.CardBackgroundRepository
import vcmsa.projects.prog7314.ui.viewmodels.CardBackgroundViewModel
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.BiometricHelper
import vcmsa.projects.prog7314.utils.LanguageManager
import vcmsa.projects.prog7314.utils.NotificationTracker
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

    // Language State
    val currentLanguage = remember { LanguageManager.getCurrentLanguage(context) }

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
    var showLanguageDialog by remember { mutableStateOf(false) }
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
            // Back button - matching arcade mode style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
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

            // Settings title - moved down
            Text(
                text = stringResource(R.string.settings).uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
            )

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
                        text = if (isUploadingImage) stringResource(R.string.saving) else stringResource(R.string.tap_photo_to_change),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                SectionHeader(stringResource(R.string.section_account))

                SettingsCard(
                    title = stringResource(R.string.edit_profile),
                    subtitle = stringResource(R.string.change_name_details),
                    onClick = onEditProfile
                )

                SettingsCard(
                    title = stringResource(R.string.change_password),
                    subtitle = stringResource(R.string.update_your_password),
                    onClick = onChangePassword
                )

                SettingsCard(
                    title = stringResource(R.string.logout),
                    subtitle = stringResource(R.string.sign_out_account),
                    onClick = { showLogoutDialog = true },
                    textColor = Color(0xFFFF5722)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader(stringResource(R.string.section_appearance))

                SettingsCard(
                    title = stringResource(R.string.card_background),
                    subtitle = selectedCardBackground.displayName,
                    onClick = { showCardBackgroundDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader(stringResource(R.string.section_language))

                SettingsCard(
                    title = stringResource(R.string.multi_language),
                    subtitle = currentLanguage.displayName,
                    onClick = { showLanguageDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader(stringResource(R.string.section_notifications))

                var notificationsEnabled by remember {
                    mutableStateOf(SettingsManager.isNotificationsEnabled(context))
                }

                SettingsToggleCard(
                    title = stringResource(R.string.push_notifications),
                    subtitle = if (notificationsEnabled)
                        stringResource(R.string.notifications_enabled)
                    else
                        stringResource(R.string.notifications_disabled),
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        SettingsManager.setNotificationsEnabled(context, enabled)
                        notificationsEnabled = enabled
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isBiometricAvailable) {
                    SectionHeader(stringResource(R.string.section_biometric_security))

                    // Read strings during composition
                    val enableBiometricText = stringResource(R.string.enable_biometric)
                    val scanToEnableText = stringResource(R.string.scan_to_enable)
                    val cancelText = stringResource(R.string.cancel)

                    SettingsToggleCard(
                        title = stringResource(R.string.biometric_login),
                        subtitle = if (biometricEnabled) stringResource(R.string.enabled) else stringResource(R.string.disabled),
                        checked = biometricEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && activity != null) {
                                BiometricHelper.showBiometricPrompt(
                                    activity = activity,
                                    title = enableBiometricText,
                                    subtitle = scanToEnableText,
                                    negativeButtonText = cancelText,
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



                SectionHeader(stringResource(R.string.section_about))

                SettingsCard(
                    title = stringResource(R.string.about_the_app),
                    subtitle = stringResource(R.string.version),
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
                    Text(stringResource(R.string.reset_to_defaults))
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

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { language ->
                LanguageManager.setLanguage(context, language)
                showLanguageDialog = false
                // Restart activity to apply language
                activity?.let { LanguageManager.restartActivity(it) }
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_question), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.logout_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 🔥 NEW: Clear notification tracking before logout
                        val userId = AuthManager.getCurrentUser()?.uid
                        if (userId != null) {
                            NotificationTracker.clearAllTracking(context, userId)
                            Log.d("SettingsScreen", "🗑️ Cleared notification tracking on logout")
                        }

                        // Existing logout code
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        AuthManager.clearSavedCredentials(context)
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5722))
                ) {
                    Text(stringResource(R.string.logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
            title = { Text(stringResource(R.string.about_title), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(stringResource(R.string.version_label), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.about_description),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.more_info_soon), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.ok))
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
fun NewCardBackgroundDialog(
    currentBackground: CardBackground,
    onBackgroundSelected: (CardBackground) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.card_background_theme),
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
                Text(stringResource(R.string.close))
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
fun LanguageSelectionDialog(
    currentLanguage: LanguageManager.Language,
    onLanguageSelected: (LanguageManager.Language) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.select_language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                LanguageManager.getAvailableLanguages().forEach { language ->
                    LanguageOption(
                        language = language,
                        isSelected = currentLanguage == language,
                        onClick = { onLanguageSelected(language) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun LanguageOption(
    language: LanguageManager.Language,
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
            text = language.displayName,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )

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
        title = { Text(stringResource(R.string.reset_question)) },
        text = { Text(stringResource(R.string.reset_confirmation)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.reset)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}