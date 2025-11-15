package vcmsa.projects.prog7314.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.AuthResult
import vcmsa.projects.prog7314.utils.BiometricHelper

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var loginAttempts by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val coroutineScope = rememberCoroutineScope()

    // Read string resources during composition
    val fillAllFieldsMsg = stringResource(R.string.please_fill_all_login_fields)
    val validEmailMsg = stringResource(R.string.please_enter_valid_email_login)
    val offlineLoginFailedMsg = stringResource(R.string.offline_login_failed)
    val googleSigninNotInitMsg = stringResource(R.string.google_signin_not_initialized)
    val tooManyAttemptsMsg = "Too many failed login attempts. Please try again later."
    val invalidCredentialsMsg = "Invalid email or password. Please try again."

    // Check biometric availability on each composition (to handle logout case)
    val hasSavedCredentials by remember { derivedStateOf { AuthManager.hasSavedCredentials(context) } }
    val isBiometricEnabled by remember { derivedStateOf { BiometricHelper.isBiometricEnabled(context) } }
    val isBiometricAvailable by remember { derivedStateOf { BiometricHelper.isBiometricAvailable(context) } }
    val canUseBiometricLogin = hasSavedCredentials && isBiometricEnabled && isBiometricAvailable

    // Trigger biometric prompt when conditions are met
    LaunchedEffect(canUseBiometricLogin) {
        if (canUseBiometricLogin && activity != null && !showBiometricPrompt) {
            showBiometricPrompt = true
        }
    }

    // Handle biometric authentication
    if (showBiometricPrompt && activity != null && canUseBiometricLogin) {
        LaunchedEffect(Unit) {
            BiometricHelper.showBiometricLoginPrompt(
                activity = activity,
                onSuccess = {
                    val offlineLoginSuccess = AuthManager.performOfflineLogin(context)
                    if (offlineLoginSuccess) {
                        Log.d("LoginScreen", "✅ Offline biometric login successful")
                        loginAttempts = 0
                        onLoginSuccess()
                    } else {
                        showBiometricPrompt = false
                        errorMessage = offlineLoginFailedMsg
                    }
                },
                onError = { error ->
                    showBiometricPrompt = false
                    Log.e("LoginScreen", "Biometric error: $error")
                    // Don't show error to user if they cancelled biometric
                    if (!error.contains("cancelled", ignoreCase = true) &&
                        !error.contains("canceled", ignoreCase = true)) {
                        errorMessage = error
                    }
                }
            )
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            coroutineScope.launch {
                isLoading = true
                errorMessage = ""
                when (val authResult = AuthManager.signInWithGoogle(account)) {
                    is AuthResult.Success -> {
                        val userId = authResult.user?.uid ?: ""
                        val userEmail = authResult.user?.email ?: ""
                        val username = authResult.user?.displayName ?: userEmail.substringBefore("@")

                        val userRepo = RepositoryProvider.getUserProfileRepository()
                        val existingProfile = userRepo.getUserProfile(userId)
                        if (existingProfile == null) {
                            userRepo.createNewUserProfile(userId, username, userEmail)
                            Log.d("LoginScreen", "✅ New Google user profile created")
                        }

                        // Save credentials for biometric login after Google sign-in
                        AuthManager.saveBiometricCredentials(context)

                        isLoading = false
                        loginAttempts = 0
                        onLoginSuccess()
                    }
                    is AuthResult.Error -> {
                        isLoading = false
                        errorMessage = "Google sign-in failed: ${authResult.message}"
                        Log.e("LoginScreen", "Google sign-in error: ${authResult.message}")
                    }
                }
            }
        } catch (e: ApiException) {
            isLoading = false
            errorMessage = "Google sign-in failed (${e.statusCode}): ${e.message ?: "Unknown error"}"
            Log.e("LoginScreen", "Google API Exception: ${e.message}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00BCD4), Color(0xFF0288D1))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.transparent_logo),
                contentDescription = "Memory Match Madness Logo",
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Fit
            )

            // Title with shadow effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                repeat(3) {
                    Text(
                        text = stringResource(R.string.test_your_memory).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black.copy(alpha = 0.3f),
                        modifier = Modifier.offset(x = 1.dp, y = 1.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.test_your_memory).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            // Main Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Login Title
                    Text(
                        text = stringResource(R.string.login_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Biometric Login Section (if available)
                    if (canUseBiometricLogin) {
                        val savedEmail = AuthManager.getSavedEmail(context) ?: ""
                        Text(
                            text = stringResource(R.string.welcome_back_email, savedEmail),
                            fontSize = 14.sp,
                            color = Color(0xFF0288D1),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Button(
                            onClick = { showBiometricPrompt = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(bottom = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_secure),
                                contentDescription = "Biometric Login",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.login_with_fingerprint),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.dp,
                            color = Color.Gray.copy(alpha = 0.3f)
                        )

                        Text(
                            text = stringResource(R.string.or_login_with_password),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Error Message Display
                    if (errorMessage.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                                    contentDescription = "Error",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp)
                                )
                                Text(
                                    text = errorMessage,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Email Input Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = ""
                        },
                        label = { Text(stringResource(R.string.email)) },
                        placeholder = { Text("Enter your email address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0288D1),
                            focusedLabelColor = Color(0xFF0288D1),
                            cursorColor = Color(0xFF0288D1)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Password Input Field with Visibility Toggle
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = { Text(stringResource(R.string.password)) },
                        placeholder = { Text("Enter your password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = !isLoading,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Filled.Visibility
                                    else
                                        Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible)
                                        "Hide password"
                                    else
                                        "Show password",
                                    tint = Color(0xFF0288D1)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0288D1),
                            focusedLabelColor = Color(0xFF0288D1),
                            cursorColor = Color(0xFF0288D1)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Login Button
                    Button(
                        onClick = {
                            // Check for too many failed attempts
                            if (loginAttempts >= 5) {
                                errorMessage = tooManyAttemptsMsg
                                return@Button
                            }

                            when {
                                email.isEmpty() || password.isEmpty() -> {
                                    errorMessage = fillAllFieldsMsg
                                }
                                !email.contains("@") || !email.contains(".") -> {
                                    errorMessage = validEmailMsg
                                }
                                password.length < 6 -> {
                                    errorMessage = "Password must be at least 6 characters"
                                }
                                else -> {
                                    isLoading = true
                                    errorMessage = ""
                                    coroutineScope.launch {
                                        when (val result = AuthManager.loginWithEmail(email, password)) {
                                            is AuthResult.Success -> {
                                                val userId = result.user?.uid ?: ""
                                                val userRepo = RepositoryProvider.getUserProfileRepository()
                                                val existingProfile = userRepo.getUserProfile(userId)
                                                if (existingProfile == null) {
                                                    userRepo.createNewUserProfile(
                                                        userId,
                                                        email.substringBefore("@"),
                                                        email
                                                    )
                                                }

                                                // Save credentials for biometric login
                                                AuthManager.saveBiometricCredentials(context)

                                                isLoading = false
                                                loginAttempts = 0
                                                onLoginSuccess()
                                            }
                                            is AuthResult.Error -> {
                                                isLoading = false
                                                loginAttempts++

                                                // Provide user-friendly error messages
                                                errorMessage = when {
                                                    result.message.contains("password", ignoreCase = true) ||
                                                            result.message.contains("email", ignoreCase = true) ||
                                                            result.message.contains("credential", ignoreCase = true) ->
                                                        invalidCredentialsMsg
                                                    result.message.contains("network", ignoreCase = true) ->
                                                        "Network error. Please check your connection."
                                                    result.message.contains("disabled", ignoreCase = true) ->
                                                        "This account has been disabled. Please contact support."
                                                    else ->
                                                        "Login failed: ${result.message}"
                                                }

                                                Log.e("LoginScreen", "Login failed (attempt $loginAttempts): ${result.message}")
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(bottom = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(26.dp),
                        enabled = !isLoading && loginAttempts < 5,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.play_now),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Divider
                    Text(
                        text = stringResource(R.string.or_continue_with),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Google Sign-In Button
                    Image(
                        painter = painterResource(id = R.drawable.android_neutral),
                        contentDescription = stringResource(R.string.sign_in_with_google),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(56.dp)
                            .padding(bottom = 12.dp)
                            .clickable(enabled = !isLoading) {
                                isLoading = true
                                errorMessage = ""
                                val signInIntent = AuthManager.getGoogleSignInIntent()
                                if (signInIntent != null) {
                                    googleSignInLauncher.launch(signInIntent)
                                } else {
                                    isLoading = false
                                    errorMessage = googleSigninNotInitMsg
                                }
                            },
                        contentScale = ContentScale.Fit
                    )

                    // Create New Account Button
                    TextButton(
                        onClick = onNavigateToRegister,
                        enabled = !isLoading
                    ) {
                        Text(
                            text = stringResource(R.string.create_new_account),
                            color = Color(0xFF0288D1),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}