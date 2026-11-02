package com.joma.mediagrab.data.download

data class DownloadProgress(
    val percent: Int,
    val bytesPerSecond: Long = 0,
    val remainingSeconds: Long? = null,
)
