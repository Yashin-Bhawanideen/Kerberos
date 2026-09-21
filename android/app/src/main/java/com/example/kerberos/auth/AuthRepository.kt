package com.example.kerberos.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(context: Context) {

    private val auth = FirebaseAuth.getInstance()

    // TODO: set this to your Web Client ID from the Firebase console
    // (Authentication -> Sign-in method -> Google -> Web SDK configuration)
    private val webClientId = "518336556444-rhvao5l5tpjcs45gvgncl78peucqj3rk.apps.googleusercontent.com"

    val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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