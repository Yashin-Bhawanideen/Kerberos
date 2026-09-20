package com.example.kerberos.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.autofill.AutofillManager
import android.R.attr.data

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


public abstract class KerberosAutofillService {
}