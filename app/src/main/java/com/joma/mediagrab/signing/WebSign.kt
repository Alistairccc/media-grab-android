package com.joma.mediagrab.signing

import java.security.MessageDigest

/**
 * Douyin's `x-secsdk-web-signature`, ported from
 * `Douyin_TikTok_Download_API/src/dtk/signing/native/websign.py` (Apache-2.0).
 *
 * The signature is a plain MD5 over the URL-encoded query, the visitor id and a
 * per-project salt. The visitor id must come from a real browser session; the
 * app mints one with a WebView and then signs locally.
 */
object WebSign {
    const val SALT: String = "A96D855A08C0A9707F8BEF0D9A527E4E"
    const val SIGNATURE_PARAM: String = "x-secsdk-web-signature"
    const val UIFID_PARAM: String = "uifid"
    const val TIMESTAMP_PARAM: String = "timestamp"
    const val VERIFY_FP_COOKIE: String = "s_v_web_id"

    val UIFID_COOKIE_NAMES: List<String> =
        listOf(
            "uifid",
            "uifid_temp",
            "uifidtemp",
            "UIFID",
            "UIFID_TEMP",
            "UIFIDTEMP",
        )

    data class Signed(
        val query: String,
        val signature: String,
        val expire: String,
    )

    fun pickUifid(cookies: Map<String, String>): String? {
        for (name in UIFID_COOKIE_NAMES) {
            val value = cookies[name]
            if (!value.isNullOrEmpty()) {
                return value
            }
        }
        return null
    }

    fun sign(
        pairs: List<Pair<String, String>>,
        uifid: String,
        timestamp: Long = System.currentTimeMillis() / 1000,
    ): Signed {
        val stamp = timestamp.toString()
        val covered = pairs.toMutableList()
        if (covered.none { (name, _) -> name == UIFID_PARAM }) {
            covered.add(UIFID_PARAM to uifid)
        }
        covered.add(TIMESTAMP_PARAM to stamp)
        val query =
            covered.joinToString("&") { (name, value) ->
                "${encodeComponent(name)}=${encodeComponent(value)}"
            }
        val signature = md5Hex("${uifid}_${stamp}_${SALT}_$query")
        return Signed(
            query = "$query&$SIGNATURE_PARAM=$signature",
            signature = signature,
            expire = stamp,
        )
    }

    fun encodeComponent(value: String): String {
        val output = StringBuilder()
        for (byte in value.toByteArray(Charsets.UTF_8)) {
            val code = byte.toInt() and 0xFF
            val char = code.toChar()
            if (char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' ||
                char == '*' || char == '-' || char == '.' || char == '_'
            ) {
                output.append(char)
            } else {
                output.append('%')
                output.append(HEX[code ushr 4])
                output.append(HEX[code and 0x0F])
            }
        }
        return output.toString()
    }

    private fun md5Hex(text: String): String =
        MessageDigest
            .getInstance("MD5")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xFF) }

    private const val HEX = "0123456789ABCDEF"
}
