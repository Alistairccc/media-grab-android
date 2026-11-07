package com.joma.mediagrab.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemeTest {
    @Test
    fun `falls back to cyan for missing or unknown keys`() {
        assertEquals(AppTheme.CYAN, AppTheme.fromKey(null))
        assertEquals(AppTheme.CYAN, AppTheme.fromKey("unknown"))
    }

    @Test
    fun `round trips every preset key`() {
        AppTheme.entries.forEach { theme ->
            assertEquals(theme, AppTheme.fromKey(theme.key))
        }
    }

    @Test
    fun `presets use distinct backgrounds and primaries`() {
        assertEquals(
            AppTheme.entries.size,
            AppTheme.entries.map { it.colors.background }.distinct().size,
        )
        assertEquals(
            AppTheme.entries.size,
            AppTheme.entries.map { it.colors.primary }.distinct().size,
        )
    }
}
