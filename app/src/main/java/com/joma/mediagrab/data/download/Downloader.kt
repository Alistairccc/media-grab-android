package com.joma.mediagrab.data.download

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Stream
import kotlinx.coroutines.flow.Flow

fun interface Downloader {
    fun download(
        media: MediaInfo,
        stream: Stream,
    ): Flow<DownloadProgress>
}
