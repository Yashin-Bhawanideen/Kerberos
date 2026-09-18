package com.example.kerberos.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VaultViewModel : ViewModel() {

    private val repo = CredentialRepository()

    private val _credentials = MutableStateFlow<List<Credential>>(emptyList())
    val credentials: StateFlow<List<Credential>> = _credentials

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked

    var selected: Credential? = null

    fun unlockVault() {
        _isVaultUnlocked.value = true
        loadCredentials()
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
        selected = null
        _credentials.value = emptyList()
    }

    fun loadCredentials() {
        viewModelScope.launch {
            _isLoading.value = true

            repo.getCredentials()
                .onSuccess {
                    _credentials.value = it
                }

            _isLoading.value = false
        }
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

            if (result.isSuccess) {
                loadCredentials()
            }

            onDone(result.isSuccess)
        }
    }

    fun deleteCredential(
        id: String,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = repo.deleteCredential(id)

            if (result.isSuccess) {
                loadCredentials()
            }

            onDone(result.isSuccess)
        }
    }
}