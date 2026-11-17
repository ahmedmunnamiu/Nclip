package com.ahmedmunna.nclip.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object C {
    val bg = Color(0xFF000000)
    val card = Color(0xFF0A0A0A)
    val press = Color(0xFF1C1C1C)
    val line = Color(0xFF222222)
    val line2 = Color(0xFF3A3A3A)
    val hi = Color(0xFFF5F5F5)
    val mid = Color(0xFFB0B0B0)
    val lo = Color(0xFF6E6E6E)
    val acc = Color(0xFF00E5A8)
    val danger = Color(0xFFFF5252)
}

val Sq = RoundedCornerShape(0.dp)

@Composable
fun NclipTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.acc, onPrimary = Color.Black,
            background = C.bg, onBackground = C.hi,
            surface = C.bg, onSurface = C.hi,
            surfaceVariant = C.card, outline = C.line, error = C.danger
        ),
        shapes = Shapes(Sq, Sq, Sq, Sq, Sq),
        content = content
    )
}
