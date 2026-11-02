package com.joma.mediagrab.data.download

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.model.Stream
import kotlinx.coroutines.flow.toList
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
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class VideoDownloaderTest {
    private lateinit var server: MockWebServer
    private lateinit var sink: MemorySink
    private lateinit var downloader: VideoDownloader

    private val media =
        MediaInfo(
            platform = Platform.DOUYIN,
            title = "凌晨四点的城市 #4K",
            author = "城市漫游者",
        )

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        sink = MemorySink()
        downloader = VideoDownloader(OkHttpClient(), sink)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `writes, publishes and reports completion`() =
        runTest {
            server.enqueue(MockResponse().setBody("hello world"))
            val stream =
                Stream(
                    qualityLabel = "720P · 无水印",
                    url = server.url("/video.mp4").toString(),
                    headers = mapOf("Referer" to "https://www.douyin.com/"),
                )

            val progress = downloader.download(media, stream).toList()

            assertEquals("hello world", sink.bytes.toString(Charsets.UTF_8.name()))
            assertTrue(sink.published)
            assertFalse(sink.discarded)
            assertEquals(100, progress.last().percent)

            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS)) { "no request" }
            assertEquals("https://www.douyin.com/", request.getHeader("Referer"))
            assertTrue(sink.displayName.startsWith("凌晨四点的城市 #4K_"))
            assertTrue(sink.displayName.endsWith(".mp4"))
        }

    @Test
    fun `throws and keeps nothing when the server fails`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))
            val stream =
                Stream(
                    qualityLabel = "720P · 无水印",
                    url = server.url("/missing.mp4").toString(),
                )

            var failure: IOException? = null
            try {
                downloader.download(media, stream).toList()
            } catch (error: IOException) {
                failure = error
            }

            assertTrue(failure != null)
            assertFalse(sink.published)
            assertEquals(0, sink.bytes.size())
        }

    private class MemorySink : DownloadSink {
        val bytes = ByteArrayOutputStream()
        var displayName = ""
        var published = false
        var discarded = false

        override fun create(
            displayName: String,
            mime: String,
        ): DownloadFile {
            this.displayName = displayName
            return object : DownloadFile {
                override fun output(): OutputStream = bytes

                override fun publish() {
                    published = true
                }

                override fun discard() {
                    discarded = true
                }
            }
        }
    }
}
