package com.example.kerberos.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kerberos.data.local.AppDatabase
import com.example.kerberos.data.local.SyncState
import com.example.kerberos.network.CreateCredentialRequest
import com.example.kerberos.network.RetrofitClient
import org.jetbrains.annotations.Async

class SyncWorker (
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        val dummyPass = "KerberosL0c4lke$".toByteArray()
        val dao = AppDatabase.getDatabase(applicationContext, dummyPass).credentialDao()
        val api = RetrofitClient.apiService

        val pending = dao.getPendingSyncCredentials()
        if (pending.isEmpty()) return Result.success()

        for (item in pending) {
            try {
                when (item.syncState) {

                    //pending create
                    SyncState.PENDING_CREATE -> {
                        val request = CreateCredentialRequest(
                            serviceName = item.serviceName,
                            username = item.username,
                            password = item.password,
                            websiteUrl = item.websiteUrl,
                            notes = item.notes
                        )

                        val response = api.addCredential(request)
                        if (response.isSuccessful) {
                            dao.updateSyncState(item.id, SyncState.SYNCED)
                        } else if (response.code() in 500..599) {
                            return Result.retry()
                        }
                    }

                    // pending delete
                    SyncState.PENDING_DELETE -> {
                        val response = api.deleteCredential(item.id)
                        if (response.isSuccessful) {
                            dao.deletePermanently(item.id)
                        } else if (response.code() in 500..599) {
                            return Result.retry()
                        }
                    }

                    else -> {}
                }

            } catch (e: Exception) {
                // if this, means no connection
                return Result.retry()
            }
        }

        return Result.success()

    }

}