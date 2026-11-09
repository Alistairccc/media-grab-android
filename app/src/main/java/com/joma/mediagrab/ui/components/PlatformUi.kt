package com.joma.mediagrab.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joma.mediagrab.R
import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.model.Stream

@Composable
internal fun platformLabel(platform: Platform): String =
    stringResource(
        when (platform) {
            Platform.DOUYIN -> R.string.platform_douyin
            Platform.BILIBILI -> R.string.platform_bilibili
            Platform.TIKTOK -> R.string.platform_tiktok
        },
    )

@Composable
internal fun mediaTitle(media: MediaInfo): String {
    if (media.title.isNotBlank()) {
        return media.title
    }
    return stringResource(
        when (media.platform) {
            Platform.DOUYIN -> R.string.fallback_title_douyin
            Platform.BILIBILI -> R.string.fallback_title_bilibili
            Platform.TIKTOK -> R.string.fallback_title_tiktok
        },
    )
}

@Composable
internal fun streamLabel(stream: Stream): String {
    val quality =
        if (stream.qualityLabel.isNotBlank()) {
            stream.qualityLabel
        } else {
            stringResource(R.string.quality_unknown)
        }
    return if (stream.watermarkFree) {
        "$quality · ${stringResource(R.string.no_watermark)}"
    } else {
        quality
    }
}

internal fun platformDot(platform: Platform): Brush =
    when (platform) {
        Platform.DOUYIN ->
            Brush.horizontalGradient(
                listOf(Color(0xFFFE2C55), Color(0xFF25F4EE)),
            )

        Platform.BILIBILI ->
            Brush.horizontalGradient(
                listOf(Color(0xFFFB7299), Color(0xFFFB7299)),
            )

        Platform.TIKTOK ->
            Brush.horizontalGradient(
                listOf(Color(0xFF69C9D0), Color(0xFFEE1D52)),
            )
    }
