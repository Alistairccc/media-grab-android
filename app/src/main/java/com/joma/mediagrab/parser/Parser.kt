package com.joma.mediagrab.parser

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.result.AppResult

interface Parser {
    fun canHandle(url: String): Boolean

    suspend fun parse(url: String): AppResult<MediaInfo>
}
