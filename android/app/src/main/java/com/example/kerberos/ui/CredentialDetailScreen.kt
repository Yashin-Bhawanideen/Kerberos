package com.example.kerberos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kerberos.data.Credential
import com.example.kerberos.data.VaultViewModel
import com.example.kerberos.ui.theme.KerberosBlue
import com.example.kerberos.ui.theme.KerberosDanger
import com.example.kerberos.ui.theme.KerberosGrayText
import androidx.compose.ui.res.stringResource
import com.example.kerberos.R

@Composable
fun CredentialDetailScreen(
    credential: Credential,
    vaultViewModel: VaultViewModel,
    onBack: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("\u2190 ${stringResource(R.string.credential_details)}")
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { /* navigate to edit - not yet implemented */ }) {
                Text(
                    stringResource(R.string.edit),
                    color = KerberosBlue
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(KerberosBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(credential.serviceName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(credential.serviceName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(credential.websiteUrl, color = KerberosBlue, fontSize = 12.sp)
                    Text(
                        stringResource(R.string.modified_recently),
                        color = KerberosGrayText,
                        fontSize = 11.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        DetailField(
            label = stringResource(R.string.username),
            value = credential.username,
            trailing = {
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(credential.username))
                    }
                ) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = stringResource(R.string.copy_username)
                    )
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        DetailField(
            label = stringResource(R.string.password),
            value = if (passwordVisible) credential.password else "\u2022".repeat(credential.password.length.coerceAtMost(16)),
            trailing = {
                Row {
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
                    IconButton(onClick = { clipboard.setText(AnnotatedString(credential.password)) }) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = stringResource(R.string.copy_password)
                        )
                    }
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        DetailField(
            label = stringResource(R.string.website),
            value = credential.websiteUrl,
            trailing = {
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(credential.websiteUrl))
                    }
                ) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = stringResource(R.string.copy_website)
                    )
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        DetailField(
            label = stringResource(R.string.notes),
            value = credential.notes.ifBlank { "-" }
        )
        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = KerberosDanger)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, tint = KerberosDanger)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.delete_credential))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(stringResource(R.string.delete_credential_question))
            },
            text = {
                Text(stringResource(R.string.delete_warning))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vaultViewModel.deleteCredential(credential.id) { onBack() }
                        showDeleteConfirm = false
                    }
                ) {
                    Text(
                        stringResource(R.string.delete),
                        color = KerberosDanger
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun DetailField(label: String, value: String, trailing: (@Composable () -> Unit)? = null) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = KerberosGrayText)
                Text(value, fontSize = 15.sp)
            }
            trailing?.invoke()
        }
    }
}
