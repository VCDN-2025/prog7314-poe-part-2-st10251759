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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.repository.RepositoryProvider
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.AuthResult

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            coroutineScope.launch {
                isLoading = true
                when (val authResult = AuthManager.signInWithGoogle(account)) {
                    is AuthResult.Success -> {
                        val userId = authResult.user?.uid ?: ""
                        val userEmail = authResult.user?.email ?: ""
                        val username = authResult.user?.displayName ?: userEmail.substringBefore("@")

                        val userRepo = RepositoryProvider.getUserProfileRepository()
                        val existingProfile = userRepo.getUserProfile(userId)
                        if (existingProfile == null) {
                            userRepo.createNewUserProfile(userId, username, userEmail)
                            Log.d("RegisterScreen", "✅ New Google user profile created in database")
                        }

                        isLoading = false
                        onRegisterSuccess()
                    }
                    is AuthResult.Error -> {
                        isLoading = false
                        errorMessage = authResult.message
                    }
                }
            }
        } catch (e: ApiException) {
            isLoading = false
            errorMessage = "Google sign-up failed: ${e.message}"
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.transparent_logo),
                contentDescription = "Memory Match Madness Logo",
                modifier = Modifier.size(180.dp).padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                repeat(3) {
                    Text(
                        text = "TEST YOUR MEMORY SKILLS!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        modifier = Modifier.offset(x = 1.dp, y = 1.dp)
                    )
                }
                Text(
                    text = "TEST YOUR MEMORY SKILLS!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "REGISTER",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = "" },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = "" },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = "" },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Button(
                        onClick = {
                            when {
                                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                                    errorMessage = "Please fill in all fields"
                                }
                                password != confirmPassword -> {
                                    errorMessage = "Passwords do not match"
                                }
                                password.length < 6 -> {
                                    errorMessage = "Password must be at least 6 characters"
                                }
                                !email.contains("@") -> {
                                    errorMessage = "Please enter a valid email address"
                                }
                                else -> {
                                    isLoading = true
                                    errorMessage = ""

                                    coroutineScope.launch {
                                        when (val result = AuthManager.registerWithEmail(email, password)) {
                                            is AuthResult.Success -> {
                                                val userId = result.user?.uid ?: ""
                                                val userEmail = result.user?.email ?: ""
                                                val username = userEmail.substringBefore("@")

                                                val userRepo = RepositoryProvider.getUserProfileRepository()
                                                userRepo.createNewUserProfile(userId, username, userEmail)
                                                Log.d("RegisterScreen", "✅ New user profile created in database")

                                                isLoading = false
                                                onRegisterSuccess()
                                            }
                                            is AuthResult.Error -> {
                                                isLoading = false
                                                errorMessage = result.message
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(25.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("PLAY NOW", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Text("or continue with", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

                    Image(
                        painter = painterResource(id = R.drawable.android_neutral),
                        contentDescription = "Sign up with Google",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(60.dp)
                            .padding(bottom = 16.dp)
                            .clickable(enabled = !isLoading) {
                                isLoading = true
                                val signInIntent = AuthManager.getGoogleSignInIntent()
                                if (signInIntent != null) {
                                    googleSignInLauncher.launch(signInIntent)
                                } else {
                                    isLoading = false
                                    errorMessage = "Google Sign-In not initialized"
                                }
                            },
                        contentScale = ContentScale.Fit
                    )

                    TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
                        Text("Already Have an Account?", color = Color(0xFF0288D1))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}