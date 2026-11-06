package com.joma.mediagrab.core.network

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MemoryCookieJarTest {
    private val jar = MemoryCookieJar()
    private val page = "https://www.tiktok.com/@user/video/1".toHttpUrl()
    private val cdn = "https://v16-webapp-prime.tiktok.com/video/tos/example.mp4".toHttpUrl()

    @Test
    fun `returns cookies for a matching subdomain`() {
        jar.saveFromResponse(page, listOf(cookie("ttwid", "abc", "tiktok.com")))

        assertEquals("ttwid=abc", jar.headerFor(cdn))
    }

    @Test
    fun `joins multiple cookies`() {
        jar.saveFromResponse(
            page,
            listOf(
                cookie("ttwid", "abc", "tiktok.com"),
                cookie("msToken", "xyz", "tiktok.com"),
            ),
        )

        assertEquals("ttwid=abc; msToken=xyz", jar.headerFor(cdn))
    }

    @Test
    fun `does not return cookies for other hosts`() {
        jar.saveFromResponse(page, listOf(cookie("ttwid", "abc", "tiktok.com")))

        assertNull(jar.headerFor("https://www.douyin.com/".toHttpUrl()))
    }

    @Test
    fun `drops expired cookies`() {
        jar.saveFromResponse(
            page,
            listOf(cookie("ttwid", "abc", "tiktok.com", expiresAt = System.currentTimeMillis() - 1_000)),
        )

        assertNull(jar.headerFor(cdn))
    }

    @Test
    fun `replaces a cookie with the same name and domain`() {
        jar.saveFromResponse(page, listOf(cookie("ttwid", "old", "tiktok.com")))
        jar.saveFromResponse(page, listOf(cookie("ttwid", "new", "tiktok.com")))

        assertEquals("ttwid=new", jar.headerFor(cdn))
    }

    private fun cookie(
        name: String,
        value: String,
        domain: String,
        expiresAt: Long = System.currentTimeMillis() + 60_000,
    ): Cookie =
        Cookie
            .Builder()
            .domain(domain)
            .name(name)
            .value(value)
            .path("/")
            .expiresAt(expiresAt)
            .build()
}
