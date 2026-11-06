package com.joma.mediagrab.core.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Session cookie store for parsers whose media CDN requires the cookies issued
 * by the page request. Kept per parser so other platforms stay unaffected.
 */
class MemoryCookieJar : CookieJar {
    private val cookies = mutableListOf<Cookie>()

    @Synchronized
    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>,
    ) {
        this.cookies.removeAll { stored ->
            cookies.any { it.name == stored.name && it.domain == stored.domain && it.path == stored.path }
        }
        this.cookies.addAll(cookies)
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        cookies.removeAll { it.expiresAt <= System.currentTimeMillis() }
        return cookies.filter { it.matches(url) }
    }

    @Synchronized
    fun headerFor(url: HttpUrl): String? {
        val matches = loadForRequest(url)
        return if (matches.isEmpty()) {
            null
        } else {
            matches.joinToString("; ") { "${it.name}=${it.value}" }
        }
    }
}
