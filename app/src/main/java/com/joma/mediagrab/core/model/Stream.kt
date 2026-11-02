package com.joma.mediagrab.core.model

data class Stream(
    val qualityLabel: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val mime: String = "video/mp4",
    val sizeBytes: Long? = null,
)
