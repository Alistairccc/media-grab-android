package com.joma.mediagrab.parser

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.network.MemoryCookieJar
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult
import com.joma.mediagrab.core.util.LinkParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException
import java.net.URI

/**
 * No-login TikTok parser: the public video page embeds the item metadata in an
 * `api-data` JSON script. A mobile user agent is required; the desktop page
 * answers with a WAF challenge instead.
 */
class TikTokParser(
    client: OkHttpClient,
    private val webBase: String = "https://www.tiktok.com",
    private val cookieJar: MemoryCookieJar = MemoryCookieJar(),
) : Parser {
    private val client = client.newBuilder().cookieJar(cookieJar).build()

    override fun canHandle(url: String): Boolean {
        val host = hostOf(url) ?: return false
        return host == "tiktok.com" || host.endsWith(".tiktok.com")
    }

    override suspend fun parse(url: String): AppResult<MediaInfo> {
        val input =
            LinkParser.extractFirstSupported(url)
                ?: return AppResult.Err(AppError.InvalidLink)
        if (!canHandle(input)) {
            return AppResult.Err(AppError.InvalidLink)
        }
        return try {
            val resolved =
                resolveShortLink(input)
                    ?: return AppResult.Err(AppError.InvalidLink)
            val awemeId =
                extractAwemeId(resolved)
                    ?: return AppResult.Err(AppError.InvalidLink)
            Timber.d("TikTok parse awemeId=%s", awemeId)
            requestPage(awemeId)
        } catch (error: IOException) {
            Timber.w(error, "TikTok parse failed")
            AppResult.Err(AppError.Network(error))
        } catch (error: Exception) {
            Timber.w(error, "TikTok parse failed")
            AppResult.Err(AppError.Parse(error))
        }
    }

    private suspend fun requestPage(awemeId: String): AppResult<MediaInfo> =
        withContext(Dispatchers.IO) {
            val pageUrl = "${webBase.trimEnd('/')}/@/video/$awemeId"
            val request =
                Request
                    .Builder()
                    .url(pageUrl)
                    .header("User-Agent", MOBILE_USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .build()
            client.newCall(request).execute().use { response ->
                Timber.d("TikTok page HTTP %d", response.code)
                if (!response.isSuccessful) {
                    return@withContext AppResult.Err(AppError.Network())
                }
                val html =
                    response.body?.string()
                        ?: return@withContext AppResult.Err(AppError.Parse())
                parsePage(html, pageUrl)
            }
        }

    private fun parsePage(
        html: String,
        pageUrl: String,
    ): AppResult<MediaInfo> {
        val data =
            extractApiData(html)
                ?: return AppResult.Err(AppError.Parse())
        val detail =
            data["videoDetail"] as? JsonObject
                ?: return AppResult.Err(AppError.Parse())
        val status = detail["statusCode"]?.jsonPrimitive?.intOrNull
        if (status != null && status != 0) {
            Timber.w("TikTok video statusCode=%s", status)
            return AppResult.Err(AppError.Parse())
        }
        val item =
            (detail["itemInfo"] as? JsonObject)
                ?.get("itemStruct") as? JsonObject
                ?: return AppResult.Err(AppError.NoStream)
        if ((item["imagePost"] as? JsonObject)?.isNotEmpty() == true) {
            return AppResult.Err(AppError.UnsupportedContent)
        }
        val video =
            item["video"] as? JsonObject
                ?: return AppResult.Err(AppError.NoStream)
        val playUrl =
            video["playAddr"]?.jsonPrimitive?.contentOrNull
                ?: video["downloadAddr"]?.jsonPrimitive?.contentOrNull
                ?: return AppResult.Err(AppError.NoStream)

        val width = video["width"]?.jsonPrimitive?.intOrNull
        val height = video["height"]?.jsonPrimitive?.intOrNull
        val qualityLabel =
            if (width != null && height != null) {
                "${minOf(width, height)}P"
            } else {
                ""
            }
        val author =
            (item["author"] as? JsonObject)
                ?.get("nickname")
                ?.jsonPrimitive
                ?.contentOrNull
                .orEmpty()
        val title = item["desc"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
        val headers =
            mutableMapOf(
                "User-Agent" to MOBILE_USER_AGENT,
                "Referer" to pageUrl,
            )
        cookieHeader(playUrl)?.let { headers["Cookie"] = it }

        return AppResult.Ok(
            MediaInfo(
                platform = Platform.TIKTOK,
                title = title,
                author = author,
                coverUrl = video["cover"]?.jsonPrimitive?.contentOrNull,
                durationMs = video["duration"]?.jsonPrimitive?.longOrNull?.times(1000) ?: 0L,
                width = width,
                height = height,
                streams =
                    listOf(
                        Stream(
                            qualityLabel = qualityLabel,
                            url = playUrl,
                            headers = headers,
                            watermarkFree = true,
                        ),
                    ),
            ),
        )
    }

    private fun cookieHeader(url: String): String? = url.toHttpUrlOrNull()?.let(cookieJar::headerFor)

    private fun extractApiData(html: String): JsonObject? {
        val match = API_DATA_PATTERN.find(html) ?: return null
        return runCatching {
            Json.parseToJsonElement(match.groupValues[1]).jsonObject
        }.getOrNull()
    }

    private suspend fun resolveShortLink(url: String): String? {
        if (!isShortLink(url)) {
            return url
        }
        return withContext(Dispatchers.IO) {
            try {
                val request =
                    Request
                        .Builder()
                        .url(url)
                        .header("User-Agent", MOBILE_USER_AGENT)
                        .build()
                client.newCall(request).execute().use { response ->
                    response.request.url.toString()
                }
            } catch (error: IOException) {
                null
            }
        }
    }

    private fun isShortLink(url: String): Boolean {
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        val host = uri.host?.lowercase() ?: return false
        if (host == "vm.tiktok.com" || host == "vt.tiktok.com") {
            return true
        }
        return uri.path?.startsWith("/t/") == true
    }

    private fun extractAwemeId(url: String): String? {
        for (pattern in AWEME_ID_PATTERNS) {
            val match = pattern.find(url) ?: continue
            return match.groupValues[1]
        }
        return null
    }

    private fun hostOf(url: String): String? = runCatching { URI(url).host }.getOrNull()?.lowercase()

    private companion object {
        const val MOBILE_USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 " +
                "(KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"

        val API_DATA_PATTERN =
            Regex(
                """<script[^>]*id="api-data"[^>]*>(.*?)</script>""",
                RegexOption.DOT_MATCHES_ALL,
            )

        val AWEME_ID_PATTERNS =
            listOf(
                Regex("""/video/(\d{6,25})"""),
                Regex("""/v/(\d{6,25})"""),
                Regex("""/embed/(\d{6,25})"""),
                Regex("""[?&](?:item_id|aweme_id)=(\d{6,25})"""),
            )
    }
}
