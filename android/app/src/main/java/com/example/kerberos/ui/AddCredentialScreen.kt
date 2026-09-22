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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kerberos.data.VaultViewModel
import com.example.kerberos.ui.theme.KerberosBlue
import com.example.kerberos.ui.theme.KerberosGrayText
import kotlin.random.Random
import androidx.compose.ui.res.stringResource
import com.example.kerberos.R

@Composable
fun AddCredentialScreen(
    vaultViewModel: VaultViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var serviceName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("\u2190") }
            Text(
                stringResource(R.string.add_credential),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = {
                    vaultViewModel.addCredential(serviceName, username, password, url, notes) { success ->
                        if (success) onSaved()
                    }
                },
                enabled = serviceName.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            ) {
                Text(
                    stringResource(R.string.save),
                    color = KerberosBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        FieldLabel(stringResource(R.string.service_name))
        OutlinedTextField(
            value = serviceName, onValueChange = { serviceName = it },
            placeholder = { Text(stringResource(R.string.service_name_example)) },
            leadingIcon = { Icon(Icons.Filled.Language, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        FieldLabel(stringResource(R.string.username_email))
        OutlinedTextField(
            value = username, onValueChange = { username = it },
            placeholder = { Text(stringResource(R.string.email_example)) },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        FieldLabel(stringResource(R.string.password))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            placeholder = { Text(stringResource(R.string.password_placeholder)) },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) {
                            stringResource(R.string.hide_password)
                        } else {
                            stringResource(R.string.show_password)
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { password = generateStrongPassword() }) {
            Text(
                stringResource(R.string.generate_strong_password),
                color = KerberosBlue
            )
        }
        Spacer(Modifier.height(4.dp))

        FieldLabel(stringResource(R.string.website_url))
        OutlinedTextField(
            value = url, onValueChange = { url = it },
            placeholder = { Text(stringResource(R.string.website_example)) },
            leadingIcon = { Icon(Icons.Filled.Language, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        FieldLabel(stringResource(R.string.notes))
        OutlinedTextField(
            value = notes, onValueChange = { notes = it },
            placeholder = { Text(stringResource(R.string.notes_placeholder)) },
            modifier = Modifier.fillMaxWidth().height(90.dp)
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                vaultViewModel.addCredential(serviceName, username, password, url, notes) { success ->
                    if (success) onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KerberosBlue),
            enabled = serviceName.isNotBlank() && username.isNotBlank() && password.isNotBlank()
        ) {
            Text(
                stringResource(R.string.add_credential),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontSize = 11.sp, color = KerberosGrayText, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
}

fun generateStrongPassword(length: Int = 16): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
    return (1..length).map { chars[Random.nextInt(chars.length)] }.joinToString("")
}
