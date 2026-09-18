package com.example.kerberos.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
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

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onSignedIn: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { viewModel.onGoogleIdTokenReceived(it) }
        } catch (e: ApiException) {
            errorMsg = "Google sign-in failed: ${e.message}"
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.SignedIn) onSignedIn()
        if (authState is AuthState.Error) errorMsg = (authState as AuthState.Error).message
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Top brand / hero panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
                .background(
                    Brush.verticalGradient(listOf(KerberosNavy, KerberosBlue))
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_kerberos_logo),
                contentDescription = "Kerberos logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text("Kerberos", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(
                "Your passwords, secured by design",
                color = KerberosLightBlueText,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("AES-256", "Zero-Knowledge", "2FA Ready").forEach { Badge(it) }
            }
            Spacer(Modifier.height(8.dp))
            Badge("Open Source")
        }

        // Bottom sign-in sheet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(24.dp)
        ) {
            Text("Welcome back", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Sign in to access your secure vault", color = KerberosGrayText, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = { launcher.launch(viewModel.googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                GoogleGIcon()
                Spacer(Modifier.width(8.dp))
                Text("Continue with Google", color = Color.Black)
            }

            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { viewModel.simulateSignInError() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Simulate sign-in error \u2192", fontSize = 12.sp, color = KerberosGrayText)
            }

            errorMsg?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "OR",
                color = KerberosGrayText,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* master password flow - future work */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Filled.VpnKey, contentDescription = null, tint = KerberosBlue)
                Spacer(Modifier.width(8.dp))
                Text("Sign in with master password", color = KerberosBlue)
            }
        }
    }
}
