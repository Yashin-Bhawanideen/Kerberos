package com.example.kerberos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.FragmentActivity
import com.example.kerberos.settings.SettingsManager
import com.example.kerberos.ui.KerberosNavGraph
import com.example.kerberos.ui.theme.KerberosTheme

class MainActivity : AppCompatActivity() {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsManager = SettingsManager(this)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(settingsManager.getLanguage())
        )
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        setContent {
            KerberosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    KerberosNavGraph()
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }

        }
    }
}
//References
//Android, 2021. AppCompatDelegate. [Online]
//Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate
//Android, 2025. Request runtime permissions. [Online]
//Available at: https://developer.android.com/training/permissions/requesting
//Android, 2026. Per-app language preferences. [Online]
//Available at: https://developer.android.com/guide/topics/resources/app-languages