package com.example.kerberos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.example.kerberos.ui.KerberosNavGraph
import com.example.kerberos.ui.theme.KerberosTheme
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.kerberos.settings.SettingsManager

class MainActivity : AppCompatActivity() {
    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //loads the language that the user selected
        val settingsManager = SettingsManager(this)
        val savedLanguage = settingsManager.getLanguage()

        val languageCode = if (savedLanguage == "Afrikaans") {
            "af"
        } else {
            "en"
        }

        //applies the saved app language (Android Developers, 2026)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode)
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

/*
REFERENCE LIST

Android Developers. 2026. Per-app language preferences. [Online].
Available at: https://developer.android.com/guide/topics/resources/app-languages
[Accessed 22 September 2026].
*/