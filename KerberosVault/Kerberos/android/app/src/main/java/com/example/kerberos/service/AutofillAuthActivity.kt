package com.example.kerberos.service

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.autofill.AutofillValue
import androidx.fragment.app.FragmentActivity
import com.example.kerberos.security.BiometricAuthenticator
import android.view.autofill.AutofillManager

class AutofillAuthActivity : FragmentActivity(){

    override fun onCreate(savedIntanceState: Bundle?) {
        super.onCreate(savedIntanceState)

        // Credential values passed in by the autofill service for this
        // specific field the user is filling
        val username = intent.getStringExtra("EXTRA_USERNAME") ?: ""
        val password = intent.getStringExtra("EXTRA_PASSWORD") ?: ""

        val authenticator = BiometricAuthenticator(this)

        if (authenticator.canAuthenticate()) {
            authenticator.authenticate(
                activity = this,
                onSuccess = {
                    val dataset = Dataset.Builder().apply {
                        //give dataset back to caller
                    }.build()

                    // Returns the completed dataset to the autofill framework,
                    // which fills it into the original app's login form
                    val replyIntent = Intent().apply {
                        putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
                    }
                    setResult(Activity.RESULT_OK, replyIntent)
                    finish()
                },
                onError = {
                    // Auth failed/cancelled: no data is released
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        } else {
            //for if no biometrics enabled
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}