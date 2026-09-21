package com.example.kerberos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.kerberos.ui.KerberosNavGraph
import com.example.kerberos.ui.theme.KerberosTheme

import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KerberosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KerberosNavGraph()
                }
            }
        }
    }
}
