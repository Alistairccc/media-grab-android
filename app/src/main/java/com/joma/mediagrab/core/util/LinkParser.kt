package com.joma.mediagrab.core.util

import java.net.URI

object LinkParser {
    private val urlPattern = Regex("""https?://[^\s]+""", RegexOption.IGNORE_CASE)

    private val supportedHosts =
        setOf(
            "douyin.com",
            "v.douyin.com",
            "www.douyin.com",
            "iesdouyin.com",
            "www.iesdouyin.com",
            "bilibili.com",
            "www.bilibili.com",
            "b23.tv",
            "tiktok.com",
        )

    fun extractFirstSupported(text: String): String? =
        urlPattern
            .findAll(text)
            .map { match -> match.value.trimEnd('.', ',', '，', '。', ')', '）') }
            .firstOrNull(::isSupported)

    fun isSupported(url: String): Boolean {
        val host = runCatching { URI(url).host }.getOrNull()?.lowercase() ?: return false
        return supportedHosts.any { supported ->
            host == supported || host.endsWith(".$supported")
        }
    }
}
