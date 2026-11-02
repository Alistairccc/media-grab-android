package com.joma.mediagrab.core.model

data class MediaInfo(
    val platform: Platform,
    val title: String,
    val author: String,
    val coverUrl: String? = null,
    val durationMs: Long = 0,
    val width: Int? = null,
    val height: Int? = null,
    val streams: List<Stream> = emptyList(),
)
