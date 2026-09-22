package com.example.kerberos.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.autofill.AutofillManager
import android.R.attr.data
import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.IntentSender
import android.os.CancellationSignal
import android.service.autofill.*
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.example.kerberos.R
import com.example.kerberos.data.local.AppDatabase
import com.example.kerberos.data.local.CredentialEntity
import com.example.kerberos.data.local.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.jvm.java
import androidx.room.Room.databaseBuilder


object AutofillUtils {
    fun isAutofillServiceEnabled(context: Context): Boolean {
        val autoFillManager = context.getSystemService(AutofillManager::class.java)
        return autoFillManager.hasEnabledAutofillServices() == true
    }

    fun openAutofillSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }

}


class KerberosAutofillService: AutofillService(){

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        //get structure from request
        val fillContexts: List<FillContext> = request.fillContexts
        val lastStructure: AssistStructure = fillContexts.last().structure

        //parse fields
        val parsedFields = ParsedFields()
        parseNode(lastStructure.getWindowNodeAt(0).rootViewNode, parsedFields)

        //exit if no fields found to fill
        if (parsedFields.usernameId == null && parsedFields.passwordId == null) {
            callback.onSuccess(null)
            return
        }

        serviceScope.launch {
            val dummyPass = "KerberosL0c4lke$".toByteArray()
            val dao = AppDatabase.getDatabase(applicationContext, dummyPass).credentialDao()
            val credentials = dao.getAllCredentials().first()

            if (credentials.isEmpty()){
                callback.onSuccess(null)
                return@launch
            }

            val responseBuilder = FillResponse.Builder()

            //get correct web credentials
            val matchedCredentials = if (parsedFields.webDomain.isNotBlank()) {
                credentials.filter {
                    it.websiteUrl.contains(parsedFields.webDomain, ignoreCase = true) ||
                            it.serviceName.contains(parsedFields.webDomain, ignoreCase = true)
                }.ifEmpty { credentials }
            } else {
                credentials
            }

            //fillin from matchedCredentials
            for (entity in matchedCredentials) {
                val datasetBuilder = Dataset.Builder()

                //view popup - client
                val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
                    setTextViewText(
                        android.R.id.text1,
                        "Kerberos: ${entity.serviceName} (${entity.username})"
                    )
                }

                var hasValue = false

                parsedFields.usernameId?.let { id ->
                    datasetBuilder.setValue(id, AutofillValue.forText(entity.username), presentation)
                    hasValue = true
                }

                parsedFields.passwordId?.let { id ->
                    datasetBuilder.setValue(id, AutofillValue.forText(entity.password), presentation)
                    hasValue = true
                }

                if (hasValue) {
                    //auth
                    val authIntent = Intent(applicationContext, AutofillAuthActivity::class.java).apply {
                        putExtra("EXTRA_USERNAME", entity.username)
                        putExtra("EXTRA_PASSWORD", entity.password)
                    }

                    val pendingIntent = PendingIntent.getActivity(
                        applicationContext,
                        entity.id.hashCode(),
                        authIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
                    )

                    datasetBuilder.setAuthentication(pendingIntent.intentSender)
                    responseBuilder.addDataset(datasetBuilder.build())

                }
            }

            //define fields for save request
            val saveIds = mutableListOf<AutofillId>()
            parsedFields.usernameId?.let {saveIds.add(it) }
            parsedFields.passwordId?.let {saveIds.add(it)}

            if (saveIds.isNotEmpty()) {
                val saveInfo = SaveInfo.Builder (
                    SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                    saveIds.toTypedArray()
                ).build()
                responseBuilder.setSaveInfo(saveInfo)
            }

