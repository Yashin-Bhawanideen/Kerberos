package com.example.kerberos.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kerberos.data.local.AppDatabase
import com.example.kerberos.data.local.SyncState
import com.example.kerberos.network.CreateCredentialRequest
import com.example.kerberos.network.RetrofitClient
import com.example.kerberos.notifications.KerberosNotificationManager
import com.example.kerberos.network.UpdateCredentialRequest
import android.util.Log

// Background worker (WorkManager) that pushes locally pending credential
// changes (creates/deletes) to the backend and reconciles local sync state
class SyncWorker (
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val logTag = "KerberosSync"

    override suspend fun doWork(): Result {

        Log.i(logTag, "Credential sync started")


        val dummyPass = "KerberosL0c4lke$".toByteArray()
        val dao = AppDatabase.getDatabase(applicationContext, dummyPass).credentialDao()
        val api = RetrofitClient.apiService

        // Nothing to do if there are no unsynced rows
        val pending = dao.getPendingSyncCredentials()
        if (pending.isEmpty()) {
            Log.i(logTag, "No credentials waiting to sync")
            return Result.success()
        }

        for (item in pending) {
            try {
                when (item.syncState) {

                    //pending create
                    // Push a locally-created credential to the backend
                    SyncState.PENDING_CREATE -> {
                        val request = CreateCredentialRequest(
                            id = item.id,
                            serviceName = item.serviceName,
                            username = item.username,
                            password = item.password,
                            websiteUrl = item.websiteUrl,
                            notes = item.notes
                        )

                        val response = api.addCredential(request)
                        if (response.isSuccessful) {
                            dao.updateSyncState(item.id, SyncState.SYNCED)
                            Log.i(logTag, "Credential creation synced successfully")
                        } else if (response.code() in 500..599) {
                            Log.e(logTag, "Credential sync failed with server error ${response.code()}")
                            KerberosNotificationManager(
                                applicationContext
                            ).showSyncFailure()

                            return Result.retry()
                        }
                    }

                    //sends locally edited credentials when connection is available (Android, 2026)
                    SyncState.PENDING_UPDATE -> {
                        val request = UpdateCredentialRequest(
                            serviceName = item.serviceName,
                            username = item.username,
                            password = item.password,
                            websiteUrl = item.websiteUrl,
                            notes = item.notes
                        )

                        val response = api.updateCredential(item.id, request)

                        if (response.isSuccessful) {
                            dao.updateSyncState(item.id, SyncState.SYNCED)
                            Log.i(logTag, "Credential update synced successfully")
                        } else if (response.code() in 500..599) {
                            Log.e(logTag, "Credential sync failed with server error ${response.code()}")
                            KerberosNotificationManager(
                                applicationContext
                            ).showSyncFailure()

                            return Result.retry()
                        }
                    }

                    // pending delete
                    SyncState.PENDING_DELETE -> {
                        val response = api.deleteCredential(item.id)
                        if (response.isSuccessful) {
                            dao.deletePermanently(item.id)
                            Log.i(logTag, "Credential deletion synced successfully")
                        } else if (response.code() in 500..599) {
                            Log.e(logTag, "Credential sync failed with server error ${response.code()}")
                            KerberosNotificationManager(
                                applicationContext
                            ).showSyncFailure()

                            return Result.retry()
                        }
                    }

                    else -> {}
                }

                // Covers network errors, timeouts, etc. — same retry/notify path
                // as a 5xx response
            } catch (e: Exception) {
                Log.e(logTag, "Credential sync failed: ${e.javaClass.simpleName}")

                KerberosNotificationManager(
                    applicationContext
                ).showSyncFailure()

                return Result.retry()
            }
        }

        //save sync timestamps
        val prefs = applicationContext.getSharedPreferences("kerberos_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_sync_time", System.currentTimeMillis()).apply()

        Log.i(logTag, "Credential sync completed successfully")

        return Result.success()

    }

}

//References
//Android, 2026. Task scheduling. [Online]
//Available at: https://developer.android.com/develop/background-work/background-tasks/persistent
//Developers, A., 2026. Transfer data using sync adapters. [Online]
//Available at: https://developer.android.com/training/sync-adapters
//Fered, M., 2021. android studio Disable Gradle 'offline mode' and sync project. [Online]
//Available at: https://stackoverflow.com/questions/68067241/android-studio-disable-gradle-offline-mode-and-sync-project
//Prasanna, 2018. enable gradle 'offline mode' and sync project is poping in android studio even though i unchecked the offline mode. [Online]
//Available at: https://stackoverflow.com/questions/52241579/enable-gradle-offline-mode-and-sync-project-is-poping-in-android-studio-even-t