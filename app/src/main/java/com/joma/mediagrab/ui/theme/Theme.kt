package com.joma.mediagrab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun MediaGrabTheme(
    theme: AppTheme = AppTheme.CYAN,
    content: @Composable () -> Unit,
) {
    val colors = theme.colors
    val colorScheme =
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            secondary = colors.secondary,
            onSecondary = colors.onPrimary,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onBackground,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.muted,
            outline = colors.border,
            error = colors.error,
            onError = colors.onPrimary,
        )
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
