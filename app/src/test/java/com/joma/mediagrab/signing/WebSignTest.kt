package com.joma.mediagrab.signing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebSignTest {
    @Test
    fun matchesTheReferenceVector() {
        val signed =
            WebSign.sign(
                pairs = listOf("a" to "1", "b" to "2"),
                uifid = "test-uifid",
                timestamp = 1_700_000_000,
            )
        assertEquals(
            "a=1&b=2&uifid=test-uifid&timestamp=1700000000" +
                "&x-secsdk-web-signature=d717f66297fe2472bf1645872cbeb191",
            signed.query,
        )
        assertEquals("1700000000", signed.expire)
        assertEquals("d717f66297fe2472bf1645872cbeb191", signed.signature)
    }

    @Test
    fun encodesSpacesAndSpecials() {
        assertEquals(
            "a%20b%2Bc%3Dd*-._",
            WebSign.encodeComponent("a b+c=d*-._"),
        )
    }

    @Test
    fun picksTheVisitorIdFromAnyKnownCookieName() {
        assertEquals("v", WebSign.pickUifid(mapOf("UIFID_TEMP" to "v")))
        assertEquals("v", WebSign.pickUifid(mapOf("uifid" to "v")))
        assertNull(WebSign.pickUifid(emptyMap()))
    }
}
