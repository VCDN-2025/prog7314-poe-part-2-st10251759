package vcmsa.projects.prog7314.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object FirebaseHelper {

    private const val TAG = "FirebaseHelper"

    fun initializeFirebase() {
        try {
            val auth = FirebaseAuth.getInstance()
            Log.d(TAG, "Firebase Auth initialized successfully")
            Log.d(TAG, "Current user: ${auth.currentUser?.email ?: "No user signed in"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase: ${e.message}")
        }
    }

    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
}