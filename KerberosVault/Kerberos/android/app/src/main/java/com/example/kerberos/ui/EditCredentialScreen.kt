package com.example.kerberos.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kerberos.R
import com.example.kerberos.data.Credential
import com.example.kerberos.data.VaultViewModel
import com.example.kerberos.ui.theme.KerberosBlue
import com.example.kerberos.ui.theme.KerberosGrayText

@Composable
fun EditCredentialScreen(
    credential: Credential,
    vaultViewModel: VaultViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    //keeps the current field values while the screen recomposes (Android Developers, 2026)
    var serviceName by remember { mutableStateOf(credential.serviceName) }
    var username by remember { mutableStateOf(credential.username) }
    var password by remember { mutableStateOf(credential.password) }
    var url by remember { mutableStateOf(credential.websiteUrl) }
    var notes by remember { mutableStateOf(credential.notes) }
    var passwordVisible by remember { mutableStateOf(false) }

    fun saveCredential() {
        val updatedCredential = credential.copy(
            serviceName = serviceName,
            username = username,
            password = password,
            websiteUrl = url,
            notes = notes,
            modifiedAt = System.currentTimeMillis()
        )

        vaultViewModel.updateCredential(updatedCredential) { success ->
            if (success) {
                onSaved()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("\u2190")
            }

            Text(
                stringResource(R.string.edit),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.weight(1f))

            TextButton(
                onClick = { saveCredential() },
                enabled = serviceName.isNotBlank() &&
                        username.isNotBlank() &&
                        password.isNotBlank()
            ) {
                Text(
                    stringResource(R.string.save),
                    color = KerberosBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        EditFieldLabel(stringResource(R.string.label_service_name))

        //editable text field using Compose state (Android Developers, 2026)
        OutlinedTextField(
            value = serviceName,
            onValueChange = { serviceName = it },
            leadingIcon = {
                Icon(Icons.Filled.Language, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        EditFieldLabel(stringResource(R.string.label_username_email))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            leadingIcon = {
                Icon(Icons.Filled.Person, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        EditFieldLabel(stringResource(R.string.label_password))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            leadingIcon = {
                Icon(Icons.Filled.Lock, contentDescription = null)
            },
            visualTransformation =
                if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            trailingIcon = {
                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Icon(
                        if (passwordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription =
                            if (passwordVisible) {
                                stringResource(R.string.cd_hide_password)
                            } else {
                                stringResource(R.string.cd_show_password)
                            }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        EditFieldLabel(stringResource(R.string.label_website_url))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            leadingIcon = {
                Icon(Icons.Filled.Language, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        EditFieldLabel(stringResource(R.string.label_notes))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { saveCredential() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KerberosBlue
            ),
            enabled = serviceName.isNotBlank() &&
                    username.isNotBlank() &&
                    password.isNotBlank()
        ) {
            Text(
                stringResource(R.string.save),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EditFieldLabel(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        color = KerberosGrayText,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(4.dp))
}

/*
REFERENCE LIST

Android Developers. 2026. State and Jetpack Compose. [Online].
Available at: https://developer.android.com/develop/ui/compose/state
[Accessed 22 September 2026].

Android Developers. 2026. Text fields in Compose. [Online].
Available at: https://developer.android.com/develop/ui/compose/text/user-input
[Accessed 22 September 2026].
*/