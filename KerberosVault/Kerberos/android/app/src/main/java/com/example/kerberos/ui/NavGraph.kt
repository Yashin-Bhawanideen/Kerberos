package com.example.kerberos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kerberos.auth.AuthViewModel
import com.example.kerberos.data.VaultViewModel
import com.example.kerberos.notifications.NotificationStore
import com.example.kerberos.notifications.NotificationType
import com.example.kerberos.settings.SettingsManager

// Defines the app's navigation graph: which screen is shown for each route,
// and how they connect (login -> vault -> add/detail/settings)
@Composable
fun KerberosNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val vaultViewModel: VaultViewModel = viewModel()

    // Shared notification list used by the bell icon on the vault screen
    val context = LocalContext.current
    val notificationStore = remember { NotificationStore.get(context) }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            // Whenever the login screen is showing, the vault must be locked so
            // the next sign-in goes through the biometric check again
            LaunchedEffect(Unit) {
                vaultViewModel.lockVault()
            }

            LoginScreen(authViewModel) {
                // Records the successful login; the time is stored automatically
                notificationStore.add(NotificationType.LOGIN)

                // Clears "login" from the back stack so the user can't
                // navigate back to it after signing in
                navController.navigate("vault") {
                    popUpTo("login") {
                        inclusive = true
                    }
                }
            }
        }

        composable("vault") {
            VaultScreen(
                authViewModel = authViewModel,
                vaultViewModel = vaultViewModel,
                onAddCredential = {
                    navController.navigate("add")
                },
                onOpenCredential = { cred ->
                    vaultViewModel.selected = cred
                    navController.navigate("detail")
                },
                onOpenSettings = {
                    navController.navigate("settings")
                },
                onSignOut = {
                    authViewModel.signOut()
                    // Removes this user's activity so the next user starts clean
                    notificationStore.clearAll()
                    navController.navigate("login") {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("add") {
            AddCredentialScreen(
                vaultViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable("detail") {
            vaultViewModel.selected?.let { cred ->
                CredentialDetailScreen(
                    credential = cred,
                    vaultViewModel = vaultViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onEdit = {
                        navController.navigate("edit")
                    }
                )
            }
        }

        //adds the edit screen to the navigation graph (Android Developers, 2026)
        composable("edit") {
            vaultViewModel.selected?.let { cred ->
                EditCredentialScreen(
                    credential = cred,
                    vaultViewModel = vaultViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onSaved = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("settings") {
            // remember avoids recreating SettingsManager on every recomposition
            val settingsManager = remember {
                SettingsManager(context)
            }

            SettingsScreen(
                settingsManager = settingsManager,
                // Returns to the vault (home) screen
                onBack = {
                    navController.popBackStack()
                },
                onSignOut = {
                    authViewModel.signOut()
                    // Removes this user's activity so the next user starts clean
                    notificationStore.clearAll()

                    navController.navigate("login") {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
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
//Android Developers, 2026. Navigation with Compose. [Online]
//Available at: https://developer.android.com/develop/ui/compose/navigation
//[Accessed 22 September 2026].