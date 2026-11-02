package com.joma.mediagrab.parser

import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.network.UserAgents
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class BilibiliParserTest {
    private lateinit var server: MockWebServer
    private lateinit var parser: BilibiliParser

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        parser =
            BilibiliParser(
                client = OkHttpClient(),
                apiBase = server.url("/").toString().trimEnd('/'),
            )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `parses a 720p mp4`() =
        runTest {
            enqueueBody(fixture("bili_view.json"))
            enqueueBody(fixture("bili_playurl.json"))
            val view = json("bili_view.json").getValue("data").jsonObject
            val play = json("bili_playurl.json").getValue("data").jsonObject
            val expectedUrl =
                play.getValue("durl")
                    .jsonArray[0]
                    .jsonObject
                    .getValue("url")
                    .jsonPrimitive
                    .contentOrNull

            val result = parser.parse("https://www.bilibili.com/video/BV1egpo6GEH3?t=2.2")

            val media = (result as AppResult.Ok).value
            assertEquals(Platform.BILIBILI, media.platform)
            assertEquals(view.getValue("title").jsonPrimitive.contentOrNull, media.title)
            assertEquals(
                view.getValue("owner").jsonObject.getValue("name").jsonPrimitive.contentOrNull,
                media.author,
            )
            assertEquals(198_000L, media.durationMs)
            assertEquals(1920, media.width)
            assertEquals(1080, media.height)
            assertTrue(media.coverUrl.orEmpty().startsWith("https://"))
            val stream = media.streams.single()
            assertEquals("720P", stream.qualityLabel)
            assertEquals(expectedUrl, stream.url)
            assertEquals(9_127_880L, stream.sizeBytes)
            assertEquals(UserAgents.DESKTOP_CHROME, stream.headers["User-Agent"])
            assertEquals("https://www.bilibili.com/", stream.headers["Referer"])

            val viewRequest = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS)) { "no view request" }
            assertEquals("/x/web-interface/view?bvid=BV1egpo6GEH3", viewRequest.path)
            assertEquals(
                "https://www.bilibili.com/video/BV1egpo6GEH3",
                viewRequest.getHeader("Referer"),
            )
            val playRequest = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS)) { "no playurl request" }
            assertEquals(
                "/x/player/playurl?bvid=BV1egpo6GEH3&cid=42680520495&qn=64&fnval=1&fourk=0",
                playRequest.path,
            )
            assertEquals(UserAgents.DESKTOP_CHROME, playRequest.getHeader("User-Agent"))
        }

    @Test
    fun `selects the requested page`() =
        runTest {
            val view = json("bili_view.json").getValue("data").jsonObject
            val firstPage = view.getValue("pages").jsonArray[0].jsonObject
            assertEquals(1, firstPage.getValue("page").jsonPrimitive.intOrNull)
            assertEquals(42680520495L, firstPage.getValue("cid").jsonPrimitive.longOrNull)

            enqueueBody(fixture("bili_view.json"))

            val result = parser.parse("https://www.bilibili.com/video/BV1egpo6GEH3?p=2")

            assertTrue((result as AppResult.Err).error is AppError.Parse)
            assertEquals(1, server.requestCount)
        }

    @Test
    fun `maps an api error to a parse error`() =
        runTest {
            enqueueBody("""{"code":-404,"message":"啥都木有"}""")

            val result = parser.parse("https://www.bilibili.com/video/BV1egpo6GEH3")

            assertTrue((result as AppResult.Err).error is AppError.Parse)
        }

    @Test
    fun `reports no stream when the response has no durl`() =
        runTest {
            enqueueBody(fixture("bili_view.json"))
            enqueueBody("""{"code":0,"data":{"quality":64}}""")

            val result = parser.parse("https://www.bilibili.com/video/BV1egpo6GEH3")

            assertEquals(AppError.NoStream, (result as AppResult.Err).error)
        }

    @Test
    fun `handles only bilibili hosts`() {
        assertTrue(parser.canHandle("https://b23.tv/abc123"))
        assertTrue(parser.canHandle("https://www.bilibili.com/video/BV1egpo6GEH3"))
        assertTrue(parser.canHandle("https://m.bilibili.com/video/BV1egpo6GEH3"))
        assertFalse(parser.canHandle("https://v.douyin.com/Jo9_lP54-AU/"))
    }

    private fun enqueueBody(body: String) {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(body),
        )
    }

    private fun json(name: String): JsonObject = Json.parseToJsonElement(fixture(name)).jsonObject

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/$name")) {
            "missing fixture fixtures/$name"
        }.bufferedReader().use { it.readText() }
}
