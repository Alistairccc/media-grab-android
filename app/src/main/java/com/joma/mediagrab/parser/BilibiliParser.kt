package com.joma.mediagrab.parser

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.network.UserAgents
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult
import com.joma.mediagrab.core.util.LinkParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException
import java.net.URI

/**
 * No-login Bilibili parser: the public `view` API identifies the video and
 * `playurl` with `fnval=1` returns a single MP4 (video + audio).
 */
class BilibiliParser(
    private val client: OkHttpClient,
    private val apiBase: String = "https://api.bilibili.com",
    private val webBase: String = "https://www.bilibili.com",
) : Parser {
    override fun canHandle(url: String): Boolean {
        val host = hostOf(url) ?: return false
        return host == "bilibili.com" || host.endsWith(".bilibili.com") ||
            host == "b23.tv" || host.endsWith(".b23.tv")
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
            val bvid =
                BV_PATTERN.find(resolved)?.value
                    ?: return AppResult.Err(AppError.InvalidLink)
            val page = pageOf(resolved)
            Timber.d("Bilibili parse bvid=%s page=%d", bvid, page)
            requestVideo(bvid, page)
        } catch (error: IOException) {
            Timber.w(error, "Bilibili parse failed")
            AppResult.Err(AppError.Network(error))
        } catch (error: Exception) {
            Timber.w(error, "Bilibili parse failed")
            AppResult.Err(AppError.Parse(error))
        }
    }

    private suspend fun requestVideo(
        bvid: String,
        page: Int,
    ): AppResult<MediaInfo> =
        withContext(Dispatchers.IO) {
            val view = fetchData(bvid, "/x/web-interface/view?bvid=$bvid")
            val cid =
                cidOf(view, page)
                    ?: return@withContext AppResult.Err(
                        AppError.Parse(IllegalStateException("page $page not found")),
                    )
            val play =
                fetchData(
                    bvid,
                    "/x/player/playurl?bvid=$bvid&cid=$cid&qn=64&fnval=1&fourk=0",
                )
            val durl = play["durl"] as? JsonArray
            val segment =
                durl?.firstOrNull() as? JsonObject
                    ?: return@withContext AppResult.Err(AppError.NoStream)
            val playUrl =
                segment["url"]?.jsonPrimitive?.contentOrNull
                    ?: return@withContext AppResult.Err(AppError.NoStream)

            val owner = view["owner"] as? JsonObject
            val dimension = view["dimension"] as? JsonObject
            val quality = play["quality"]?.jsonPrimitive?.intOrNull
            val title = view["title"]?.jsonPrimitive?.contentOrNull.orEmpty().ifEmpty { "B站视频" }
            AppResult.Ok(
                MediaInfo(
                    platform = Platform.BILIBILI,
                    title = title,
                    author = owner?.get("name")?.jsonPrimitive?.contentOrNull.orEmpty(),
                    coverUrl =
                        view["pic"]
                            ?.jsonPrimitive
                            ?.contentOrNull
                            ?.replaceFirst("http://", "https://"),
                    durationMs = view["duration"]?.jsonPrimitive?.longOrNull?.times(1000) ?: 0L,
                    width = dimension?.get("width")?.jsonPrimitive?.intOrNull,
                    height = dimension?.get("height")?.jsonPrimitive?.intOrNull,
                    streams =
                        listOf(
                            Stream(
                                qualityLabel = QUALITY_LABELS[quality] ?: "视频",
                                url = playUrl,
                                headers =
                                    mapOf(
                                        "User-Agent" to UserAgents.DESKTOP_CHROME,
                                        "Referer" to "$webBase/",
                                    ),
                                sizeBytes = segment["size"]?.jsonPrimitive?.longOrNull,
                            ),
                        ),
                ),
            )
        }

    private fun fetchData(
        bvid: String,
        path: String,
    ): JsonObject {
        val request =
            Request
                .Builder()
                .url("${apiBase.trimEnd('/')}$path")
                .header("User-Agent", UserAgents.DESKTOP_CHROME)
                .header("Referer", "$webBase/video/$bvid")
                .build()
        client.newCall(request).execute().use { response ->
            Timber.d("Bilibili API HTTP %d", response.code)
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("empty response body")
            val root = Json.parseToJsonElement(body).jsonObject
            val code = root["code"]?.jsonPrimitive?.intOrNull
            if (code != 0) {
                Timber.w(
                    "Bilibili API code=%s message=%s",
                    code,
                    root["message"]?.jsonPrimitive?.contentOrNull,
                )
                throw IllegalStateException("bilibili code $code: ${root["message"]}")
            }
            return root["data"] as? JsonObject
                ?: throw IllegalStateException("bilibili response has no data")
        }
    }

    private fun cidOf(
        view: JsonObject,
        page: Int,
    ): Long? {
        val pages = view["pages"] as? JsonArray
        val match =
            pages?.firstOrNull { element ->
                (element as? JsonObject)?.get("page")?.jsonPrimitive?.intOrNull == page
            } as? JsonObject
        return match?.get("cid")?.jsonPrimitive?.longOrNull
            ?: if (page == 1) view["cid"]?.jsonPrimitive?.longOrNull else null
    }

    private suspend fun resolveShortLink(url: String): String? {
        val host = hostOf(url) ?: return null
        if (!host.endsWith("b23.tv")) {
            return url
        }
        return withContext(Dispatchers.IO) {
            try {
                val request =
                    Request
                        .Builder()
                        .url(url)
                        .header("User-Agent", UserAgents.DESKTOP_CHROME)
                        .build()
                client.newCall(request).execute().use { response ->
                    response.request.url.toString()
                }
            } catch (error: IOException) {
                null
            }
        }
    }

    private fun pageOf(url: String): Int =
        url.substringAfter('?', "")
            .split('&')
            .firstOrNull { it.startsWith("p=") }
            ?.substringAfter('=')
            ?.toIntOrNull()
            ?.coerceAtLeast(1)
            ?: 1

    private fun hostOf(url: String): String? = runCatching { URI(url).host }.getOrNull()?.lowercase()

    private companion object {
        val BV_PATTERN = Regex("""BV[0-9A-Za-z]{10}""")

        val QUALITY_LABELS =
            mapOf(
                16 to "360P",
                32 to "480P",
                64 to "720P",
                80 to "1080P",
                120 to "4K",
            )
    }
}
