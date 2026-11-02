package com.joma.mediagrab.download

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.data.download.DownloadProgress
import com.joma.mediagrab.data.download.Downloader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadManagerTest {
    private val media =
        MediaInfo(
            platform = Platform.DOUYIN,
            title = "测试视频",
            author = "作者",
        )
    private val stream =
        Stream(
            qualityLabel = "720P",
            url = "https://example.com/video.mp4",
            sizeBytes = 1_000_000,
        )

    @Test
    fun `completes the download`() =
        runTest {
            var started = false
            val manager =
                manager(
                    downloader =
                        Downloader { _, _ ->
                            flow {
                                emit(DownloadProgress(percent = 50))
                                emit(DownloadProgress(percent = 100))
                            }
                        },
                    startService = { started = true },
                )

            manager.start(media, stream)
            assertTrue(started)
            manager.execute()
            advanceUntilIdle()

            assertTrue(manager.state.value is DownloadState.Completed)
        }

    @Test
    fun `cancel stops the download and returns to cancelled`() =
        runTest {
            val manager =
                manager(
                    downloader =
                        Downloader { _, _ ->
                            flow {
                                emit(DownloadProgress(percent = 10))
                                awaitCancellation()
                            }
                        },
                )

            manager.start(media, stream)
            manager.execute()
            runCurrent()
            assertTrue(manager.state.value is DownloadState.Running)

            manager.cancel()
            advanceUntilIdle()

            assertTrue(manager.state.value is DownloadState.Cancelled)
        }

    @Test
    fun `download failure is reported`() =
        runTest {
            val manager =
                manager(
                    downloader =
                        Downloader { _, _ ->
                            flow { throw IOException("boom") }
                        },
                )

            manager.start(media, stream)
            manager.execute()
            advanceUntilIdle()

            val state = manager.state.value
            assertTrue(state is DownloadState.Failed)
            assertTrue((state as DownloadState.Failed).error is AppError.Download)
        }

    private fun kotlinx.coroutines.test.TestScope.manager(
        downloader: Downloader,
        startService: () -> Unit = {},
    ): DownloadManager =
        DownloadManager(
            downloader = downloader,
            scope = this,
            startService = startService,
        )
}
