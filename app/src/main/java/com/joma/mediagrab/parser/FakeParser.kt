package com.joma.mediagrab.parser

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult
import com.joma.mediagrab.core.util.LinkParser
import kotlinx.coroutines.delay

class FakeParser : Parser {
    override fun canHandle(url: String): Boolean = LinkParser.isSupported(url)

    override suspend fun parse(url: String): AppResult<MediaInfo> {
        delay(600)
        if (url.contains("fail")) {
            return AppResult.Err(AppError.Parse())
        }

        val platform = if (url.contains("douyin")) Platform.DOUYIN else Platform.BILIBILI
        return AppResult.Ok(
            MediaInfo(
                platform = platform,
                title = "凌晨四点的城市，原来是这样一点点苏醒的｜4K 延时记录",
                author = "@城市漫游者",
                durationMs = 32_000,
                width = 1080,
                height = 1920,
                streams =
                    listOf(
                        Stream(
                            qualityLabel = "720P",
                            url = "https://example.com/720.mp4",
                            sizeBytes = 12_400_000,
                            watermarkFree = true,
                        ),
                        Stream(
                            qualityLabel = "480P",
                            url = "https://example.com/480.mp4",
                            sizeBytes = 6_200_000,
                        ),
                    ),
            ),
        )
    }
}
