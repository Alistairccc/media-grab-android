package com.joma.mediagrab.signing

/**
 * SM3 (GB/T 32905-2016), used by Douyin's a_bogus digest chains.
 *
 * Ported from the reference implementation in
 * `Douyin_TikTok_Download_API/src/dtk/signing/native/sm3.py` (Apache-2.0).
 */
object Sm3 {
    private const val MASK = 0xFFFFFFFFL
    private const val T0 = 0x79CC4519L
    private const val T1 = 0x7A879D8AL

    private val IV =
        longArrayOf(
            0x7380166FL,
            0x4914B2B9L,
            0x172442D7L,
            0xDA8A0600L,
            0xA96F30BCL,
            0x163138AAL,
            0xE38DEE4DL,
            0xB0FB0E4EL,
        )

    fun hash(message: ByteArray): ByteArray {
        val state = IV.copyOf()
        val padded = pad(message)
        var offset = 0
        while (offset < padded.size) {
            compress(state, padded.copyOfRange(offset, offset + BLOCK_SIZE))
            offset += BLOCK_SIZE
        }
        val output = ByteArray(32)
        for (index in state.indices) {
            output[index * 4] = ((state[index] ushr 24) and 0xFF).toByte()
            output[index * 4 + 1] = ((state[index] ushr 16) and 0xFF).toByte()
            output[index * 4 + 2] = ((state[index] ushr 8) and 0xFF).toByte()
            output[index * 4 + 3] = (state[index] and 0xFF).toByte()
        }
        return output
    }

    fun toArray(message: ByteArray): IntArray = hash(message).map { byte -> byte.toInt() and 0xFF }.toIntArray()

    fun toArray(text: String): IntArray = toArray(text.toByteArray(Charsets.UTF_8))

    private fun compress(
        state: LongArray,
        block: ByteArray,
    ) {
        val w = LongArray(68)
        val w1 = LongArray(64)
        for (index in 0 until 16) {
            val base = index * 4
            w[index] =
                ((block[base].toLong() and 0xFF) shl 24) or
                ((block[base + 1].toLong() and 0xFF) shl 16) or
                ((block[base + 2].toLong() and 0xFF) shl 8) or
                (block[base + 3].toLong() and 0xFF)
        }
        for (index in 16 until 68) {
            var value = w[index - 16] xor w[index - 9] xor rotl(w[index - 3], 15)
            value = value xor rotl(value, 15) xor rotl(value, 23)
            w[index] = (value xor rotl(w[index - 13], 7) xor w[index - 6]) and MASK
        }
        for (index in 0 until 64) {
            w1[index] = (w[index] xor w[index + 4]) and MASK
        }

        var a = state[0]
        var b = state[1]
        var c = state[2]
        var d = state[3]
        var e = state[4]
        var f = state[5]
        var g = state[6]
        var h = state[7]

        for (index in 0 until 64) {
            val t = if (index < 16) T0 else T1
            val ss1 = rotl((rotl(a, 12) + e + rotl(t, index)) and MASK, 7)
            val ss2 = ss1 xor rotl(a, 12)
            val tt1 = (ff(index, a, b, c) + d + ss2 + w1[index]) and MASK
            val tt2 = (gg(index, e, f, g) + h + ss1 + w[index]) and MASK
            d = c
            c = rotl(b, 9)
            b = a
            a = tt1
            h = g
            g = rotl(f, 19)
            f = e
            e = (tt2 xor rotl(tt2, 9) xor rotl(tt2, 17)) and MASK
        }

        state[0] = (state[0] xor a) and MASK
        state[1] = (state[1] xor b) and MASK
        state[2] = (state[2] xor c) and MASK
        state[3] = (state[3] xor d) and MASK
        state[4] = (state[4] xor e) and MASK
        state[5] = (state[5] xor f) and MASK
        state[6] = (state[6] xor g) and MASK
        state[7] = (state[7] xor h) and MASK
    }

    private fun pad(message: ByteArray): ByteArray {
        val bitLength = message.size.toLong() * 8
        val padded = message + 0x80.toByte()
        val zeroCount = ((56 - padded.size % BLOCK_SIZE) + BLOCK_SIZE) % BLOCK_SIZE
        val withZeros = padded + ByteArray(zeroCount)
        val result = ByteArray(withZeros.size + 8)
        withZeros.copyInto(result)
        for (index in 0 until 8) {
            result[result.size - 1 - index] = ((bitLength ushr (8 * index)) and 0xFF).toByte()
        }
        return result
    }

    private fun rotl(
        value: Long,
        bits: Int,
    ): Long {
        val shift = bits % 32
        if (shift == 0) {
            return value and MASK
        }
        return ((value shl shift) or (value ushr (32 - shift))) and MASK
    }

    private fun ff(
        index: Int,
        x: Long,
        y: Long,
        z: Long,
    ): Long =
        if (index < 16) {
            x xor y xor z
        } else {
            (x and y) or (x and z) or (y and z)
        }

    private fun gg(
        index: Int,
        x: Long,
        y: Long,
        z: Long,
    ): Long =
        if (index < 16) {
            x xor y xor z
        } else {
            (x and y) or ((x.inv() and MASK) and z)
        }

    private const val BLOCK_SIZE = 64
}
