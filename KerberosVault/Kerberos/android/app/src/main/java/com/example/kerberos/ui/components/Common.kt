package com.example.kerberos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kerberos.ui.theme.KerberosBadgeBg

@Composable
fun Badge(text: String) {
    Text(
        text,
        color = Color.White,
        fontSize = 11.sp,
        modifier = Modifier
            .background(KerberosBadgeBg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** A simple stand-in for the multicolour Google "G" mark, used on the sign-in button. */
@Composable
fun GoogleGIcon(size: androidx.compose.ui.unit.Dp = 18.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "G",
            fontSize = (size.value * 0.65).sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4285F4)
        )
    }
}
