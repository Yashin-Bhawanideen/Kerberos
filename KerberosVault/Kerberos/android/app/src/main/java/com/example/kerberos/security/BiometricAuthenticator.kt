package com.example.kerberos.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// Wraps AndroidX BiometricPrompt to gate vault access behind
// fingerprint/face authentication
class BiometricAuthenticator(
    private val context: Context
) {

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(context)

        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    // Shows the system biometric prompt and reports the outcome via callbacks
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // BiometricPrompt callbacks are delivered on this executor;
        // main executor keeps UI updates safe
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                // Fired once on a successful biometric match
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                // Fired on unrecoverable errors (e.g. user cancels, too many
                // attempts, hardware unavailable) — the prompt closes itself
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }
        )

        // Configures the prompt's title/subtitle and cancel button;
        // no setDeviceCredentialAllowed()/allowedAuthenticators fallback,
        // so this only accepts biometrics, not PIN/pattern
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Kerberos")
            .setSubtitle("Authenticate to access your vault")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
//References
//Android, 2024. BiometricPrompt. [Online]
//Available at: https://developer.android.com/reference/androidx/biometric/BiometricPrompt
//Android, 2025. Show a biometric authentication dialog. [Online]
//Available at: https://developer.android.com/identity/sign-in/biometric-auth
//Android, 2026. Biometrics. [Online]
//Available at: https://source.android.com/docs/security/features/biometric