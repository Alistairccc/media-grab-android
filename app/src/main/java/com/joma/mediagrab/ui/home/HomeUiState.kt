package com.joma.mediagrab.ui.home

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.result.AppError

sealed interface HomeUiState {
    data object Idle : HomeUiState

    data object Parsing : HomeUiState

    data class Ready(
        val media: MediaInfo,
        val selectedStream: Stream,
    ) : HomeUiState

    data class Downloading(
        val media: MediaInfo,
        val selectedStream: Stream,
        val percent: Int,
        val bytesPerSecond: Long = 0,
        val remainingSeconds: Long? = null,
    ) : HomeUiState

    data class Done(
        val media: MediaInfo,
        val selectedStream: Stream,
    ) : HomeUiState

    data class Failed(val error: AppError) : HomeUiState
}
