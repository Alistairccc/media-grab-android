package com.joma.mediagrab.signing

import kotlin.random.Random

/**
 * Guest tokens Douyin's web signature expects beside a_bogus.
 * Ported from `Douyin_TikTok_Download_API/src/dtk/signing/native/tokens.py`.
 */
object DouyinTokens {
    const val MS_TOKEN_ALPHABET: String =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-"
    const val DOUYIN_MS_TOKEN_LENGTH: Int = 126
    const val VERIFY_FP_ALPHABET: String =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    fun falseMsToken(
        length: Int = DOUYIN_MS_TOKEN_LENGTH,
        random: Random = Random.Default,
    ): String {
        val body = StringBuilder(length)
        repeat(length) {
            body.append(MS_TOKEN_ALPHABET[random.nextInt(MS_TOKEN_ALPHABET.length)])
        }
        return body.append("==").toString()
    }

    fun verifyFp(
        nowMs: Long = System.currentTimeMillis(),
        random: Random = Random.Default,
    ): String {
        val tail = CharArray(36) { ' ' }
        for (index in intArrayOf(8, 13, 18, 23)) {
            tail[index] = '_'
        }
        tail[14] = '4'
        for (index in tail.indices) {
            if (tail[index] != ' ') {
                continue
            }
            var position = random.nextInt(VERIFY_FP_ALPHABET.length)
            if (index == 19) {
                position = (3 and position) or 8
            }
            tail[index] = VERIFY_FP_ALPHABET[position]
        }
        return "verify_${base36(nowMs)}_${String(tail)}"
    }

    private fun base36(value: Long): String {
        if (value <= 0) {
            return "0"
        }
        val digits = "0123456789abcdefghijklmnopqrstuvwxyz"
        val output = StringBuilder()
        var remaining = value
        while (remaining > 0) {
            output.append(digits[(remaining % 36).toInt()])
            remaining /= 36
        }
        return output.reverse().toString()
    }
}
