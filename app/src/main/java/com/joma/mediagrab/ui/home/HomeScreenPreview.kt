package com.joma.mediagrab.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.ui.components.ErrorBanner
import com.joma.mediagrab.ui.components.VideoCard
import com.joma.mediagrab.ui.theme.AppTheme
import com.joma.mediagrab.ui.theme.MediaGrabTheme

private val previewStream =
    Stream(
        qualityLabel = "720P · 无水印",
        url = "https://example.com/720.mp4",
        sizeBytes = 12_400_000,
    )

private val previewMedia =
    MediaInfo(
        platform = Platform.DOUYIN,
        title = "凌晨四点的城市，原来是这样一点点苏醒的｜4K 延时记录",
        author = "@城市漫游者",
        durationMs = 32_000,
        width = 1080,
        height = 1920,
        streams =
            listOf(
                previewStream,
                Stream("480P", "https://example.com/480.mp4", sizeBytes = 6_200_000),
            ),
    )

@Preview(name = "Ready", widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF08080B)
@Composable
private fun ReadyPreview() {
    MediaGrabTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(AppTheme.CYAN.colors.background)
                    .padding(18.dp),
        ) {
            VideoCard(
                media = previewMedia,
                selectedStream = previewStream,
                isDownloading = false,
                isDone = false,
                percent = 0,
                bytesPerSecond = 0,
                remainingSeconds = null,
                onQualityClick = {},
                onSave = {},
                onCancel = {},
            )
        }
    }
}

@Preview(name = "Downloading", widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF08080B)
@Composable
private fun DownloadingPreview() {
    MediaGrabTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(AppTheme.CYAN.colors.background)
                    .padding(18.dp),
        ) {
            VideoCard(
                media = previewMedia,
                selectedStream = previewStream,
                isDownloading = true,
                isDone = false,
                percent = 62,
                bytesPerSecond = 3_200_000,
                remainingSeconds = 8,
                onQualityClick = {},
                onSave = {},
                onCancel = {},
            )
        }
    }
}

@Preview(name = "Done", widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF08080B)
@Composable
private fun DonePreview() {
    MediaGrabTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(AppTheme.CYAN.colors.background)
                    .padding(18.dp),
        ) {
            VideoCard(
                media = previewMedia,
                selectedStream = previewStream,
                isDownloading = false,
                isDone = true,
                percent = 100,
                bytesPerSecond = 0,
                remainingSeconds = null,
                onQualityClick = {},
                onSave = {},
                onCancel = {},
            )
        }
    }
}

@Preview(name = "Failed", widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF08080B)
@Composable
private fun FailedPreview() {
    MediaGrabTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(AppTheme.CYAN.colors.background)
                    .padding(18.dp),
        ) {
            ErrorBanner(error = AppError.InvalidLink, onRetry = {})
        }
    }
}
