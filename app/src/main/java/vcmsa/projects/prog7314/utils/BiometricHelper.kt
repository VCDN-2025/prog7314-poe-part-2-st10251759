package vcmsa.projects.prog7314.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/*
     Code Attribution for: Biometric Authentication
    ===================================================
    Android Developers, 2024. Login with Biometrics on Android (Version unknown) [Source code].
    Available at: <https://developer.android.com/codelabs/biometric-login#0>
    [Accessed 18 November 2025].
*/


object BiometricHelper {
    private const val TAG = "BiometricHelper"
    private const val PREFS_NAME = "biometric_prefs"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

    /**
     * Check if biometric authentication is available on this device
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Biometric authentication is available")
                true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d(TAG, "No biometric hardware available")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d(TAG, "Biometric hardware is currently unavailable")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d(TAG, "No biometric credentials enrolled")
                false
            }
            else -> {
                Log.d(TAG, "Biometric authentication not available")
                false
            }
        }
    }

    /**
     * Show biometric authentication prompt
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Authentication error: $errString")
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Authentication succeeded")
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.e(TAG, "Authentication failed")
                    onFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Check if user has enabled biometric login
     */
    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /**
     * Set biometric login preference
     */
    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val prefs = getPrefs(context)
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        Log.d(TAG, "Biometric enabled set to: $enabled")
    }

    /**
     * Get shared preferences
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Show biometric prompt for auto-login
     */
    fun showBiometricLoginPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable(activity)) {
            onError("Biometric authentication not available")
            return
        }

        if (!isBiometricEnabled(activity)) {
            onError("Biometric login not enabled")
            return
        }

        showBiometricPrompt(
            activity = activity,
            title = "Login with Fingerprint",
            subtitle = "Touch the fingerprint sensor to login",
            negativeButtonText = "Use Password",
            onSuccess = {
                Log.d(TAG, "Biometric login successful")
                onSuccess()
            },
            onError = { error ->
                Log.e(TAG, "Biometric login error: $error")
                onError(error)
            },
            onFailed = {
                Log.e(TAG, "Biometric login failed")
                onError("Authentication failed")
            }
        )
    }
}