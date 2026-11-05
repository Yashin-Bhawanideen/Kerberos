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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.kerberos.notifications.NotificationStore
import com.example.kerberos.security.BiometricAuthenticator
import com.example.kerberos.settings.SettingsManager
import com.example.kerberos.ui.theme.KerberosBlue
import com.example.kerberos.ui.theme.KerberosDanger
import com.example.kerberos.ui.theme.KerberosFieldBg
import com.example.kerberos.ui.theme.KerberosGrayText
import com.example.kerberos.ui.theme.KerberosLightBlueText

// Sort choices for the credential list
private const val SORT_DEFAULT = 0        // original order, nothing ticked
private const val SORT_ALPHABETICAL = 1   // A to Z
private const val SORT_NOT_ALPHABETICAL = 2 // Z to A

// Main vault screen: shows the biometric lock gate first, then (once
// unlocked) the header, stats, and the scrollable list of saved credentials
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
    // Biometric prompt needs a FragmentActivity host; null-safe cast so
    // this composable doesn't crash if hosted elsewhere
    val activity = context as? FragmentActivity

    val biometricAuthenticator = BiometricAuthenticator(context)
    val settingsManager = SettingsManager(context)

    val lastSyncedText by vaultViewModel.lastSyncedText.collectAsState()

    // Notifications: saved list, unread count for the badge, and dialog visibility
    val notificationStore = remember { NotificationStore.get(context) }
    val notifications by notificationStore.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }
    var showNotifications by remember { mutableStateOf(false) }

    // Chosen sort option. rememberSaveable keeps it when the language change
    // recreates the activity.
    var sortOption by rememberSaveable { mutableIntStateOf(SORT_DEFAULT) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Text typed into the search bar. Empty means "show everything".
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Search first (by service name only), then sort. Re-runs only when the
    // credentials, the search text or the sort option change.
    val visibleCredentials = remember(credentials, searchQuery, sortOption) {
        val query = searchQuery.trim()

        val filtered = if (query.isEmpty()) {
            credentials
        } else {
            credentials.filter {
                it.serviceName.contains(query, ignoreCase = true)
            }
        }

        when (sortOption) {
            SORT_ALPHABETICAL -> filtered.sortedBy { it.serviceName.lowercase() }
            SORT_NOT_ALPHABETICAL -> filtered.sortedByDescending { it.serviceName.lowercase() }
            else -> filtered
        }
    }

    // Automatically prompts for biometric auth as soon as the screen
    //checks biometric setting before locking the vault (Android Developers, 2025)
    LaunchedEffect(isVaultUnlocked) {
        if (!isVaultUnlocked) {
            if (!settingsManager.isBiometricEnabled()) {
                vaultViewModel.unlockVault()
            } else if (activity != null && biometricAuthenticator.canAuthenticate()) {
                biometricAuthenticator.authenticate(
                    activity = activity,
                    onSuccess = { vaultViewModel.unlockVault() },
                    onError = {}
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        vaultViewModel.refreshLastSyncedTime()
    }

    // Early return: show the lock screen instead of vault contents while locked
    if (!isVaultUnlocked) {
        BiometricLockScreen(
            onAuthenticate = {
                if (activity != null && settingsManager.isBiometricEnabled()) {
                    biometricAuthenticator.authenticate(
                        activity = activity,
                        onSuccess = { vaultViewModel.unlockVault() },
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
                    contentDescription = stringResource(R.string.cd_add),
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
                        painter = painterResource(id = R.drawable.ic_kerberos_logo),
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

                    // Bell icon: tap to open the notifications list.
                    // The red badge only appears when there are unread items.
                    Box(
                        modifier = Modifier.clickable { showNotifications = true }
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = stringResource(R.string.cd_notifications),
                            tint = Color.White
                        )

                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                                    .background(KerberosDanger, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (unreadCount > 9) "9+" else "$unreadCount",
                                    color = Color.White,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = Color.White,
                        modifier = Modifier.clickable { onOpenSettings() }
                    )

                    Spacer(Modifier.width(16.dp))

                    Icon(
                        Icons.Filled.Logout,
                        contentDescription = stringResource(R.string.cd_sign_out),
                        tint = Color.White,
                        modifier = Modifier.clickable { onSignOut() }
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

                // Search bar: filters the list by service name as the user types
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
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
                    // X button only shows when there is text, and clears the search
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cd_clear_search),
                                tint = KerberosLightBlueText,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = KerberosFieldBg,
                        focusedContainerColor = KerberosFieldBg,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        cursorColor = Color.White
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Always shows the total, not the number of search matches
                StatCard("${credentials.size}", stringResource(R.string.stat_credentials), Modifier.weight(1f))
                StatCard("100%", stringResource(R.string.stat_secured), Modifier.weight(1f))
                StatCard(lastSyncedText, stringResource(R.string.stat_last_sync), Modifier.weight(1f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.your_credentials),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // Sort button: tap to open the dropdown. The menu sits inside this
                // Box so it opens directly under the button.
                Box {
                    Text(
                        stringResource(R.string.sort_arrow),
                        color = KerberosBlue,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showSortMenu = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_alphabetical)) },
                            onClick = {
                                sortOption = SORT_ALPHABETICAL
                                showSortMenu = false
                            },
                            // Tick marks the option currently in use
                            trailingIcon = {
                                if (sortOption == SORT_ALPHABETICAL) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = KerberosBlue
                                    )
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_not_alphabetical)) },
                            onClick = {
                                sortOption = SORT_NOT_ALPHABETICAL
                                showSortMenu = false
                            },
                            trailingIcon = {
                                if (sortOption == SORT_NOT_ALPHABETICAL) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = KerberosBlue
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Shown only when something was searched and nothing matched
            if (visibleCredentials.isEmpty() && searchQuery.isNotBlank()) {
                Text(
                    stringResource(R.string.no_search_results),
                    color = KerberosGrayText,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(visibleCredentials) { cred ->
                    CredentialRow(cred, onClick = { onOpenCredential(cred) })
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }

    // Notifications popup. Closing it marks everything as read, which clears the badge.
    if (showNotifications) {
        NotificationsDialog(
            notifications = notifications,
            onClearAll = { notificationStore.clearAll() },
            onDismiss = {
                showNotifications = false
                notificationStore.markAllRead()
            }
        )
    }
}
// Full-screen prompt shown while the vault is locked, offering biometric
// unlock or the option to sign out entirely
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
            contentDescription = stringResource(R.string.cd_biometric_auth),
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
            stringResource(R.string.vault_locked_desc),
            color = KerberosGrayText,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = onAuthenticate) {
            Icon(imageVector = Icons.Filled.Fingerprint, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.unlock_vault))
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onSignOut) {
            Icon(imageVector = Icons.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.sign_out_button))
        }
    }
}
// Small metric tile (e.g. "12 Credentials", "100% Secured")
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
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KerberosBlue)
            Text(label, fontSize = 11.sp, color = KerberosGrayText)
        }
    }
}
// Single row in the credential list: initial-letter avatar, service name,
// username, and a chevron indicating it's tappable
@Composable
fun CredentialRow(
    credential: Credential,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(KerberosBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    credential.serviceName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(credential.serviceName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(credential.username, color = KerberosGrayText, fontSize = 12.sp)
            }

            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = KerberosGrayText)
        }
    }
}
//References
//Android, 2025. Lazy lists and lazy grids. [Online]
//Available at: https://developer.android.com/develop/ui/compose/lists
//devlopers, A., 2026. Get started with Jetpack Compose. [Online]
//Available at: https://developer.android.com/develop/ui/compose/documentation
//Geek4geeks, 2025. Android UI Layouts. [Online]
//Available at: https://www.geeksforgeeks.org/android/android-ui-layouts/
//Geek4geeks, 2025. Responsive UI Design in Android. [Online]
//Available at: https://www.geeksforgeeks.org/android/responsive-ui-design-in-android/
//Pathak, A., 2026. What is System UI. [Online]
//Available at: https://www.browserstack.com/guide/what-is-system-ui
//Android Developers, 2025. Show a biometric authentication dialog. [Online]
//Available at: https://developer.android.com/identity/sign-in/biometric-auth
//[Accessed 22 September 2026].