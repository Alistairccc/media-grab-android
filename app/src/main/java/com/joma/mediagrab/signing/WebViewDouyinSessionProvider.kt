package com.joma.mediagrab.signing

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Mints the browser cookies Douyin's signed API needs.
 *
 * The visitor id is only issued to a real browser, so a hidden WebView loads
 * Douyin once and we reuse the cookies it stores. All request signing and API
 * calls stay native; the WebView never scrapes page content.
 */
class WebViewDouyinSessionProvider(
    private val context: Context,
    private val timeoutMs: Long = 20_000,
) : DouyinSessionProvider {
    private val mutex = Mutex()

    @Volatile
    private var cached: DouyinSession? = null

    override suspend fun get(): AppResult<DouyinSession> =
        mutex.withLock {
            cached?.let { return AppResult.Ok(it) }

            val stored = withContext(Dispatchers.IO) { readCookies() }
            stored.toSession()?.let { session ->
                Timber.d("Douyin guest session reused")
                cached = session
                return AppResult.Ok(session)
            }

            val minted = withContext(Dispatchers.Main) { mintCookies() }
            val session = minted.toSession()
            if (session == null) {
                Timber.w("Douyin guest session cookies missing")
                return AppResult.Err(
                    AppError.Parse(IllegalStateException("Douyin session cookies were not issued")),
                )
            }
            Timber.d("Douyin guest session minted")
            cached = session
            AppResult.Ok(session)
        }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun mintCookies(): Map<String, String> {
        val cookies = CookieManager.getInstance()
        cookies.setAcceptCookie(true)
        val webView =
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = DouyinSession.DEFAULT_USER_AGENT
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                webViewClient = WebViewClient()
            }
        cookies.setAcceptThirdPartyCookies(webView, true)

        try {
            webView.loadUrl(HOME_URL)
            val deadline = SystemClock.uptimeMillis() + timeoutMs
            while (SystemClock.uptimeMillis() < deadline) {
                delay(POLL_INTERVAL_MS)
                val current = readCookies()
                if (current.isComplete()) {
                    cookies.flush()
                    return current
                }
            }
            return readCookies()
        } finally {
            webView.stopLoading()
            webView.destroy()
        }
    }

    private fun readCookies(): Map<String, String> {
        val header = CookieManager.getInstance().getCookie(HOME_URL) ?: return emptyMap()
        return header
            .split(';')
            .mapNotNull { part ->
                val index = part.indexOf('=')
                if (index <= 0) {
                    null
                } else {
                    part.substring(0, index).trim() to part.substring(index + 1).trim()
                }
            }.toMap()
    }

    private fun Map<String, String>.toSession(): DouyinSession? {
        val uifid = WebSign.pickUifid(this) ?: return null
        val verifyFp = this["s_v_web_id"] ?: return null
        return DouyinSession(
            uifid = uifid,
            verifyFp = verifyFp,
            userAgent = DouyinSession.DEFAULT_USER_AGENT,
            cookies = this,
        )
    }

    private fun Map<String, String>.isComplete(): Boolean =
        WebSign.pickUifid(this) != null &&
            !this["s_v_web_id"].isNullOrEmpty() &&
            !this["ttwid"].isNullOrEmpty()

    private companion object {
        const val HOME_URL = "https://www.douyin.com/"
        const val POLL_INTERVAL_MS = 250L
    }
}
