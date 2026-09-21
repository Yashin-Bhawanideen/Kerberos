package com.example.kerberos.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.kerberos.settings.SettingsManager
import androidx.compose.ui.res.stringResource
import com.example.kerberos.R
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat


@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onSignOut: () -> Unit
) {
    var biometricEnabled by remember {
        mutableStateOf(settingsManager.isBiometricEnabled())
    }

    var notificationsEnabled by remember {
        mutableStateOf(settingsManager.areNotificationsEnabled())
    }

    var selectedLanguage by remember {
        mutableStateOf(settingsManager.getLanguage())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium
        )

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
            language = "English",
            displayName = stringResource(R.string.english),
            selectedLanguage = selectedLanguage,
            onSelected = {
                selectedLanguage = it
                settingsManager.setLanguage(it)

                //changes app language to English (Android Developers, 2026)
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags("en")
                )
            }
        )

        LanguageOption(
            language = "Afrikaans",
            displayName = stringResource(R.string.afrikaans),
            selectedLanguage = selectedLanguage,
            onSelected = {
                selectedLanguage = it
                settingsManager.setLanguage(it)

                //changes app language to Afrikaans (Android Developers, 2026)
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags("af")
                )
            }
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
    language: String,
    displayName: String,
    selectedLanguage: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selectedLanguage == language,
                onClick = { onSelected(language) },
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedLanguage == language,
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