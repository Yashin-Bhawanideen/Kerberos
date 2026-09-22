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

//utility last sync calc
import android.content.Context
import android.text.format.DateUtils

class VaultViewModel(app: Application) : AndroidViewModel(app) {

    //ini passphrase for room
    private val dummyPass = "KerberosL0c4lke$".toByteArray()
    private val repo = CredentialRepository(app.applicationContext, dummyPass)

    //bound flow to local room db
    // Converts the repository's cold Flow into a hot StateFlow scoped to
    // this ViewModel, so the UI always has a current value to read
    val credentials: StateFlow<List<Credential>> = repo.credentialFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    // Tracks whether the vault has been unlocked (e.g. after biometric auth)
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked

    // Currently selected credential, e.g. for a detail/edit screen
    var selected: Credential? = null

    fun unlockVault() {
        _isVaultUnlocked.value = true

        //try fetch from api
        viewModelScope.launch {
            repo.refreshRemoteCredentials()
            refreshLastSyncedTime() // get last sync times
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

    fun updateCredential(
        credential: Credential,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = repo.updateCredential(credential)

            if (result.isSuccess) {
                selected = credential
            }

            onDone(result.isSuccess)
        }
    }

    fun deleteCredential (id: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repo.deleteCredential(id)
            onDone(result.isSuccess)
        }
    }

    //utility for time calculations (ill think about giving this its own file tho but not now)
    private val _lastSyncedText = MutableStateFlow("Never")
    val lastSyncedText: StateFlow<String> = _lastSyncedText

    fun refreshLastSyncedTime() {
        val prefs = getApplication<Application>().getSharedPreferences("kerberos_prefs", Context.MODE_PRIVATE)
        val lastSyncTime = prefs.getLong("last_sync_time", 0L)

        if (lastSyncTime == 0L) {
            _lastSyncedText.value = "Never"
        } else {
            _lastSyncedText.value = DateUtils.getRelativeTimeSpanString(
                lastSyncTime,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()
        }
    }
}