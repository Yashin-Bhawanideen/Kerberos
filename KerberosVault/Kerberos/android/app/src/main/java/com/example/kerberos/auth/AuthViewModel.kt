package com.example.kerberos.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Represents the possible states of the sign-in flow, exposed to the UI
sealed class AuthState {
    object SignedOut : AuthState()
    object Loading : AuthState()
    object SignedIn : AuthState()
    data class Error(val message: String) : AuthState()
}

// ViewModel backing the auth/login screen; survives configuration changes
// and exposes auth state as a StateFlow for the UI to collect
class AuthViewModel(app: Application) : AndroidViewModel(app) {

    // Repository doing the actual Firebase/Google Sign-In work
    private val repo = AuthRepository(app.applicationContext)

    // Exposed so the login screen can launch the Google Sign-In intent
    val googleSignInClient = repo.googleSignInClient

    // Backing mutable state; initial value reflects whether a user is
    // already signed in when the ViewModel is created
    private val _authState = MutableStateFlow<AuthState>(
        if (repo.currentUser() != null) AuthState.SignedIn else AuthState.SignedOut
    )
    val authState: StateFlow<AuthState> = _authState

    // Called once the login screen has a Google ID token; exchanges it for
    // a Firebase session and updates state accordingly
    fun onGoogleIdTokenReceived(idToken: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            repo.firebaseAuthWithGoogle(idToken)
                .onSuccess {
                    _authState.value = AuthState.SignedIn
                }
                .onFailure {
                    _authState.value =
                        AuthState.Error(it.message ?: "Sign-in failed")
                }
        }
    }

    /** Lets the login screen exercise its error UI without a real failed sign-in. */
    fun simulateSignInError() {
        _authState.value = AuthState.Error("Simulated sign-in error")
    }

    fun signOut() {
        viewModelScope.launch {
            repo.signOut(getApplication())
            _authState.value = AuthState.SignedOut
        }
    }

    // Convenience accessor for displaying the signed-in user's name
    fun currentUserName() = repo.currentUser()?.displayName ?: "User"
}

//References
//Developers, a., 2025. StateFlow and SharedFlow. [Online]
//Available at: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
//Developers, a., 2025. ViewModel overview. [Online]
//Available at: https://developer.android.com/topic/libraries/architecture/viewmodel
//Devtheory, 2021. Observe StateFlow emission in ViewModel initialization. [Online]
//Available at: https://stackoverflow.com/questions/69213929/observe-stateflow-emission-in-viewmodel-initialization
//Github, K., 2026. Flows. [Online]
//Available at: https://kotlinlang.org/docs/coroutines-flow.html
//Team, G. D., 2023. ViewModel and State in Compose. [Online]
//Available at: https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state#0