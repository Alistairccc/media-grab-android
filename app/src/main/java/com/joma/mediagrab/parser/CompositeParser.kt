package com.joma.mediagrab.parser

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult

class CompositeParser(
    private val parsers: List<Parser>,
) : Parser {
    override fun canHandle(url: String): Boolean = parsers.any { it.canHandle(url) }

    override suspend fun parse(url: String): AppResult<MediaInfo> {
        val parser =
            parsers.firstOrNull { it.canHandle(url) }
                ?: return AppResult.Err(AppError.UnsupportedPlatform)
        return parser.parse(url)
    }
}
