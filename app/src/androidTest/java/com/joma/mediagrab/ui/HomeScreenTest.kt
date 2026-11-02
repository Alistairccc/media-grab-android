package com.joma.mediagrab.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.data.MediaRepository
import com.joma.mediagrab.download.DownloadController
import com.joma.mediagrab.download.DownloadState
import com.joma.mediagrab.parser.FakeParser
import com.joma.mediagrab.ui.home.HomeScreen
import com.joma.mediagrab.ui.home.HomeViewModel
import com.joma.mediagrab.ui.theme.MediaGrabTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val downloadController =
        object : DownloadController {
            override val state: StateFlow<DownloadState> = MutableStateFlow(DownloadState.Idle)

            override fun start(
                media: MediaInfo,
                stream: Stream,
            ) = Unit

            override fun cancel() = Unit
        }

    private fun viewModel() = HomeViewModel(MediaRepository(FakeParser()), downloadController)

    @Test
    fun emptyStateShowsPasteHint() {
        val viewModel = viewModel()
        composeRule.setContent {
            MediaGrabTheme {
                HomeScreen(viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("粘贴抖音 / B站分享链接").assertExists()
        composeRule.onNodeWithText("复制分享链接，点上方「粘贴」").assertExists()
    }

    @Test
    fun pasteShowsResultCard() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText("link", "https://v.douyin.com/abc123/"),
        )

        val viewModel = viewModel()
        composeRule.setContent {
            MediaGrabTheme {
                HomeScreen(viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("粘贴").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithText("凌晨四点的城市，原来是这样一点点苏醒的｜4K 延时记录")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("720P · 无水印").assertExists()
    }
}
