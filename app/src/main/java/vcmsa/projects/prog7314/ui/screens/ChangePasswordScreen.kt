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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.utils.AuthManager
import vcmsa.projects.prog7314.utils.AuthResult
/*
    Code Attribution for: Developing Kotlin Game Application
    ===================================================
    Dentistkiller, 2025. X and O - Android Tic Tac Toe Game | Kotlin (Version 2.2.21) [Source code].
    Available at: <https://github.com/Dentistkiller/TicTacToe>
    [Accessed 18 November 2025].
*/

/**
 * ChangePasswordScreen
 *
 * This Composable provides a user interface for changing the current user's password.
 * Users must enter their current password, a new password, and confirm the new password.
 * The screen handles validation, shows errors or success messages, and interacts with AuthManager
 * to reauthenticate the user and update their password in Firebase.
 *
 * Parameters:
 * - onBackClick: () -> Unit -> Callback when the user presses the back or cancel button.
 * - onChangeSuccess: () -> Unit -> Callback when the password change is successful (after delay).
 *
 * Key Concepts:
 * 1. State Management:
 *    - Uses `remember` to store the current, new, and confirm password fields.
 *    - Tracks visibility toggles for each password input.
 *    - Tracks loading state, error messages, and success messages.
 *    - Tracks validation errors for each field.
 *
 * 2. Validation:
 *    - Validates that current password is entered.
 *    - Validates that new password is at least 6 characters and differs from current password.
 *    - Validates that confirmation matches new password.
 *
 * 3. Password Change Workflow:
 *    - Clears keyboard focus before processing.
 *    - Runs validation functions and stops if any fail.
 *    - Uses coroutineScope to reauthenticate user via AuthManager.
 *    - On successful reauthentication, attempts to change password.
 *    - Shows success or error messages based on the result.
 *    - Calls onChangeSuccess() after a short delay if password changed successfully.
 *
 * 4. UI Layout:
 *    - Uses a Box with vertical gradient background.
 *    - Column organizes the header, scrollable form, and action buttons.
 *    - Header contains back button and screen title.
 *    - Form is inside a Card with white semi-transparent background and rounded corners.
 *    - Form fields are OutlinedTextFields with show/hide password toggles.
 *    - Shows field-specific error messages and global success/error messages.
 *    - "Change Password" button shows a loading indicator when processing.
 *    - "Cancel" button calls onBackClick and uses outlined styling.
 *
 * 5. UX Features:
 *    - Password visibility toggle for all fields using trailing icons.
 *    - Keyboard actions integrated (Next / Done) for smooth form navigation.
 *    - Buttons are disabled during loading to prevent multiple submissions.
 *    - Clear feedback for users via error messages, success messages, and loading indicators.
 *
 * 6. Design Choices:
 *    - Gradient background and rounded card for modern look.
 *    - Color coding for success (green) and errors (red).
 *    - Modular structure allows reuse of validation logic and field components if needed.
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit = {},
    onChangeSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
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

    // Read string resources during composition
    val currentPasswordRequiredMsg = stringResource(R.string.current_password_required)
    val newPasswordRequiredMsg = stringResource(R.string.new_password_required)
    val passwordMin6Msg = stringResource(R.string.password_min_6_chars)
    val newPasswordDifferentMsg = stringResource(R.string.new_password_different)
    val confirmPasswordMsg = stringResource(R.string.please_confirm_password)
    val passwordsNoMatchMsg = stringResource(R.string.passwords_do_not_match)
    val currentPasswordIncorrectMsg = stringResource(R.string.current_password_incorrect)
    val passwordChangedMsg = stringResource(R.string.password_changed_successfully)

    fun validateCurrentPassword(): Boolean {
        currentPasswordError = when {
            currentPassword.isBlank() -> currentPasswordRequiredMsg
            else -> ""
        }
        return currentPasswordError.isEmpty()
    }

    fun validateNewPassword(): Boolean {
        newPasswordError = when {
            newPassword.isBlank() -> newPasswordRequiredMsg
            newPassword.length < 6 -> passwordMin6Msg
            newPassword == currentPassword -> newPasswordDifferentMsg
            else -> ""
        }
        return newPasswordError.isEmpty()
    }

    fun validateConfirmPassword(): Boolean {
        confirmPasswordError = when {
            confirmPassword.isBlank() -> confirmPasswordMsg
            confirmPassword != newPassword -> passwordsNoMatchMsg
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
                val reauthResult = AuthManager.reauthenticateUser(userEmail, currentPassword)

                if (reauthResult is AuthResult.Error) {
                    errorMessage = currentPasswordIncorrectMsg
                    isLoading = false
                    return@launch
                }

                val changeResult = AuthManager.changePassword(newPassword)

                if (changeResult is AuthResult.Success) {
                    successMessage = passwordChangedMsg
                    Log.d("ChangePasswordScreen", "✅ Password changed")

                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""

                    kotlinx.coroutines.delay(2000)
                    onChangeSuccess()
                } else if (changeResult is AuthResult.Error) {
                    errorMessage = context.getString(R.string.failed_to_change_password, changeResult.message)
                    Log.e("ChangePasswordScreen", "❌ Password change failed: ${changeResult.message}")
                }
            } catch (e: Exception) {
                errorMessage = context.getString(R.string.failed_to_change_password, e.message ?: "Unknown error")
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
                    text = stringResource(R.string.change_password_title),
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
                            text = stringResource(R.string.update_your_password),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.enter_current_and_new),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = stringResource(R.string.current_password),
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
                            placeholder = { Text(stringResource(R.string.enter_current_password)) },
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

                        Text(
                            text = stringResource(R.string.new_password),
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
                            placeholder = { Text(stringResource(R.string.enter_new_password)) },
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
                                        text = stringResource(R.string.at_least_6_characters),
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

                        Text(
                            text = stringResource(R.string.confirm_new_password),
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
                            placeholder = { Text(stringResource(R.string.reenter_new_password)) },
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

                        if (successMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = successMessage,
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

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
                            text = stringResource(R.string.change_password_button),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        text = stringResource(R.string.cancel),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}