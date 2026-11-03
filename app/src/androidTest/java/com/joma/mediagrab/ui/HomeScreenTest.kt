package com.joma.mediagrab.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Platform
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.data.MediaRepository
import com.joma.mediagrab.download.DownloadController
import com.joma.mediagrab.download.DownloadState
import com.joma.mediagrab.parser.FakeParser
import com.joma.mediagrab.ui.components.LINK_INPUT_TAG
import com.joma.mediagrab.ui.components.PARSE_BUTTON_TAG
import com.joma.mediagrab.ui.components.VideoCard
import com.joma.mediagrab.ui.home.HomeScreen
import com.joma.mediagrab.ui.home.HomeViewModel
import com.joma.mediagrab.ui.theme.MediaGrabTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertTrue
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
    fun emptyStateShowsInputAndHint() {
        val viewModel = viewModel()
        composeRule.setContent {
            MediaGrabTheme {
                HomeScreen(viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("粘贴或输入抖音 / B站链接").assertExists()
        composeRule.onNodeWithText("复制分享链接，或直接输入链接").assertExists()
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
        waitForResultCard()
        composeRule.onNodeWithText("720P · 无水印").assertExists()
    }

    @Test
    fun typedLinkParsesFromTheParseButton() {
        val viewModel = viewModel()
        composeRule.setContent {
            MediaGrabTheme {
                HomeScreen(viewModel = viewModel)
            }
        }

        composeRule.onNodeWithTag(LINK_INPUT_TAG).performTextInput(
            "https://www.bilibili.com/video/BV1egpo6GEH3",
        )
        composeRule.onNodeWithTag(PARSE_BUTTON_TAG).performClick()
        waitForResultCard()
    }

    @Test
    fun doneCardShowsOnlyTheSavedMessage() {
        val stream = Stream(qualityLabel = "720P · 无水印", url = "https://example.com/v.mp4")
        val media =
            MediaInfo(
                platform = Platform.DOUYIN,
                title = "测试视频",
                author = "作者",
                streams = listOf(stream),
            )

        composeRule.setContent {
            MediaGrabTheme {
                VideoCard(
                    media = media,
                    selectedStream = stream,
                    isDownloading = false,
                    isDone = true,
                    percent = 100,
                    bytesPerSecond = 0,
                    remainingSeconds = null,
                    onQualityClick = {},
                    onSave = {},
                    onCancel = {},
                )
            }
        }

        composeRule.onNodeWithText("已保存完成").assertExists()
        assertTrue(composeRule.onAllNodesWithText("查看").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithText("打开相册").fetchSemanticsNodes().isEmpty())
    }

    private fun waitForResultCard() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithText("凌晨四点的城市，原来是这样一点点苏醒的｜4K 延时记录")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
