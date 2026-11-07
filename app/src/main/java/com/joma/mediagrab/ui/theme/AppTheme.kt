package com.joma.mediagrab.ui.theme

import androidx.annotation.StringRes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.joma.mediagrab.R

data class AppColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryDeep: Color,
    val onPrimaryDeep: Color,
    val secondary: Color,
    val muted: Color,
    val border: Color,
    val track: Color,
    val error: Color,
    val hero: Color,
)

enum class AppTheme(
    val key: String,
    @param:StringRes val labelRes: Int,
) {
    CYAN("cyan", R.string.theme_cyan),
    BLUE("blue", R.string.theme_blue),
    VIOLET("violet", R.string.theme_violet),
    EMBER("ember", R.string.theme_ember),
    ;

    val colors: AppColors
        get() =
            when (this) {
                CYAN -> CyanColors
                BLUE -> BlueColors
                VIOLET -> VioletColors
                EMBER -> EmberColors
            }

    companion object {
        fun fromKey(key: String?): AppTheme = entries.firstOrNull { it.key == key } ?: CYAN
    }
}

private val CyanColors =
    AppColors(
        background = Color(0xFF15181F),
        onBackground = Color(0xFFF2F5F7),
        surface = Color(0xFF232633),
        surfaceVariant = Color(0xFF2C3140),
        primary = Color(0xFF36AFCB),
        onPrimary = Color(0xFF061317),
        primaryDeep = Color(0xFF247E94),
        onPrimaryDeep = Color(0xFFFFFFFF),
        secondary = Color(0xFF7DD3FC),
        muted = Color(0xFF9AA4B2),
        border = Color(0x14FFFFFF),
        track = Color(0xFF3A4252),
        error = Color(0xFFFF6B6B),
        hero = Color(0x8C36AFCB),
    )

private val BlueColors =
    AppColors(
        background = Color(0xFF0F1420),
        onBackground = Color(0xFFEDF2FA),
        surface = Color(0xFF1A2233),
        surfaceVariant = Color(0xFF243048),
        primary = Color(0xFF4C8DFF),
        onPrimary = Color(0xFF071021),
        primaryDeep = Color(0xFF2F63C7),
        onPrimaryDeep = Color(0xFFFFFFFF),
        secondary = Color(0xFF7DD3FC),
        muted = Color(0xFF93A3B8),
        border = Color(0x14FFFFFF),
        track = Color(0xFF2C3A55),
        error = Color(0xFFFF6B6B),
        hero = Color(0x8C4C8DFF),
    )

private val VioletColors =
    AppColors(
        background = Color(0xFF141219),
        onBackground = Color(0xFFF5F1FA),
        surface = Color(0xFF211D2B),
        surfaceVariant = Color(0xFF2C2639),
        primary = Color(0xFFA78BFA),
        onPrimary = Color(0xFF140F1F),
        primaryDeep = Color(0xFF7C5CE0),
        onPrimaryDeep = Color(0xFFFFFFFF),
        secondary = Color(0xFFF472B6),
        muted = Color(0xFFA79FB8),
        border = Color(0x14FFFFFF),
        track = Color(0xFF3A3348),
        error = Color(0xFFFF6B6B),
        hero = Color(0x8CA78BFA),
    )

private val EmberColors =
    AppColors(
        background = Color(0xFF171310),
        onBackground = Color(0xFFFAF4EE),
        surface = Color(0xFF241E19),
        surfaceVariant = Color(0xFF302822),
        primary = Color(0xFFFF8A4C),
        onPrimary = Color(0xFF1A0D04),
        primaryDeep = Color(0xFFDC6427),
        onPrimaryDeep = Color(0xFF1A0D04),
        secondary = Color(0xFFFFC15E),
        muted = Color(0xFFB3A296),
        border = Color(0x14FFFFFF),
        track = Color(0xFF3C3128),
        error = Color(0xFFFF6B6B),
        hero = Color(0x8CFF8A4C),
    )

val LocalAppColors = staticCompositionLocalOf { CyanColors }
