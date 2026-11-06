package com.joma.mediagrab.parser

import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult
import kotlinx.coroutines.test.runTest
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

class TikTokParserTest {
    private lateinit var server: MockWebServer
    private lateinit var parser: TikTokParser

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        parser =
            TikTokParser(
                client = OkHttpClient(),
                webBase = server.url("/").toString().trimEnd('/'),
            )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `parses a video page`() =
        runTest {
            enqueueHtml(fixture("tiktok_page.html"))

            val result = parser.parse("https://www.tiktok.com/@scout2015/video/6718335390845095173")

            val media = (result as AppResult.Ok).value
            assertEquals(Platform.TIKTOK, media.platform)
            assertEquals("A dog on the beach #dog", media.title)
            assertEquals("scout2015", media.author)
            assertEquals(15_000L, media.durationMs)
            assertEquals(720, media.width)
            assertEquals(1280, media.height)
            assertEquals(
                "https://p16-sign.tiktokcdn.com/obj/tos-maliva-p-0068/cover.jpeg",
                media.coverUrl,
            )

            val stream = media.streams.single()
            assertEquals("720P", stream.qualityLabel)
            assertEquals("https://v16-webapp-prime.tiktok.com/video/tos/example.mp4", stream.url)
            assertTrue(stream.headers["User-Agent"].orEmpty().startsWith("Mozilla/5.0 (iPhone"))
            assertEquals(
                server.url("/@/video/6718335390845095173").toString(),
                stream.headers["Referer"],
            )

            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS)) { "no request" }
            assertEquals("/@/video/6718335390845095173", request.path)
            assertTrue(request.getHeader("User-Agent").orEmpty().startsWith("Mozilla/5.0 (iPhone"))
        }

    @Test
    fun `attaches session cookies to the stream`() =
        runTest {
            val base = server.url("/").toString().trimEnd('/')
            val html =
                fixture("tiktok_page.html").replace(
                    "https://v16-webapp-prime.tiktok.com/video/tos/example.mp4",
                    "$base/video/tos/example.mp4",
                )
            server.enqueue(
                MockResponse()
                    .setHeader("Content-Type", "text/html; charset=utf-8")
                    .setHeader("Set-Cookie", "ttwid=session-value; Path=/")
                    .setBody(html),
            )

            val result = parser.parse("https://www.tiktok.com/@user/video/123456789")

            val stream = (result as AppResult.Ok).value.streams.single()
            assertEquals("$base/video/tos/example.mp4", stream.url)
            assertEquals("ttwid=session-value", stream.headers["Cookie"])
        }

    @Test
    fun `falls back to the download address`() =
        runTest {
            enqueueHtml(
                """
                <html><body><script id="api-data" type="application/json">
                {"videoDetail":{"statusCode":0,"itemInfo":{"itemStruct":{"desc":"x",
                "video":{"width":720,"height":1280,"duration":9,
                "downloadAddr":"https://v16-webapp-prime.tiktok.com/video/tos/fallback.mp4"}}}}}
                </script></body></html>
                """.trimIndent(),
            )

            val result = parser.parse("https://www.tiktok.com/@user/video/123456789")

            val media = (result as AppResult.Ok).value
            assertEquals(
                "https://v16-webapp-prime.tiktok.com/video/tos/fallback.mp4",
                media.streams.single().url,
            )
        }

    @Test
    fun `rejects image posts`() =
        runTest {
            enqueueHtml(
                """
                <html><body><script id="api-data" type="application/json">
                {"videoDetail":{"statusCode":0,"itemInfo":{"itemStruct":{"desc":"x",
                "imagePost":{"infoList":[{"imageURL":{"urlList":["https://example.com/1.jpeg"]}}]}}}}}
                </script></body></html>
                """.trimIndent(),
            )

            val result = parser.parse("https://www.tiktok.com/@user/video/123456789")

            assertEquals(AppError.UnsupportedContent, (result as AppResult.Err).error)
        }

    @Test
    fun `rejects a non zero status code`() =
        runTest {
            enqueueHtml(
                """
                <html><body><script id="api-data" type="application/json">
                {"videoDetail":{"statusCode":10204,"statusMessage":"video not found"}}
                </script></body></html>
                """.trimIndent(),
            )

            val result = parser.parse("https://www.tiktok.com/@user/video/123456789")

            assertTrue((result as AppResult.Err).error is AppError.Parse)
        }

    @Test
    fun `rejects a page without embedded data`() =
        runTest {
            enqueueHtml("<html><body>Please wait...</body></html>")

            val result = parser.parse("https://www.tiktok.com/@user/video/123456789")

            assertTrue((result as AppResult.Err).error is AppError.Parse)
        }

    @Test
    fun `rejects an item without a play address`() =
        runTest {
            enqueueHtml(
                """
                <html><body><script id="api-data" type="application/json">
                {"videoDetail":{"statusCode":0,"itemInfo":{"itemStruct":{"desc":"x",
                "video":{"width":720,"height":1280}}}}}
                </script></body></html>
                """.trimIndent(),
            )

            val result = parser.parse("https://www.tiktok.com/@user/video/123456789")

            assertEquals(AppError.NoStream, (result as AppResult.Err).error)
        }

    @Test
    fun `maps http failures to a network error`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(429).setBody("ratelimit triggered"))

            val result = parser.parse("https://www.tiktok.com/@user/video/123456789")

            assertTrue((result as AppResult.Err).error is AppError.Network)
        }

    @Test
    fun `handles only tiktok hosts`() {
        assertTrue(parser.canHandle("https://vm.tiktok.com/ZMabc123/"))
        assertTrue(parser.canHandle("https://www.tiktok.com/@scout2015/video/6718335390845095173"))
        assertTrue(parser.canHandle("https://m.tiktok.com/v/6718335390845095173"))
        assertFalse(parser.canHandle("https://www.douyin.com/video/1"))
        assertFalse(parser.canHandle("https://www.bilibili.com/video/BV1egpo6GEH3"))
    }

    private fun enqueueHtml(body: String) {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "text/html; charset=utf-8")
                .setBody(body),
        )
    }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/$name")) {
            "missing fixture fixtures/$name"
        }.bufferedReader().use { it.readText() }
}
