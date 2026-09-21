package com.example.kerberos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

private val KerberosColorScheme = lightColorScheme(
    primary = KerberosBlue,
    onPrimary = KerberosCardWhite,
    background = KerberosBackground,
    surface = KerberosCardWhite,
    error = KerberosDanger
)

@Composable
fun KerberosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KerberosColorScheme,
        typography = MaterialTheme.typography.copy(
            headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        ),
        content = content
    )
}
