package com.example.kerberos.data

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VaultViewModel(app: Application) : AndroidViewModel(app) {

    //ini passphrase for room
    private val dummyPass = "KerberosL0c4lke$".toByteArray()
    private val repo = CredentialRepository(app.applicationContext, dummyPass)

    //bound flow to local room db
    val credentials: StateFlow<List<Credential>> = repo.credentialFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked

    var selected: Credential? = null

    fun unlockVault() {
        _isVaultUnlocked.value = true

        //try fetch from api
        viewModelScope.launch {
            repo.refreshRemoteCredentials()
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
        selected = null
    }

    fun addCredential(
        serviceName: String,
        username: String,
        password: String,
        url: String,
        notes: String,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val cred = Credential(
                serviceName = serviceName,
                username = username,
                password = password,
                websiteUrl = url,
                notes = notes
            )
            val result = repo.addCredential(cred)
            onDone(result.isSuccess)
        }
    }

    fun deleteCredential (id: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repo.deleteCredential(id)
            onDone(result.isSuccess)
        }
    }
}