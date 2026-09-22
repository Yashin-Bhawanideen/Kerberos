package com.example.kerberos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kerberos.auth.AuthViewModel
import com.example.kerberos.data.VaultViewModel
import com.example.kerberos.settings.SettingsManager

// Defines the app's navigation graph: which screen is shown for each route,
// and how they connect (login -> vault -> add/detail/settings)
@Composable
fun KerberosNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val vaultViewModel: VaultViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(authViewModel) {
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
            val context = LocalContext.current
            // remember avoids recreating SettingsManager on every recomposition
            val settingsManager = remember {
                SettingsManager(context)
            }

            SettingsScreen(
                settingsManager = settingsManager,
                onSignOut = {
                    navController.navigate("login") {
                        popUpTo("vault") {
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