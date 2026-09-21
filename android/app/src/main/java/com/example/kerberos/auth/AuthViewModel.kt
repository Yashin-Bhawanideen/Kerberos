package com.example.kerberos.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object SignedOut : AuthState()
    object Loading : AuthState()
    object SignedIn : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(app.applicationContext)
    val googleSignInClient = repo.googleSignInClient

    private val _authState = MutableStateFlow<AuthState>(
        if (repo.currentUser() != null) AuthState.SignedIn else AuthState.SignedOut
    )
    val authState: StateFlow<AuthState> = _authState

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

    fun currentUserName() = repo.currentUser()?.displayName ?: "User"
}