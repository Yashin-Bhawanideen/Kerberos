package com.example.kerberos.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Handles authentication for the app via Google Sign-In + Firebase Auth.
 * Wraps the Google/Firebase SDK calls behind a small repository so the
 * rest of the app doesn't need to know about either SDK directly.
 */
class AuthRepository(context: Context) {

    // Singleton Firebase Auth instance used for sign-in state and credentials
    private val auth = FirebaseAuth.getInstance()


    // (Authentication -> Sign-in method -> Google -> Web SDK configuration)
    private val webClientId = "518336556444-rhvao5l5tpjcs45gvgncl78peucqj3rk.apps.googleusercontent.com"

    val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    /**
     * Exchanges a Google ID token (obtained from the Google Sign-In flow)
     * for a Firebase credential, then signs the user into Firebase with it.
     * Returns Result.success on success, or Result.failure wrapping
     * whatever exception occurred (network error, invalid token, etc.).
     */
    suspend fun firebaseAuthWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Returns the currently signed-in Firebase user, or null if none
    fun currentUser() = auth.currentUser

    // Signs out of Firebase, then revokes Google access entirely so the next
    // person on this device sees the full account picker/consent screen again
    // rather than a silently remembered account.
    suspend fun signOut(context: Context) {
        auth.signOut()
        try {
            googleSignInClient.revokeAccess().await()
        } catch (e: Exception) {
            // revokeAccess() can fail without network connectivity -- fall back
            // to a plain sign-out so the user isn't stuck unable to log out.
            googleSignInClient.signOut()
        }
    }
}

//References
//developers, A., 2026. About Sign in with Google. [Online]
//Available at: https://developer.android.com/identity/sign-in/credential-manager-siwg
//Developers, A., 2026. Show a biometric authentication dialog. [Online]
//Available at: https://developer.android.com/identity/sign-in/biometric-auth
//Erez, 2025. trying to implement google sign in from firebase authentication in android studio.. [Online]
//Available at: https://stackoverflow.com/questions/79606978/trying-to-implement-google-sign-in-from-firebase-authentication-in-android-studi
//Geek4geeks, 2025. Google Signing using Firebase Authentication in Android. [Online]
//Available at: https://www.geeksforgeeks.org/android/google-signing-using-firebase-authentication-in-android-using-java/
//walther, F., 2019. What is the USE_BIOMETRIC permission needed for?. [Online]
//Available at: https://stackoverflow.com/questions/59237106/what-is-the-use-biometric-permission-needed-for