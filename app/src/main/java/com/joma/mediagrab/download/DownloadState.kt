package com.joma.mediagrab.download

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.data.download.DownloadProgress

sealed interface DownloadState {
    data object Idle : DownloadState

    data class Running(
        val media: MediaInfo,
        val stream: Stream,
        val progress: DownloadProgress,
    ) : DownloadState

    data class Completed(
        val media: MediaInfo,
        val stream: Stream,
    ) : DownloadState

    data class Failed(
        val media: MediaInfo,
        val stream: Stream,
        val error: AppError,
    ) : DownloadState

    data class Cancelled(
        val media: MediaInfo,
        val stream: Stream,
    ) : DownloadState
}
