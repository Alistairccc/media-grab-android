package com.joma.mediagrab.ui.home

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.data.MediaRepository
import com.joma.mediagrab.data.download.DownloadProgress
import com.joma.mediagrab.download.DownloadController
import com.joma.mediagrab.download.DownloadState
import com.joma.mediagrab.parser.FakeParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val controller = FakeDownloadController()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun invalidLinkShowsError() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.onLinkChange("not a link")

            viewModel.onParse()

            val state = viewModel.uiState.value
            assertTrue(state is HomeUiState.Failed)
            assertEquals(AppError.InvalidLink, (state as HomeUiState.Failed).error)
            assertEquals("not a link", viewModel.link.value)
        }

    @Test
    fun validLinkParsesToReady() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.onLinkChange("https://v.douyin.com/abc123/")

            viewModel.onParse()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is HomeUiState.Ready)
            assertEquals(
                "720P · 无水印",
                (state as HomeUiState.Ready).selectedStream.qualityLabel,
            )
        }

    @Test
    fun parseTypedLink() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.onLinkChange("https://www.bilibili.com/video/BV1egpo6GEH3")

            assertTrue(viewModel.uiState.value is HomeUiState.Idle)
            assertEquals(
                "https://www.bilibili.com/video/BV1egpo6GEH3",
                viewModel.link.value,
            )

            viewModel.onParse()
            assertTrue(viewModel.uiState.value is HomeUiState.Parsing)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is HomeUiState.Ready)
        }

    @Test
    fun parseInvalidTextKeepsItAndShowsError() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.onLinkChange("not a link")

            viewModel.onParse()

            assertEquals(AppError.InvalidLink, (viewModel.uiState.value as HomeUiState.Failed).error)
            assertEquals("not a link", viewModel.link.value)
        }

    @Test
    fun editingAfterReadyClearsTheStaleResult() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.onLinkChange("https://v.douyin.com/abc123/")
            viewModel.onParse()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is HomeUiState.Ready)

            viewModel.onLinkChange("https://v.douyin.com/new/")

            assertTrue(viewModel.uiState.value is HomeUiState.Idle)
        }

    @Test
    fun saveShowsDownloadingThenDone() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.onLinkChange("https://v.douyin.com/abc123/")
            viewModel.onParse()
            advanceUntilIdle()
            viewModel.onSave()
            assertTrue(viewModel.uiState.value is HomeUiState.Downloading)

            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is HomeUiState.Downloading)

            controller.complete()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is HomeUiState.Done)
        }

    @Test
    fun cancelReturnsToReady() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.onLinkChange("https://v.douyin.com/abc123/")
            viewModel.onParse()
            advanceUntilIdle()
            viewModel.onSave()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is HomeUiState.Downloading)
            viewModel.onCancel()
            advanceUntilIdle()
            val state = viewModel.uiState.value
            assertTrue(state is HomeUiState.Ready)
            assertEquals(
                "720P · 无水印",
                (state as HomeUiState.Ready).selectedStream.qualityLabel,
            )
        }

    private fun viewModel() = HomeViewModel(MediaRepository(FakeParser()), controller)

    private class FakeDownloadController : DownloadController {
        private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
        override val state: StateFlow<DownloadState> = _state

        override fun start(
            media: MediaInfo,
            stream: Stream,
        ) {
            _state.value = DownloadState.Running(media, stream, DownloadProgress(0))
        }

        override fun cancel() {
            val running = _state.value as? DownloadState.Running ?: return
            _state.value = DownloadState.Cancelled(running.media, running.stream)
        }

        fun complete() {
            val running = _state.value as? DownloadState.Running ?: return
            _state.value = DownloadState.Completed(running.media, running.stream)
        }
    }
}
