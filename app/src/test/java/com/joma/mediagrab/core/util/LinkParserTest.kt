package com.joma.mediagrab.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkParserTest {
    @Test
    fun extractsSupportedUrlFromShareText() {
        val text = "复制打开抖音，看看这个视频 https://v.douyin.com/iRNB7xQe/ 更多精彩"
        assertEquals("https://v.douyin.com/iRNB7xQe/", LinkParser.extractFirstSupported(text))
    }

    @Test
    fun trimsChinesePunctuation() {
        val text = "看这个：https://b23.tv/abc123）。"
        assertEquals("https://b23.tv/abc123", LinkParser.extractFirstSupported(text))
    }

    @Test
    fun ignoresUnsupportedHosts() {
        assertNull(LinkParser.extractFirstSupported("https://example.com/video/1"))
        assertFalse(LinkParser.isSupported("https://example.com/video/1"))
    }

    @Test
    fun acceptsSubdomains() {
        assertTrue(LinkParser.isSupported("https://www.douyin.com/video/123"))
        assertTrue(LinkParser.isSupported("https://www.bilibili.com/video/BV1xx411c7mD"))
        assertTrue(LinkParser.isSupported("https://vm.tiktok.com/ZMabc123/"))
        assertTrue(LinkParser.isSupported("https://www.tiktok.com/@user/video/123"))
    }

    @Test
    fun extractsTikTokUrlFromShareText() {
        val text = "Watch this https://www.tiktok.com/@user/video/6718335390845095173?is_from_webapp=1"
        assertEquals(
            "https://www.tiktok.com/@user/video/6718335390845095173?is_from_webapp=1",
            LinkParser.extractFirstSupported(text),
        )
    }
}
