package com.example.kerberos.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.kerberos.settings.SettingsManager
import androidx.compose.ui.res.stringResource
import com.example.kerberos.R
import com.example.kerberos.notifications.NotificationStore
import com.example.kerberos.notifications.NotificationType
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat


@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val notificationStore = remember { NotificationStore.get(context) }

    var biometricEnabled by remember {
        mutableStateOf(settingsManager.isBiometricEnabled())
    }

    var notificationsEnabled by remember {
        mutableStateOf(settingsManager.areNotificationsEnabled())
    }

    // Holds a language CODE ("en" / "af"), not a display name
    var selectedLanguage by remember {
        mutableStateOf(settingsManager.getLanguage())
    }

    // Shared by both language options. The notification is recorded BEFORE the
    // locale changes, because changing the locale recreates the activity.
    fun onLanguageSelected(code: String) {
        if (code != selectedLanguage) {
            notificationStore.add(NotificationType.LANGUAGE_CHANGED, code)
        }

        selectedLanguage = code
        settingsManager.setLanguage(code)

        //changes app language (Android Developers, 2026)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(code)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Blue rounded back button returns to the vault (home) screen, then the title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KerberosBackButton(onClick = onBack)

            Spacer(Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.biometric_authentication),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(R.string.biometric_description)
                )
            }

            Switch(
                checked = biometricEnabled,
                onCheckedChange = {
                    biometricEnabled = it
                    settingsManager.setBiometricEnabled(it)
                    notificationStore.add(
                        if (it) NotificationType.BIOMETRIC_ON
                        else NotificationType.BIOMETRIC_OFF
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.security_notifications),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(R.string.security_notifications_description)
                )
            }

            Switch(
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                    settingsManager.setNotificationsEnabled(it)
                    notificationStore.add(
                        if (it) NotificationType.SECURITY_ALERTS_ON
                        else NotificationType.SECURITY_ALERTS_OFF
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.titleMedium
        )

        LanguageOption(
            languageCode = "en",
            displayName = stringResource(R.string.english),
            selectedLanguage = selectedLanguage,
            onSelected = { code -> onLanguageSelected(code) }
        )

        LanguageOption(
            languageCode = "af",
            displayName = stringResource(R.string.afrikaans),
            selectedLanguage = selectedLanguage,
            onSelected = { code -> onLanguageSelected(code) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.sign_out))
        }
    }
}

@Composable
private fun LanguageOption(
    languageCode: String,
    displayName: String,
    selectedLanguage: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selectedLanguage == languageCode,
                onClick = { onSelected(languageCode) },
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedLanguage == languageCode,
            onClick = null
        )

        Text(
            text = displayName,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/*
REFERENCE LIST

Android Developers. 2026. Per-app language preferences. [Online].
Available at: https://developer.android.com/guide/topics/resources/app-languages
[Accessed 21 September 2026].
*/