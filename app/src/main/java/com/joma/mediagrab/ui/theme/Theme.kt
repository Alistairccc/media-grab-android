package com.joma.mediagrab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NoirColors =
    darkColorScheme(
        primary = NoirAccent,
        onPrimary = Color.White,
        secondary = NoirAccent2,
        onSecondary = NoirBg,
        background = NoirBg,
        onBackground = NoirText,
        surface = NoirSurface,
        onSurface = NoirText,
        surfaceVariant = NoirSurface2,
        onSurfaceVariant = NoirMuted,
        outline = NoirBorder,
        error = NoirError,
        onError = NoirBg,
    )

@Composable
fun MediaGrabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NoirColors,
        typography = AppTypography,
        content = content,
    )
}
