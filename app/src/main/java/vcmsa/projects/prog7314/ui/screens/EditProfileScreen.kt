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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vcmsa.projects.prog7314.R
import vcmsa.projects.prog7314.data.AppDatabase
import vcmsa.projects.prog7314.data.repository.UserProfileRepository
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
 * EditProfileScreen
 *
 * This Composable allows the user to view and edit their profile information.
 * The primary editable field is the display name, while the email is read-only.
 * The screen provides validation, feedback messages, and updates both Firebase
 * authentication and the local database on successful profile changes.
 *
 * Parameters:
 * - onBackClick: () -> Unit -> Callback triggered when the user presses the back or cancel button.
 * - onSaveSuccess: () -> Unit -> Callback triggered after successfully saving the profile.
 *
 * Key Concepts:
 * 1. State Management:
 *    - Uses `remember` to store displayName, loading state, error messages, and success messages.
 *    - Tracks validation errors for the display name.
 *
 * 2. Validation:
 *    - Display name cannot be blank.
 *    - Minimum length 2 characters, maximum 30 characters.
 *    - Only allows alphanumeric characters, underscores, and spaces.
 *
 * 3. Profile Save Workflow:
 *    - Clears keyboard focus before saving.
 *    - Validates the display name; exits if validation fails.
 *    - Calls AuthManager to update the display name in Firebase.
 *    - On success, updates the local Room database via UserProfileRepository.
 *    - Displays a success message and calls onSaveSuccess() after a short delay.
 *    - Displays error messages if saving fails.
 *
 * 4. UI Layout:
 *    - Box with vertical gradient background for modern look.
 *    - Column structure:
 *      - Header row with back button and title.
 *      - Scrollable content including profile card.
 *      - Card contains:
 *          - Read-only email field.
 *          - Editable display name field with validation errors.
 *          - Success and error messages.
 *      - Save and Cancel buttons at the bottom with proper styling.
 *
 * 5. UX Features:
 *    - Keyboard navigation using ImeAction.Done triggers saveProfile().
 *    - Buttons are disabled while loading to prevent multiple submissions.
 *    - Provides immediate feedback for validation errors.
 *    - Displays loading indicator on save button while profile is being updated.
 *
 * 6. Design Choices:
 *    - Semi-transparent white card with rounded corners for content separation.
 *    - Color-coded feedback: green for success, red for errors.
 *    - Consistent button styling and gradients matching the app theme.
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val currentUser = AuthManager.getCurrentUser()
    val userEmail = currentUser?.email ?: ""
    val currentDisplayName = currentUser?.displayName ?: userEmail.substringBefore("@")

    var displayName by remember { mutableStateOf(currentDisplayName) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    // Validation states
    var displayNameError by remember { mutableStateOf("") }

    // Read string resources during composition
    val displayNameEmptyMsg = stringResource(R.string.display_name_empty)
    val displayNameMin2Msg = stringResource(R.string.display_name_min_2)
    val displayNameMax30Msg = stringResource(R.string.display_name_max_30)
    val displayNameInvalidCharsMsg = stringResource(R.string.display_name_invalid_chars)
    val profileUpdatedMsg = stringResource(R.string.profile_updated_successfully)

    fun validateDisplayName(): Boolean {
        displayNameError = when {
            displayName.isBlank() -> displayNameEmptyMsg
            displayName.length < 2 -> displayNameMin2Msg
            displayName.length > 30 -> displayNameMax30Msg
            !displayName.matches(Regex("^[a-zA-Z0-9_ ]+$")) -> displayNameInvalidCharsMsg
            else -> ""
        }
        return displayNameError.isEmpty()
    }

    fun saveProfile() {
        focusManager.clearFocus()

        if (!validateDisplayName()) return

        isLoading = true
        errorMessage = ""
        successMessage = ""

        coroutineScope.launch {
            try {
                val result = AuthManager.updateDisplayName(displayName.trim())

                if (result is AuthResult.Success) {
                    // Also update in local database
                    val userId = currentUser?.uid
                    if (userId != null) {
                        val db = AppDatabase.getDatabase(context)
                        val userRepo = UserProfileRepository(db.userProfileDao())

                        val existingProfile = userRepo.getUserProfile(userId)
                        if (existingProfile != null) {
                            val updatedProfile = existingProfile.copy(
                                username = displayName.trim(),
                                lastUpdated = System.currentTimeMillis(),
                                isSynced = false
                            )
                            userRepo.saveUserProfile(updatedProfile)
                        }
                    }

                    successMessage = profileUpdatedMsg
                    Log.d("EditProfileScreen", "✅ Profile updated")

                    kotlinx.coroutines.delay(1500)
                    onSaveSuccess()
                } else if (result is AuthResult.Error) {
                    errorMessage = context.getString(R.string.failed_to_update_profile, result.message)
                    Log.e("EditProfileScreen", "❌ Update failed: ${result.message}")
                }
            } catch (e: Exception) {
                errorMessage = context.getString(R.string.failed_to_update_profile, e.message ?: "Unknown error")
                Log.e("EditProfileScreen", "❌ Error: ${e.message}", e)
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
                    text = stringResource(R.string.edit_profile_title),
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

                // Info Card
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
                            text = stringResource(R.string.your_profile),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email (Read-only)
                        Text(
                            text = stringResource(R.string.email_label),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = userEmail,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray.copy(alpha = 0.3f),
                                disabledContainerColor = Color.Gray.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display Name (Editable)
                        Text(
                            text = stringResource(R.string.display_name),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                displayNameError = ""
                                errorMessage = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.enter_display_name)) },
                            isError = displayNameError.isNotEmpty(),
                            supportingText = {
                                if (displayNameError.isNotEmpty()) {
                                    Text(
                                        text = displayNameError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { saveProfile() }
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

                // Save Button
                Button(
                    onClick = { saveProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && displayName != currentDisplayName,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
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
                            text = stringResource(R.string.save_changes),
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