package vcmsa.projects.prog7314.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

/*
     Code Attribution for: Firebase Authentication
    ===================================================
    Firebase, 2019b. Get Started with Firebase Authentication on Android | Firebase (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/auth/android/start>
    [Accessed 18 November 2025].
*/

object AuthManager {

    private const val TAG = "AuthManager"
    private val auth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null

    // Initialize Google Sign-In Client
    fun initializeGoogleSignIn(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("524919839541-eljmes26pu7edbs2s18r8270kfk7r6im.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
        Log.d(TAG, "Google Sign-In client initialized")
    }

    // Get Google Sign-In Intent
    fun getGoogleSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }

    // Sign in with Google Account
    suspend fun signInWithGoogle(account: GoogleSignInAccount): AuthResult {
        return try {
            Log.d(TAG, "Attempting to sign in with Google account: ${account.email}")

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user

            if (user != null) {
                Log.d(TAG, "Google sign-in successful for user: ${user.email}")
                AuthResult.Success(user)
            } else {
                Log.e(TAG, "Google sign-in failed: User is null")
                AuthResult.Error("Google sign-in failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in error: ${e.message}", e)
            AuthResult.Error(e.message ?: "Google sign-in failed")
        }
    }

    // Register with email and password
    suspend fun registerWithEmail(email: String, password: String): AuthResult {
        return try {
            Log.d(TAG, "Attempting to register user with email: $email")

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                Log.d(TAG, "Registration successful for user: ${user.email}")
                AuthResult.Success(user)
            } else {
                Log.e(TAG, "Registration failed: User is null")
                AuthResult.Error("Registration failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Registration error: ${e.message}", e)
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }

    // Login with email and password
    suspend fun loginWithEmail(email: String, password: String): AuthResult {
        return try {
            Log.d(TAG, "Attempting to login user with email: $email")

            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                Log.d(TAG, "Login successful for user: ${user.email}")
                AuthResult.Success(user)
            } else {
                Log.e(TAG, "Login failed: User is null")
                AuthResult.Error("Login failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            AuthResult.Error(e.message ?: "Login failed")
        }
    }

    // Sign out
    fun signOut() {
        try {
            Log.d(TAG, "Signing out user")
            auth.signOut()
            googleSignInClient?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error: ${e.message}", e)
        }
    }

    // Get current user
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        val user = getCurrentUser()
        return user != null
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            Log.d(TAG, "Sending password reset email to: $email")
            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent successfully")
            AuthResult.Success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset error: ${e.message}", e)
            AuthResult.Error(e.message ?: "Failed to send reset email")
        }
    }

    // ===== PROFILE & PASSWORD MANAGEMENT =====

    /**
     * Update user's display name
     */
    suspend fun updateDisplayName(displayName: String): AuthResult {
        return try {
            val user = auth.currentUser
            if (user == null) {
                Log.e(TAG, "Cannot update display name: No user logged in")
                return AuthResult.Error("No user logged in")
            }

            Log.d(TAG, "Updating display name to: $displayName")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()

            user.updateProfile(profileUpdates).await()

            Log.d(TAG, "Display name updated successfully")
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Display name update error: ${e.message}", e)
            AuthResult.Error(e.message ?: "Failed to update display name")
        }
    }

    /**
     * Reauthenticate user with email and password
     * Required before sensitive operations like password change
     */
    suspend fun reauthenticateUser(email: String, password: String): AuthResult {
        return try {
            val user = auth.currentUser
            if (user == null) {
                Log.e(TAG, "Cannot reauthenticate: No user logged in")
                return AuthResult.Error("No user logged in")
            }

            Log.d(TAG, "Reauthenticating user: $email")

            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()

            Log.d(TAG, "Reauthentication successful")
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Reauthentication error: ${e.message}", e)
            AuthResult.Error(e.message ?: "Reauthentication failed")
        }
    }

    /**
     * Change user's password
     * Note: User must be reauthenticated before calling this
     */
    suspend fun changePassword(newPassword: String): AuthResult {
        return try {
            val user = auth.currentUser
            if (user == null) {
                Log.e(TAG, "Cannot change password: No user logged in")
                return AuthResult.Error("No user logged in")
            }

            Log.d(TAG, "Changing password for user: ${user.email}")

            user.updatePassword(newPassword).await()

            Log.d(TAG, "Password changed successfully")
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Password change error: ${e.message}", e)
            AuthResult.Error(e.message ?: "Failed to change password")
        }
    }

    /**
     * Update user's email address (optional - use with caution)
     * Note: User must be reauthenticated before calling this
     */
    suspend fun updateEmail(newEmail: String): AuthResult {
        return try {
            val user = auth.currentUser
            if (user == null) {
                Log.e(TAG, "Cannot update email: No user logged in")
                return AuthResult.Error("No user logged in")
            }

            Log.d(TAG, "Updating email to: $newEmail")

            user.updateEmail(newEmail).await()

            Log.d(TAG, "Email updated successfully")
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Email update error: ${e.message}", e)
            AuthResult.Error(e.message ?: "Failed to update email")
        }
    }

    // ===== OFFLINE AUTO-LOGIN SUPPORT =====

    private const val PREFS_CREDENTIALS = "auth_credentials"
    private const val KEY_SAVED_EMAIL = "saved_email"
    private const val KEY_SAVED_USER_ID = "saved_user_id"
    private const val KEY_HAS_CREDENTIALS = "has_credentials"

    /**
     * Save credentials for biometric auto-login
     */
    fun saveBiometricCredentials(context: Context) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val prefs = context.getSharedPreferences(PREFS_CREDENTIALS, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString(KEY_SAVED_EMAIL, currentUser.email)
                putString(KEY_SAVED_USER_ID, currentUser.uid)
                putBoolean(KEY_HAS_CREDENTIALS, true)
                apply()
            }
            Log.d(TAG, "Biometric credentials saved for: ${currentUser.email}")
        }
    }

    /**
     * Check if user has saved credentials
     */
    fun hasSavedCredentials(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_CREDENTIALS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_HAS_CREDENTIALS, false)
    }

    /**
     * Get saved user email
     */
    fun getSavedEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_CREDENTIALS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_EMAIL, null)
    }

    /**
     * Get saved user ID
     */
    fun getSavedUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_CREDENTIALS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_USER_ID, null)
    }

    /**
     * Clear saved credentials
     */
    fun clearSavedCredentials(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_CREDENTIALS, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Log.d(TAG, "Saved credentials cleared")
    }

    /**
     * Perform offline auto-login (no Firebase call)
     */
    fun performOfflineLogin(context: Context): Boolean {
        return try {
            val savedUserId = getSavedUserId(context)
            val savedEmail = getSavedEmail(context)

            if (savedUserId != null && savedEmail != null) {
                Log.d(TAG, "Offline auto-login successful for: $savedEmail")
                true
            } else {
                Log.e(TAG, "No saved credentials found for offline login")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Offline login error: ${e.message}", e)
            false
        }
    }
}

// Sealed class to handle authentication results
sealed class AuthResult {
    data class Success(val user: FirebaseUser?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}