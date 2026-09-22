package com.example.kerberos.data

import android.content.Context
import androidx.work.*
import com.example.kerberos.data.local.AppDatabase
import com.example.kerberos.data.local.CredentialEntity
import com.example.kerberos.data.local.SyncState
import com.example.kerberos.data.sync.SyncWorker
import com.example.kerberos.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.example.kerberos.network.CredentialDto
import com.example.kerberos.network.CreateCredentialRequest
import android.util.Log.e

// Single source of truth for credential data. Mediates between the local
// encrypted Room database, the remote API, and the sync worker that keeps
// them consistent. UI/ViewModels should go through this, not the DAO directly.
class CredentialRepository (context: Context, passphrase: ByteArray){

    //database
    private val dao = AppDatabase.getDatabase(context, passphrase).credentialDao()

    //workmanager class
    private val workManager = WorkManager.getInstance(context)

    //api
    private val api = RetrofitClient.apiService

    //source of truth
    // Exposes local DB rows as domain-layer Credential objects (stripping
    // out syncState, which is an internal persistence detail)
    val credentialFlow: Flow<List<Credential>> = dao.getAllCredentials().map {list ->
        list.map { entity ->
            Credential(
                id = entity.id,
                serviceName = entity.serviceName,
                username = entity.username,
                password = entity.password,
                websiteUrl = entity.websiteUrl,
                notes = entity.notes,
                createdAt = entity.createdAt,
                modifiedAt = entity.modifiedAt
            )
        }
    }

    // Saves a credential locally (generating an ID if new), marks it as
    // needing a push to the backend, and triggers a sync attempt
    suspend fun addCredential(credential: Credential): Result<Unit> {

        val newId = if (credential.id.isBlank()) UUID.randomUUID().toString() else credential.id
        val entity = CredentialEntity(
            id = newId,
            serviceName = credential.serviceName,
            username = credential.username,
            password = credential.password,
            websiteUrl = credential.websiteUrl,
            notes = credential.notes,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            syncState = SyncState.PENDING_CREATE
        )

        //write to room db
        dao.insertOrUpdate(entity)

        //schedule manager syncs here
        scheduleSync()
        return Result.success(Unit)
    }

    suspend fun deleteCredential(id: String): Result<Unit>{
        //mark as local pending delete so that the ui does not show deleted records
        dao.updateSyncState(id, SyncState.PENDING_DELETE)
        scheduleSync()

        return Result.success(Unit)
    }

    // fetches latest from api if online, then merge to local db
    suspend fun refreshRemoteCredentials(): Result<Unit> {
        return try {
            val response = api.getCredentials()
            if (response.isSuccessful) {
                response.body()?.forEach { dto ->
                    dao.insertOrUpdate(
                        CredentialEntity(
                            id = dto.id,
                            serviceName = dto.serviceName,
                            username = dto.username,
                            password = dto.password,
                            websiteUrl = dto.websiteUrl,
                            notes = dto.notes,
                            createdAt = dto.createdAt,
                            modifiedAt = dto.modifiedAt,
                            syncState = SyncState.SYNCED
                        )
                    )
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("API Error ${response.code()}"))
            }
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "KerberosSyncWork",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
//References
//Developer, A., 2024. Define work requests. [Online]
//Available at: https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work
//Developer, A., 2025. Data layer. [Online]
//Available at: https://developer.android.com/topic/architecture/data-layer
//Kotlin, 2026. Kotlin libraries. [Online]
//Available at: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-result/