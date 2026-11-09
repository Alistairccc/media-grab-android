package com.joma.mediagrab.parser

import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult
import com.joma.mediagrab.signing.ABogus
import com.joma.mediagrab.signing.DouyinSession
import com.joma.mediagrab.signing.StaticDouyinSessionProvider
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DouyinParserTest {
    private lateinit var server: MockWebServer
    private lateinit var parser: DouyinParser

    private val session =
        DouyinSession(
            uifid = "0123456789abcdef".repeat(10),
            verifyFp = "verify_abc123_def456",
            userAgent = DouyinSession.DEFAULT_USER_AGENT,
            cookies =
                mapOf(
                    "UIFID_TEMP" to "0123456789abcdef".repeat(10),
                    "s_v_web_id" to "verify_abc123_def456",
                    "ttwid" to "ttwid-value",
                    "odin_tt" to "odin-value",
                ),
        )

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        parser =
            DouyinParser(
                client = OkHttpClient(),
                sessionProvider = StaticDouyinSessionProvider(session),
                baseUrl = server.url("/").toString().trimEnd('/'),
                random = Random(7),
            )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `parses a video detail and signs the request`() =
        runTest {
            enqueueBody(fixture("douyin_detail.json"))

            val result = parser.parse("https://www.douyin.com/video/7700027115164960235?previous_page=app_code_link")

            val media = (result as AppResult.Ok).value
            assertEquals(Platform.DOUYIN, media.platform)
            assertEquals("凌晨四点的城市，原来是这样一点点苏醒的 #城市漫游 #4K", media.title)
            assertEquals("城市漫游者", media.author)
            assertEquals(105267L, media.durationMs)
            assertEquals(1080, media.width)
            assertEquals(1920, media.height)
            assertEquals("https://p3-pc-sign.douyinpic.com/image-cut-tos-priv/cover.jpeg", media.coverUrl)

            val stream = media.streams.single()
            assertEquals("1080P", stream.qualityLabel)
            assertTrue(stream.watermarkFree)
            assertEquals("https://v26-weba.douyinvod.com/video/tos/example.mp4?a=6383", stream.url)
            assertEquals(20_117_456L, stream.sizeBytes)
            assertEquals(DouyinSession.DEFAULT_USER_AGENT, stream.headers["User-Agent"])
            assertEquals("https://www.douyin.com/", stream.headers["Referer"])

            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS)) { "no request" }
            val url = request.requestUrl!!
            assertEquals("7700027115164960235", url.queryParameter("aweme_id"))
            assertEquals("6383", url.queryParameter("aid"))
            assertNotNull(url.queryParameter("msToken"))
            assertNotNull(url.queryParameter("uifid"))
            assertNotNull(url.queryParameter("timestamp"))
            assertNotNull(url.queryParameter("verifyFp"))
            assertNotNull(url.queryParameter("fp"))

            val aBogus = url.queryParameter("a_bogus")
            assertNotNull(aBogus)
            assertEquals(null, ABogus.structureError(aBogus!!))

            val signature = url.queryParameter("x-secsdk-web-signature")
            assertNotNull(signature)
            assertEquals(signature, request.getHeader("x-secsdk-web-signature"))
            assertEquals(url.queryParameter("timestamp"), request.getHeader("x-secsdk-web-expire"))
            assertEquals(session.uifid, request.getHeader("uifid"))
            assertEquals(DouyinSession.DEFAULT_USER_AGENT, request.getHeader("User-Agent"))
            assertEquals("https://www.douyin.com/", request.getHeader("Referer"))

            val cookie = request.getHeader("Cookie").orEmpty()
            assertTrue(cookie.contains("UIFID_TEMP=${session.uifid}"))
            assertTrue(cookie.contains("s_v_web_id=${session.verifyFp}"))
            assertTrue(cookie.contains("ttwid=ttwid-value"))
            assertTrue(cookie.contains("odin_tt=odin-value"))
        }

    @Test
    fun `accepts a modal id url`() =
        runTest {
            enqueueBody(fixture("douyin_detail.json"))

            val result = parser.parse("https://www.douyin.com/discover?modal_id=7700027115164960235")

            assertTrue(result is AppResult.Ok)
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS)) { "no request" }
            assertEquals("7700027115164960235", request.requestUrl!!.queryParameter("aweme_id"))
        }

    @Test
    fun `rejects image posts`() =
        runTest {
            enqueueBody(
                """
                {"status_code":0,"aweme_detail":{"aweme_id":"1","media_type":2,
                "images":[{"url_list":["https://example.com/1.jpeg"]}]}}
                """.trimIndent(),
            )

            val result = parser.parse("https://www.douyin.com/note/1")

            assertEquals(AppError.UnsupportedContent, (result as AppResult.Err).error)
        }

    @Test
    fun `rejects a non zero status code`() =
        runTest {
            enqueueBody("""{"status_code":2154,"status_msg":"blocked"}""")

            val result = parser.parse("https://www.douyin.com/video/7700027115164960235")

            assertTrue((result as AppResult.Err).error is AppError.Parse)
        }

    @Test
    fun `rejects a detail without a play address`() =
        runTest {
            enqueueBody(
                """
                {"status_code":0,"aweme_detail":{"aweme_id":"1","desc":"x",
                "video":{"width":1080,"height":1920}}}
                """.trimIndent(),
            )

            val result = parser.parse("https://www.douyin.com/video/7700027115164960235")

            assertEquals(AppError.NoStream, (result as AppResult.Err).error)
        }

    @Test
    fun `maps http failures to a network error`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(403).setBody("blocked"))

            val result = parser.parse("https://www.douyin.com/video/7700027115164960235")

            assertTrue((result as AppResult.Err).error is AppError.Network)
        }

    @Test
    fun `handles only douyin hosts`() {
        assertTrue(parser.canHandle("https://v.douyin.com/Jo9_lP54-AU/"))
        assertTrue(parser.canHandle("https://www.iesdouyin.com/share/video/1/"))
        assertFalse(parser.canHandle("https://www.bilibili.com/video/BV1egpo6GEH3"))
    }

    private fun enqueueBody(body: String) {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(body),
        )
    }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/$name")) {
            "missing fixture fixtures/$name"
        }.bufferedReader().use { it.readText() }
}