            callback.onSuccess(responseBuilder.build())
        }
    }

    // Called by the OS after the user submits a form the service flagged
    // via SaveInfo, so new/changed credentials can be captured
    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val fillContexts = request.fillContexts
        val latestStructure = fillContexts.last().structure

        val parsedFields = ParsedFields()
        parseNode(latestStructure.getWindowNodeAt(0).rootViewNode, parsedFields)

        val username = parsedFields.extractedUsername
        val password = parsedFields.extractedPassword

        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            serviceScope.launch {
                val dummyPass = "KerberosL0c4lke$".toByteArray()
                val dao = AppDatabase.getDatabase(applicationContext, dummyPass).credentialDao()

                val serviceName = parsedFields.webDomain.ifBlank { "Autofilled Service" }
                val newEntity = CredentialEntity(
                    id = UUID.randomUUID().toString(),
                    serviceName = serviceName,
                    username = username,
                    password = password,
                    websiteUrl = parsedFields.webDomain,
                    notes = "Saved via Kerberos Autofill",
                    createdAt = System.currentTimeMillis(),
                    modifiedAt = System.currentTimeMillis(),
                    syncState = SyncState.PENDING_CREATE
                )

                dao.insertOrUpdate(newEntity)
                callback.onSuccess()

            }
        } else {
            callback.onSuccess()
        }
    }

    // Recursively walks the view tree looking for username/password fields,
    // first by official autofill hints, then falling back to guessing from
    // the view's id name (e.g. "editTextPassword")
    private fun parseNode(node: AssistStructure.ViewNode, parsedFields: ParsedFields){
        if (node.webDomain != null) {
            parsedFields.webDomain = node.webDomain ?: ""
        }

        val hints = node.autofillHints
        if (hints != null) {
            for (hint in hints) {
                when (hint) {
                    android.view.View.AUTOFILL_HINT_USERNAME,
                    android.view.View.AUTOFILL_HINT_EMAIL_ADDRESS -> {
                        if (parsedFields.usernameId == null) {
                            parsedFields.usernameId = node.autofillId
                            parsedFields.extractedUsername = node.autofillValue?.textValue?.toString()
                        }
                    }

                    android.view.View.AUTOFILL_HINT_PASSWORD -> {
                        if (parsedFields.passwordId == null) {
                            parsedFields.passwordId = node.autofillId
                            parsedFields.extractedPassword = node.autofillValue?.textValue?.toString()
                        }
                    }

                }

            }
        }

        //fallback
        // Apps that don't set proper autofill hints are matched by
        // guessing from their view id name instead

        val viewId = node.idEntry?.lowercase() ?: ""
        if (parsedFields.usernameId == null && (viewId.contains("user") ||
                    viewId.contains("email") || viewId.contains("login"))) {

            parsedFields.usernameId = node.autofillId
            parsedFields.extractedUsername = node.autofillValue?.textValue.toString()

        }

        if (parsedFields.passwordId == null && (viewId.contains("pass") || viewId.contains("pwd"))) {
            parsedFields.passwordId = node.autofillId
            parsedFields.extractedPassword = node.autofillValue?.textValue?.toString()
        }

        for (i in 0 until node.childCount) {
            parseNode(node.getChildAt(i), parsedFields)
        }
    }

    // Accumulator used while walking the view tree: holds whatever
    // username/password fields (and values) have been found so far
    private data class ParsedFields(
        var usernameId: AutofillId? = null,
        var passwordId: AutofillId? = null,
        var extractedUsername: String? = null,
        var extractedPassword: String? = null,
        var webDomain: String = ""
    )

}

//References
//Android, 2024. BiometricPrompt. [Online]
//Available at: https://developer.android.com/reference/androidx/biometric/BiometricPrompt
//Android, 2025. Show a biometric authentication dialog. [Online]
//Available at: https://developer.android.com/identity/sign-in/biometric-auth
//Android, 2026. AssistStructure. [Online]
//Available at: https://developer.android.com/reference/android/app/assist/AssistStructure
//Android, 2026. Biometrics. [Online]
//Available at: https://source.android.com/docs/security/features/biometric
//Android, 2026. Build autofill services. [Online]
//Available at: https://developer.android.com/identity/autofill/autofill-services
//Developers, A., 2021. AutofillService. [Online]
//Available at: https://developer.android.com/reference/android/service/autofill/AutofillService
