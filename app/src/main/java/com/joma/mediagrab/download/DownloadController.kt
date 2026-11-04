package com.joma.mediagrab.download

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Stream
import kotlinx.coroutines.flow.StateFlow

interface DownloadController {
    val state: StateFlow<DownloadState>

    fun start(
        media: MediaInfo,
        stream: Stream,
    )

    fun cancel()

    fun reset()
}
