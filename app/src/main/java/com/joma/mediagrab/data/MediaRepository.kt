package com.joma.mediagrab.data

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult
import com.joma.mediagrab.core.util.LinkParser
import com.joma.mediagrab.parser.Parser

class MediaRepository(
    private val parser: Parser,
) {
    suspend fun parse(url: String): AppResult<MediaInfo> {
        if (!LinkParser.isSupported(url)) {
            return AppResult.Err(AppError.InvalidLink)
        }
        if (!parser.canHandle(url)) {
            return AppResult.Err(AppError.UnsupportedPlatform)
        }
        return parser.parse(url)
    }
}
