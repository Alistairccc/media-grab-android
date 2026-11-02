package com.joma.mediagrab.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.joma.mediagrab.R

val ManropeFamily =
    FontFamily(
        Font(R.font.manrope_bold, FontWeight.Bold),
    )

val NotoSerifFamily =
    FontFamily(
        Font(R.font.noto_serif_sc_medium, FontWeight.Medium),
    )

val JetBrainsMonoFamily =
    FontFamily(
        Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
        Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    )

val UiSansFamily = FontFamily.SansSerif

object AppType {
    val brand =
        TextStyle(
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.24.em,
        )

    val title =
        TextStyle(
            fontFamily = NotoSerifFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 17.5.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.02.em,
        )

    val body =
        TextStyle(
            fontFamily = UiSansFamily,
            fontSize = 13.sp,
            lineHeight = 20.sp,
        )

    val meta =
        TextStyle(
            fontFamily = UiSansFamily,
            fontSize = 11.sp,
            lineHeight = 16.sp,
        )

    val label =
        TextStyle(
            fontFamily = UiSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.5.sp,
            lineHeight = 16.sp,
        )

    val value =
        TextStyle(
            fontFamily = NotoSerifFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.02.em,
        )

    val button =
        TextStyle(
            fontFamily = UiSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.16.em,
        )

    val section =
        TextStyle(
            fontFamily = UiSansFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.5.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.06.em,
        )

    val listTitle =
        TextStyle(
            fontFamily = UiSansFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )

    val monoSm =
        TextStyle(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 9.5.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.02.em,
        )

    val monoMd =
        TextStyle(
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )

    val monoLg =
        TextStyle(
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 22.sp,
        )
}

val AppTypography =
    Typography(
        bodyLarge = AppType.body,
        bodyMedium = AppType.meta,
        labelLarge = AppType.button,
    )
