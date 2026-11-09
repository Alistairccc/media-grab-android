package com.joma.mediagrab.parser

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult
import com.joma.mediagrab.core.util.LinkParser
import com.joma.mediagrab.signing.ABogus
import com.joma.mediagrab.signing.DouyinSession
import com.joma.mediagrab.signing.DouyinSessionProvider
import com.joma.mediagrab.signing.DouyinTokens
import com.joma.mediagrab.signing.WebSign
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
import java.net.URLDecoder
import kotlin.random.Random

class DouyinParser(
    private val client: OkHttpClient,
    private val sessionProvider: DouyinSessionProvider,
    private val baseUrl: String = "https://www.douyin.com",
    private val random: Random = Random.Default,
) : Parser {
    override fun canHandle(url: String): Boolean {
        val host = hostOf(url) ?: return false
        return host == "douyin.com" || host.endsWith(".douyin.com") ||
            host == "iesdouyin.com" || host.endsWith(".iesdouyin.com")
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
            val session =
                when (val result = sessionProvider.get()) {
                    is AppResult.Ok -> result.value
                    is AppResult.Err -> return result
                }
            requestDetail(awemeId, session)
        } catch (error: IOException) {
            Timber.w(error, "Douyin parse failed")
            AppResult.Err(AppError.Network(error))
        } catch (error: Exception) {
            Timber.w(error, "Douyin parse failed")
            AppResult.Err(AppError.Parse(error))
        }
    }

    private suspend fun resolveShortLink(url: String): String? {
        if (hostOf(url) != "v.douyin.com") {
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

    private suspend fun requestDetail(
        awemeId: String,
        session: DouyinSession,
    ): AppResult<MediaInfo> =
        withContext(Dispatchers.IO) {
            val nowMs = System.currentTimeMillis()
            val msToken = session.cookies["msToken"] ?: DouyinTokens.falseMsToken(random = random)
            val query = businessQuery(awemeId, msToken)
            val aBogus =
                ABogus(
                    userAgent = session.userAgent,
                    browserInfo = session.browserInfo,
                    random = random,
                ).sign(query, nowMs = nowMs)
            val signedQuery = "$query&a_bogus=${WebSign.encodeComponent(aBogus)}"
            val verifyFp = session.verifyFp
            val pairs =
                parseQuery(signedQuery) +
                    listOf("verifyFp" to verifyFp, "fp" to verifyFp)
            val web = WebSign.sign(pairs, session.uifid)

            val request =
                Request
                    .Builder()
                    .url("${baseUrl.trimEnd('/')}/aweme/v1/web/aweme/detail/?${web.query}")
                    .header("User-Agent", session.userAgent)
                    .header("Referer", "https://www.douyin.com/")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "identity")
                    .header("x-secsdk-web-expire", web.expire)
                    .header("x-secsdk-web-signature", web.signature)
                    .header("uifid", session.uifid)
                    .header("Cookie", cookieHeader(session, msToken))
                    .build()

            client.newCall(request).execute().use { response ->
                Timber.d("Douyin detail HTTP %d", response.code)
                if (!response.isSuccessful) {
                    return@withContext AppResult.Err(AppError.Network())
                }
                val body =
                    response.body?.string()
                        ?: return@withContext AppResult.Err(AppError.Parse())
                parseDetail(body, session.userAgent)
            }
        }

    private fun parseDetail(
        body: String,
        userAgent: String,
    ): AppResult<MediaInfo> {
        val root = Json.parseToJsonElement(body).jsonObject
        val status = root["status_code"]?.jsonPrimitive?.intOrNull
        if (status != 0) {
            Timber.w("Douyin detail status_code=%s", status)
            return AppResult.Err(AppError.Parse())
        }
        val detail =
            root["aweme_detail"] as? JsonObject
                ?: return AppResult.Err(AppError.Parse())
        val images = detail["images"] as? JsonArray
        val mediaType = detail["media_type"]?.jsonPrimitive?.intOrNull
        if (!images.isNullOrEmpty() || mediaType == MEDIA_TYPE_IMAGES) {
            return AppResult.Err(AppError.UnsupportedContent)
        }
        val video =
            detail["video"] as? JsonObject
                ?: return AppResult.Err(AppError.NoStream)
        val playAddr =
            video["play_addr"] as? JsonObject
                ?: return AppResult.Err(AppError.NoStream)
        val playUrl =
            firstUrl(playAddr)
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
            (detail["author"] as? JsonObject)
                ?.get("nickname")
                ?.jsonPrimitive
                ?.contentOrNull
                .orEmpty()
        val title = detail["desc"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()

        return AppResult.Ok(
            MediaInfo(
                platform = Platform.DOUYIN,
                title = title,
                author = author,
                coverUrl = (video["cover"] as? JsonObject)?.let(::firstUrl),
                durationMs = video["duration"]?.jsonPrimitive?.longOrNull ?: 0L,
                width = width,
                height = height,
                streams =
                    listOf(
                        Stream(
                            qualityLabel = qualityLabel,
                            url = playUrl,
                            headers =
                                mapOf(
                                    "User-Agent" to userAgent,
                                    "Referer" to "https://www.douyin.com/",
                                ),
                            sizeBytes = playAddr["data_size"]?.jsonPrimitive?.longOrNull,
                            watermarkFree = true,
                        ),
                    ),
            ),
        )
    }

    private fun businessQuery(
        awemeId: String,
        msToken: String,
    ): String =
        BUSINESS_PARAMS
            .map { (name, value) -> name to value }
            .plus("aweme_id" to awemeId)
            .plus("msToken" to msToken)
            .joinToString("&") { (name, value) ->
                "${formEncode(name)}=${formEncode(value)}"
            }

    private fun parseQuery(query: String): List<Pair<String, String>> =
        query.split("&")
            .filter { it.isNotEmpty() }
            .map { part ->
                val index = part.indexOf('=')
                val name = if (index < 0) part else part.substring(0, index)
                val value = if (index < 0) "" else part.substring(index + 1)
                decode(name) to decode(value)
            }

    private fun cookieHeader(
        session: DouyinSession,
        msToken: String,
    ): String {
        val cookies = LinkedHashMap(session.cookies)
        cookies["UIFID_TEMP"] = session.uifid
        cookies["s_v_web_id"] = session.verifyFp
        cookies["msToken"] = msToken
        return cookies.entries.joinToString("; ") { (name, value) -> "$name=$value" }
    }

    private fun extractAwemeId(url: String): String? {
        for (pattern in AWEME_ID_PATTERNS) {
            val match = pattern.find(url) ?: continue
            return match.groupValues[1]
        }
        return null
    }

    private fun firstUrl(holder: JsonObject): String? =
        (holder["url_list"] as? JsonArray)
            ?.firstNotNullOfOrNull { element -> element.jsonPrimitive.contentOrNull }

    private fun hostOf(url: String): String? = runCatching { URI(url).host }.getOrNull()?.lowercase()

    private fun formEncode(value: String): String {
        val output = StringBuilder()
        for (byte in value.toByteArray(Charsets.UTF_8)) {
            val code = byte.toInt() and 0xFF
            val char = code.toChar()
            when {
                char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' ||
                    char == '-' || char == '.' || char == '_' || char == '~' -> output.append(char)
                char == ' ' -> output.append('+')
                else -> {
                    output.append('%')
                    output.append(HEX[code ushr 4])
                    output.append(HEX[code and 0x0F])
                }
            }
        }
        return output.toString()
    }

    private fun decode(value: String): String = URLDecoder.decode(value, Charsets.UTF_8.name())

    private companion object {
        const val MEDIA_TYPE_IMAGES = 2
        const val HEX = "0123456789ABCDEF"
        const val MOBILE_USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 " +
                "(KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"

        val AWEME_ID_PATTERNS =
            listOf(
                Regex("""/(?:share/)?(?:video|note)/(\d+)"""),
                Regex("""[?&]modal_id=(\d+)"""),
            )

        val BUSINESS_PARAMS =
            listOf(
                "device_platform" to "webapp",
                "aid" to "6383",
                "channel" to "channel_pc_web",
                "pc_client_type" to "1",
                "version_code" to "190500",
                "version_name" to "19.5.0",
                "cookie_enabled" to "true",
                "browser_language" to "zh-CN",
                "browser_platform" to "Win32",
                "browser_name" to "Chrome",
                "browser_online" to "true",
                "engine_name" to "Blink",
                "os_name" to "Windows",
                "os_version" to "10",
                "platform" to "PC",
                "screen_width" to "1920",
                "screen_height" to "1080",
                "browser_version" to "120.0.0.0",
                "engine_version" to "120.0.0.0",
                "cpu_core_num" to "12",
                "device_memory" to "8",
            )
    }
}
