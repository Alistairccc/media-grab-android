package com.joma.mediagrab.signing

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class ABogusTest {
    @Test
    fun reproducesEveryScalarFromTheBrowserSamples() {
        val samples =
            Json
                .parseToJsonElement(fixture())
                .jsonObject
                .getValue("samples")
                .jsonArray

        samples.forEachIndexed { index, element ->
            val sample = element.jsonObject
            val value = sample.getValue("a_bogus").jsonPrimitive.content
            val userAgent = sample.getValue("user_agent").jsonPrimitive.content
            val query = sample.getValue("query").jsonPrimitive.content
            val body = sample["body"]?.jsonPrimitive?.content ?: ""
            val browserInfo = sample.getValue("browser_info").jsonPrimitive.content

            assertNull(ABogus.structureError(value))
            val expected = ABogus.decode(value)
            val ours =
                ABogus(
                    userAgent = userAgent,
                    browserInfo = browserInfo,
                    random = Random(index),
                ).sign(query, body, nowMs = expected.nowMs)
            val actual = ABogus.decode(ours)

            assertEquals(expected.fields, actual.fields)
            assertEquals(expected.browserInfo, actual.browserInfo)
            assertEquals(expected.nowMs, actual.nowMs)
            assertEquals(expected.inkMs, actual.inkMs)
            assertEquals(expected.pageId, actual.pageId)
            assertEquals(expected.aid, actual.aid)
        }
    }

    private fun fixture(): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/abogus_browser.json")) {
            "missing fixture fixtures/abogus_browser.json"
        }.bufferedReader().use { it.readText() }
}
