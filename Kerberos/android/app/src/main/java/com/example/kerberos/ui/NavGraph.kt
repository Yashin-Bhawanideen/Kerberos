package com.example.kerberos.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kerberos.auth.AuthViewModel
import com.example.kerberos.data.VaultViewModel

@Composable
fun KerberosNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val vaultViewModel: VaultViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(authViewModel) {
                navController.navigate("vault") { popUpTo("login") { inclusive = true } }
            }
        }
        composable("vault") {
            VaultScreen(
                authViewModel, vaultViewModel,
                onAddCredential = { navController.navigate("add") },
                onOpenCredential = { cred ->
                    vaultViewModel.selected = cred
                    navController.navigate("detail")
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("add") {
            AddCredentialScreen(
                vaultViewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable("detail") {
            vaultViewModel.selected?.let { cred ->
                CredentialDetailScreen(cred, vaultViewModel) { navController.popBackStack() }
            }
        }
    }
}