package com.joma.mediagrab.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joma.mediagrab.R
import com.joma.mediagrab.core.model.Platform

@Composable
internal fun platformLabel(platform: Platform): String =
    stringResource(
        when (platform) {
            Platform.DOUYIN -> R.string.platform_douyin
            Platform.BILIBILI -> R.string.platform_bilibili
        },
    )

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
    }
