package com.example.kerberos.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kerberos.R
import com.example.kerberos.auth.AuthState
import com.example.kerberos.auth.AuthViewModel
import com.example.kerberos.ui.components.Badge
import com.example.kerberos.ui.components.GoogleGIcon
import com.example.kerberos.ui.theme.KerberosBlue
import com.example.kerberos.ui.theme.KerberosGrayText
import com.example.kerberos.ui.theme.KerberosLightBlueText
import com.example.kerberos.ui.theme.KerberosNavy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

// True when the phone has a connection that can actually reach the internet
// (Android Developers, 2026). Needs the ACCESS_NETWORK_STATE permission.
private fun isOnline(context: Context): Boolean {
    return try {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (e: SecurityException) {
        // Permission missing: assume online so sign-in is never blocked by mistake
        true
    }
}

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current

    val authState by viewModel.authState.collectAsState()
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // True when the problem is "no internet". Kept as a flag (not text) so the
    // message can come from strings.xml and follow the chosen language.
    var showOfflineError by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { viewModel.onGoogleIdTokenReceived(it) }
        } catch (e: ApiException) {
            if (e.statusCode == CommonStatusCodes.NETWORK_ERROR || !isOnline(context)) {
                showOfflineError = true
            } else {
                errorMsg = "Google sign-in failed: ${e.message}"
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.SignedIn) onSignedIn()

        if (authState is AuthState.Error) {
            if (!isOnline(context)) {
                showOfflineError = true
            } else {
                errorMsg = (authState as AuthState.Error).message
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
                .background(Brush.verticalGradient(listOf(KerberosNavy, KerberosBlue)))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_kerberos_logo),
                contentDescription = stringResource(R.string.cd_kerberos_logo),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text("Kerberos", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.tagline),
                color = KerberosLightBlueText,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    stringResource(R.string.badge_aes),
                    stringResource(R.string.badge_zero_knowledge),
                    stringResource(R.string.badge_2fa)
                ).forEach { Badge(it) }
            }
            Spacer(Modifier.height(8.dp))
            Badge(stringResource(R.string.badge_open_source))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(24.dp)
        ) {
            Text(stringResource(R.string.welcome_back), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.sign_in_subtitle), color = KerberosGrayText, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    // Clear any earlier error before trying again
                    errorMsg = null

                    if (!isOnline(context)) {
                        // No internet: show the message instead of opening Google sign-in
                        showOfflineError = true
                    } else {
                        showOfflineError = false
                        launcher.launch(viewModel.googleSignInClient.signInIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                GoogleGIcon()
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.continue_with_google), color = Color.Black)
            }

            // No-internet message takes priority over other error text
            if (showOfflineError) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.error_no_internet),
                    color = Color.Red,
                    fontSize = 12.sp
                )
            } else {
                errorMsg?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color.Red, fontSize = 12.sp)
                }
            }
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
//Available at: https://www.geeksforgeeks.org/android/what-is-system-ui