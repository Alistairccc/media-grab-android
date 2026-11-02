package com.joma.mediagrab.signing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class Sm3Test {
    @Test
    fun matchesPublishedVectorAbc() {
        assertEquals(
            "66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0",
            hex(Sm3.hash("abc".toByteArray())),
        )
    }

    @Test
    fun matchesPublishedVectorAbcd16() {
        assertEquals(
            "debe9ff92275b8a138604889c18e5a4d6fdb70e5387e5765293dcba39c0c5732",
            hex(Sm3.hash("abcd".repeat(16).toByteArray())),
        )
    }

    @Test
    fun hashChangesAcrossBlockBoundaries() {
        assertNotEquals(
            hex(Sm3.hash(ByteArray(55) { 0x78 })),
            hex(Sm3.hash(ByteArray(56) { 0x78 })),
        )
    }

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xFF) }
}
