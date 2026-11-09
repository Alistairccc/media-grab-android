package com.joma.mediagrab.di

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.joma.mediagrab.core.network.HttpClientFactory
import com.joma.mediagrab.data.MediaRepository
import com.joma.mediagrab.data.download.MediaStoreSink
import com.joma.mediagrab.data.download.VideoDownloader
import com.joma.mediagrab.data.settings.LanguagePreference
import com.joma.mediagrab.data.settings.ThemePreference
import com.joma.mediagrab.download.DownloadManager
import com.joma.mediagrab.download.DownloadService
import com.joma.mediagrab.download.PendingMediaCleaner
import com.joma.mediagrab.parser.BilibiliParser
import com.joma.mediagrab.parser.CompositeParser
import com.joma.mediagrab.parser.DouyinParser
import com.joma.mediagrab.parser.Parser
import com.joma.mediagrab.parser.TikTokParser
import com.joma.mediagrab.signing.WebViewDouyinSessionProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val client = HttpClientFactory.create()
    private val parser: Parser =
        CompositeParser(
            listOf(
                DouyinParser(
                    client = client,
                    sessionProvider = WebViewDouyinSessionProvider(appContext),
                ),
                BilibiliParser(client),
                TikTokParser(client),
            ),
        )

    val repository: MediaRepository = MediaRepository(parser)

    val themePreference = ThemePreference(appContext)

    val languagePreference = LanguagePreference(appContext)

    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val downloadManager: DownloadManager =
        DownloadManager(
            downloader = VideoDownloader(client, MediaStoreSink(appContext)),
            scope = downloadScope,
            startService = {
                ContextCompat.startForegroundService(
                    appContext,
                    Intent(appContext, DownloadService::class.java),
                )
            },
        )

    init {
        downloadScope.launch { PendingMediaCleaner(appContext).cleanup() }
    }
}
