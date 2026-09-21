package com.example.kerberos.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.kerberos.R
import com.example.kerberos.auth.AuthViewModel
import com.example.kerberos.data.Credential
import com.example.kerberos.data.VaultViewModel
import com.example.kerberos.security.BiometricAuthenticator
import com.example.kerberos.settings.SettingsManager
import com.example.kerberos.ui.theme.KerberosBlue
import com.example.kerberos.ui.theme.KerberosDanger
import com.example.kerberos.ui.theme.KerberosFieldBg
import com.example.kerberos.ui.theme.KerberosGrayText
import com.example.kerberos.ui.theme.KerberosLightBlueText

@Composable
fun VaultScreen(
    authViewModel: AuthViewModel,
    vaultViewModel: VaultViewModel,
    onAddCredential: () -> Unit,
    onOpenCredential: (Credential) -> Unit,
    onOpenSettings: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val credentials by vaultViewModel.credentials.collectAsState()
    val isVaultUnlocked by vaultViewModel.isVaultUnlocked.collectAsState()


    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val biometricAuthenticator = BiometricAuthenticator(context)
    val settingsManager = SettingsManager(context)

    //get sync state
    val lastSyncedText by vaultViewModel.lastSyncedText.collectAsState()

    /*
     * When the vault screen opens, ask for biometric authentication.
     *
     * We only do this when the vault is still locked.
     */
    LaunchedEffect(isVaultUnlocked) {
        if (
            !isVaultUnlocked &&
            activity != null &&
            settingsManager.isBiometricEnabled()
        ) {
            if (biometricAuthenticator.canAuthenticate()) {
                biometricAuthenticator.authenticate(
                    activity = activity,
                    onSuccess = {
                        vaultViewModel.unlockVault()
                    },
                    onError = {}
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        vaultViewModel.refreshLastSyncedTime()
    }

    if (!isVaultUnlocked) {
        BiometricLockScreen(
            onAuthenticate = {
                if (
                    activity != null &&
                    settingsManager.isBiometricEnabled()
                ) {
                    biometricAuthenticator.authenticate(
                        activity = activity,
                        onSuccess = {
                            vaultViewModel.unlockVault()
                        },
                        onError = {}
                    )
                }
            },
            onSignOut = onSignOut
        )

        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCredential,
                containerColor = KerberosBlue
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add),
                    tint = Color.White
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KerberosBlue)
                    .padding(20.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = painterResource(
                            id = R.drawable.ic_kerberos_logo
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        "Kerberos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.weight(1f))

                    Box {

                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = stringResource(R.string.notifications),
                            tint = Color.White
                        )

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.TopEnd)
                                .background(
                                    KerberosDanger,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "2",
                                color = Color.White,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings),
                        tint = Color.White,
                        modifier = Modifier.clickable {
                            onOpenSettings()
                        }
                    )

                    Spacer(Modifier.width(16.dp))

                    Icon(
                        Icons.Filled.Logout,
                        contentDescription = stringResource(R.string.sign_out),
                        tint = Color.White,
                        modifier = Modifier.clickable {
                            onSignOut()
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(R.string.good_morning),
                    color = KerberosLightBlueText,
                    fontSize = 13.sp
                )

                Text(
                    authViewModel.currentUserName(),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = {
                        Text(
                            stringResource(R.string.search_credentials),
                            color = KerberosLightBlueText
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = KerberosLightBlueText
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = KerberosFieldBg,
                        focusedContainerColor = KerberosFieldBg,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    "${credentials.size}",
                    stringResource(R.string.credentials),
                    Modifier.weight(1f)
                )

                StatCard(
                    "100%",
                    stringResource(R.string.secured),
                    Modifier.weight(1f)
                )

                StatCard(
                    lastSyncedText,
                    stringResource(R.string.last_sync),
                    Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.your_credentials),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    stringResource(R.string.sort),
                    color = KerberosBlue,
                    fontSize = 13.sp
                )
            }

            LazyColumn(
                modifier = Modifier.padding(16.dp)
            ) {
                items(credentials) { cred ->
                    CredentialRow(
                        cred,
                        onClick = {
                            onOpenCredential(cred)
                        }
                    )

                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun BiometricLockScreen(
    onAuthenticate: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Filled.Fingerprint,
            contentDescription = stringResource(R.string.biometric_authentication),
            modifier = Modifier.size(96.dp),
            tint = KerberosBlue
        )

        Spacer(Modifier.height(24.dp))

        Text(
            stringResource(R.string.vault_locked),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(R.string.authenticate_vault),
            color = KerberosGrayText,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onAuthenticate
        ) {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(stringResource(R.string.unlock_vault))
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onSignOut
        ) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(stringResource(R.string.sign_out))
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = KerberosBlue
            )

            Text(
                label,
                fontSize = 11.sp,
                color = KerberosGrayText
            )
        }
    }
}

@Composable
fun CredentialRow(
    credential: Credential,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        KerberosBlue,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    credential.serviceName
                        .take(1)
                        .uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    credential.serviceName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    credential.username,
                    color = KerberosGrayText,
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = KerberosGrayText
            )
        }
    }
}
