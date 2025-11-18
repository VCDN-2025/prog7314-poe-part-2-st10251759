package vcmsa.projects.prog7314.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

/*
     Code Attribution for: Firebase Authentication
    ===================================================
    Firebase, 2019b. Get Started with Firebase Authentication on Android | Firebase (Version unknown) [Source code].
    Available at: <https://firebase.google.com/docs/auth/android/start>
    [Accessed 18 November 2025].
*/


/**
 * Utility object for common Firebase operations.
 * Provides helper methods for authentication and initialization.
 * Implemented as a singleton object so it can be accessed from anywhere in the app.
 */
object FirebaseHelper {

    private const val TAG = "FirebaseHelper"

    /**
     * Initializes Firebase Authentication and logs the current state.
     * This should be called when the app starts to ensure Firebase is ready.
     * Logs whether a user is currently signed in or not.
     */
    fun initializeFirebase() {
        try {
            // Get the Firebase Auth instance to verify it's working
            val auth = FirebaseAuth.getInstance()
            Log.d(TAG, "Firebase Auth initialized successfully")

            // Log current user status for debugging
            Log.d(TAG, "Current user: ${auth.currentUser?.email ?: "No user signed in"}")
        } catch (e: Exception) {
            // Log any errors that occur during initialization
            Log.e(TAG, "Error initializing Firebase: ${e.message}")
        }
    }

    /**
     * Checks if a user is currently authenticated with Firebase.
     * Returns true if someone is logged in, false otherwise.
     * Useful for determining whether to show login screen or main content.
     */
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
}