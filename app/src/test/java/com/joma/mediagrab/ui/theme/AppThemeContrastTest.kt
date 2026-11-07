package com.joma.mediagrab.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class AppThemeContrastTest {
    @Test
    fun `text tokens meet wcag aa on their surfaces`() {
        AppTheme.entries.forEach { theme ->
            val colors = theme.colors
            assertContrast(theme, "onBackground/background", colors.onBackground, colors.background, 7.0)
            assertContrast(theme, "muted/surface", colors.muted, colors.surface, 4.5)
            assertContrast(theme, "onPrimary/primary", colors.onPrimary, colors.primary, 4.5)
            assertContrast(theme, "onPrimaryDeep/primaryDeep", colors.onPrimaryDeep, colors.primaryDeep, 4.5)
            assertContrast(theme, "onPrimary/secondary", colors.onPrimary, colors.secondary, 4.5)
        }
    }

    private fun assertContrast(
        theme: AppTheme,
        pair: String,
        foreground: Color,
        background: Color,
        minimum: Double,
    ) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            "${theme.key} $pair contrast ${"%.2f".format(ratio)} < $minimum",
            ratio >= minimum,
        )
    }

    private fun contrastRatio(
        a: Color,
        b: Color,
    ): Double {
        val (lighter, darker) = listOf(luminance(a), luminance(b)).sortedDescending()
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun luminance(color: Color): Double =
        0.2126 * linearize(color.red) +
            0.7152 * linearize(color.green) +
            0.0722 * linearize(color.blue)

    private fun linearize(channel: Float): Double {
        val value = channel.toDouble()
        return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
    }
}
