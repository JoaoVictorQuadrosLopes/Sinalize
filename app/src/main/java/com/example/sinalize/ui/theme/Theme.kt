package com.example.sinalize.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B4DFF),
    primaryContainer = Color(0xFFE4E0FF),
    secondary = Color(0xFF00A6A6),
    secondaryContainer = Color(0xFFD7F7F7),
    background = Color(0xFFF8F8FF),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun SinalizeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
