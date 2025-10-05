package vcmsa.projects.prog7314.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.AuthResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit = {},
    onChangeSuccess: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val currentUser = AuthManager.getCurrentUser()
    val userEmail = currentUser?.email ?: ""

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    // Validation states
    var currentPasswordError by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    fun validateCurrentPassword(): Boolean {
        currentPasswordError = when {
            currentPassword.isBlank() -> "Current password is required"
            else -> ""
        }
        return currentPasswordError.isEmpty()
    }

    fun validateNewPassword(): Boolean {
        newPasswordError = when {
            newPassword.isBlank() -> "New password is required"
            newPassword.length < 6 -> "Password must be at least 6 characters"
            newPassword == currentPassword -> "New password must be different from current password"
            else -> ""
        }
        return newPasswordError.isEmpty()
    }

    fun validateConfirmPassword(): Boolean {
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Please confirm your password"
            confirmPassword != newPassword -> "Passwords do not match"
            else -> ""
        }
        return confirmPasswordError.isEmpty()
    }

    fun changePassword() {
        focusManager.clearFocus()

        val isCurrentValid = validateCurrentPassword()
        val isNewValid = validateNewPassword()
        val isConfirmValid = validateConfirmPassword()

        if (!isCurrentValid || !isNewValid || !isConfirmValid) return

        isLoading = true
        errorMessage = ""
        successMessage = ""

        coroutineScope.launch {
            try {
                // First, reauthenticate the user with their current password
                val reauthResult = AuthManager.reauthenticateUser(userEmail, currentPassword)

                if (reauthResult is AuthResult.Error) {
                    errorMessage = "Current password is incorrect"
                    isLoading = false
                    return@launch
                }

                // If reauthentication succeeds, change the password
                val changeResult = AuthManager.changePassword(newPassword)

                if (changeResult is AuthResult.Success) {
                    successMessage = "Password changed successfully!"
                    Log.d("ChangePasswordScreen", "✅ Password changed")

                    // Clear fields
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""

                    // Delay and navigate back
                    kotlinx.coroutines.delay(2000)
                    onChangeSuccess()
                } else if (changeResult is AuthResult.Error) {
                    errorMessage = changeResult.message
                    Log.e("ChangePasswordScreen", "❌ Password change failed: ${changeResult.message}")
                }
            } catch (e: Exception) {
                errorMessage = "Failed to change password: ${e.message}"
                Log.e("ChangePasswordScreen", "❌ Error: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
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
            // Header
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
                    text = "CHANGE PASSWORD",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Password Change Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Update Your Password",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Enter your current password and choose a new one",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Current Password
                        Text(
                            text = "Current Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = {
                                currentPassword = it
                                currentPasswordError = ""
                                errorMessage = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter current password") },
                            visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                    Text(
                                        text = if (showCurrentPassword) "👁" else "👁️‍🗨️",
                                        fontSize = 18.sp
                                    )
                                }
                            },
                            isError = currentPasswordError.isNotEmpty(),
                            supportingText = {
                                if (currentPasswordError.isNotEmpty()) {
                                    Text(
                                        text = currentPasswordError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00BCD4),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // New Password
                        Text(
                            text = "New Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                newPasswordError = ""
                                errorMessage = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter new password") },
                            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Text(
                                        text = if (showNewPassword) "👁" else "👁️‍🗨️",
                                        fontSize = 18.sp
                                    )
                                }
                            },
                            isError = newPasswordError.isNotEmpty(),
                            supportingText = {
                                if (newPasswordError.isNotEmpty()) {
                                    Text(
                                        text = newPasswordError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        text = "At least 6 characters",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00BCD4),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Password
                        Text(
                            text = "Confirm New Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                confirmPasswordError = ""
                                errorMessage = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Re-enter new password") },
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Text(
                                        text = if (showConfirmPassword) "👁" else "👁️‍🗨️",
                                        fontSize = 18.sp
                                    )
                                }
                            },
                            isError = confirmPasswordError.isNotEmpty(),
                            supportingText = {
                                if (confirmPasswordError.isNotEmpty()) {
                                    Text(
                                        text = confirmPasswordError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { changePassword() }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00BCD4),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        )

                        // Success Message
                        if (successMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = successMessage,
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Error Message
                        if (errorMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Change Password Button
                Button(
                    onClick = { changePassword() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "CHANGE PASSWORD",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel Button
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(Color.White, Color.White))
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CANCEL",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}