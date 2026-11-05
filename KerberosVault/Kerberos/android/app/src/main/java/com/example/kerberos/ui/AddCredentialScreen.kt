package com.example.kerberos.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kerberos.R
import com.example.kerberos.data.VaultViewModel
import com.example.kerberos.notifications.NotificationStore
import com.example.kerberos.notifications.NotificationType
import com.example.kerberos.ui.theme.KerberosBlue
import com.example.kerberos.ui.theme.KerberosDanger
import com.example.kerberos.ui.theme.KerberosGrayText
import kotlin.random.Random

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

    val context = LocalContext.current
    val notificationStore = remember { NotificationStore.get(context) }

    // Used by both the top "Save" button and the bottom "Add Credential" button.
    // The service name is copied first because the save callback runs later.
    val saveCredential: () -> Unit = {
        val savedServiceName = serviceName
        vaultViewModel.addCredential(serviceName, username, password, url, notes) { success ->
            if (success) {
                notificationStore.add(NotificationType.CREDENTIAL_ADDED, savedServiceName)
                onSaved()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue rounded back button shared across screens
            KerberosBackButton(onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.add_credential), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = saveCredential,
                enabled = serviceName.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            ) {
                Text(stringResource(R.string.save), color = KerberosBlue, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))

        // Required field: red * after the label
        FieldLabel(stringResource(R.string.label_service_name), required = true)
        OutlinedTextField(
            value = serviceName, onValueChange = { serviceName = it },
            placeholder = { Text(stringResource(R.string.hint_service_name)) },
            leadingIcon = { Icon(Icons.Filled.Language, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Required field
        FieldLabel(stringResource(R.string.label_username_email), required = true)
        OutlinedTextField(
            value = username, onValueChange = { username = it },
            placeholder = { Text(stringResource(R.string.hint_email)) },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Required field
        FieldLabel(stringResource(R.string.label_password), required = true)
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            placeholder = { Text(stringResource(R.string.hint_password)) },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible)
                            stringResource(R.string.cd_hide_password)
                        else
                            stringResource(R.string.cd_show_password)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Styled as a blue rounded button, matching the back button
        Button(
            onClick = { password = generateStrongPassword() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KerberosBlue,
                contentColor = Color.White
            )
        ) {
            Text(stringResource(R.string.generate_password), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))

        // Optional field: gray "(optional)" after the label
        FieldLabel(stringResource(R.string.label_website_url), optional = true)
        OutlinedTextField(
            value = url, onValueChange = { url = it },
            placeholder = { Text(stringResource(R.string.hint_website_url)) },
            leadingIcon = { Icon(Icons.Filled.Language, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Optional field
        FieldLabel(stringResource(R.string.label_notes), optional = true)
        OutlinedTextField(
            value = notes, onValueChange = { notes = it },
            placeholder = { Text(stringResource(R.string.hint_notes)) },
            modifier = Modifier.fillMaxWidth().height(90.dp)
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = saveCredential,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KerberosBlue),
            enabled = serviceName.isNotBlank() && username.isNotBlank() && password.isNotBlank()
        ) {
            Text(stringResource(R.string.add_credential), fontWeight = FontWeight.Bold)
        }
    }
}

// Field label with an optional red * (required) or gray "(optional)" marker
@Composable
private fun FieldLabel(
    text: String,
    required: Boolean = false,
    optional: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, fontSize = 11.sp, color = KerberosGrayText, fontWeight = FontWeight.Bold)

        if (required) {
            Text(
                " *",
                fontSize = 13.sp,
                color = KerberosDanger,
                fontWeight = FontWeight.Bold
            )
        }

        if (optional) {
            Text(
                " " + stringResource(R.string.optional_label),
                fontSize = 11.sp,
                color = KerberosGrayText
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}

fun generateStrongPassword(length: Int = 16): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
    return (1..length).map { chars[Random.nextInt(chars.length)] }.joinToString("")
}