package com.joma.mediagrab.signing

import java.io.ByteArrayOutputStream
import kotlin.random.Random

/**
 * Douyin web `a_bogus` generator, ported from
 * `Douyin_TikTok_Download_API/src/dtk/signing/native/abogus.py` (Apache-2.0),
 * which re-derived the algorithm from Douyin's own `bdms.js` v1.0.1.19-fix.01.
 *
 * The signature binds the query, the request body and the User-Agent. Noise is
 * decorative; [decode] / [structureError] recover the meaningful fields and are
 * used by the regression tests.
 */
class ABogus(
    private val userAgent: String,
    private val browserInfo: String = DEFAULT_BROWSER_INFO,
    private val pageId: Int = PAGE_ID,
    private val aid: Int = AID,
    private val random: Random = Random.Default,
) {
    fun sign(
        query: String,
        body: String = "",
        contentType: String = "",
        nowMs: Long = System.currentTimeMillis(),
    ): String {
        val normalizedBody =
            if (contentType.contains("multipart/form-data", ignoreCase = true)) "" else body
        require(nowMs >= FORTNIGHT_EPOCH_MS) {
            "clock is before the a_bogus epoch: $nowMs"
        }

        val fields = buildFields(query, normalizedBody, nowMs)
        val versionPartOne = maskPair(intArrayOf(SDK_VERSION[0], SDK_VERSION[1]), random)
        val versionPartTwo =
            maskPair(
                intArrayOf(SDK_VERSION[2], SDK_VERSION[3]),
                random,
                low = probeNoise(random),
                high = tripwireNoise(random),
            )
        val version = versionPartOne + versionPartTwo

        var checksum = 0
        for (byte in version) {
            checksum = checksum xor byte
        }
        for (name in FIELD_ORDER) {
            checksum = checksum xor fields.getValue(name)
        }

        val infoBytes = jsBytes(browserInfo)
        val tailBytes = jsBytes("${(nowMs + 3) and 0xFF},")
        val bodyBytes = ByteArray(FIELD_ORDER.size + infoBytes.size + tailBytes.size + 1)
        var offset = 0
        for (name in FIELD_ORDER) {
            bodyBytes[offset++] = fields.getValue(name).toByte()
        }
        infoBytes.copyInto(bodyBytes, offset)
        offset += infoBytes.size
        tailBytes.copyInto(bodyBytes, offset)
        offset += tailBytes.size
        bodyBytes[offset] = checksum.toByte()

        val header =
            maskPair(
                intArrayOf(HEADER_MAGIC[0], HEADER_MAGIC[1]),
                random,
                high = headerNoise(userAgent, random),
            )
        val frame = expandNoise(bodyBytes, random)
        val sealed = rc4(byteArrayOf(PAYLOAD_KEY.toByte()), version.toBytes() + frame)
        return encodeBase64(header.toBytes() + sealed, S4_ALPHABET)
    }

    private fun buildFields(
        query: String,
        body: String,
        nowMs: Long,
    ): Map<String, Int> {
        val queryDigest = digestOf(query)
        val bodyDigest = digestOf(body)
        val agentDigest = userAgentDigest(userAgent)
        val ink = nowMs - 1
        val fortnights = (nowMs - FORTNIGHT_EPOCH_MS) / FORTNIGHT_MS
        val infoBytes = jsBytes(browserInfo)
        val tailBytes = jsBytes("${(nowMs + 3) and 0xFF},")

        val fields = LinkedHashMap<String, Int>()
        fields["L24"] = 41
        fields["L26"] = fortnights.toInt()
        fields["L27"] = CALL_BUCKET
        fields["L28"] = 3
        fields["L35"] = ENV_FLAGS and 0xFF
        fields["L36"] = (ENV_FLAGS / 256) and 0xFF
        fields["L38"] = NR_FLAGS and 0xFF
        fields["L39"] = (NR_FLAGS shr 8) and 0xFF
        fields["L66"] = TRIPWIRE_LOCKED
        fields["L79"] = infoBytes.size and 0xFF
        fields["L80"] = (infoBytes.size shr 8) and 0xFF
        fields["L84"] = tailBytes.size and 0xFF
        fields["L85"] = (tailBytes.size shr 8) and 0xFF

        littleEndian(nowMs, 6).forEachIndexed { index, value ->
            fields["L${29 + index}"] = value
        }
        littleEndian(DETECT_FLAGS.toLong(), 4).forEachIndexed { index, value ->
            fields["L${44 + index}"] = value
        }
        NR_TAG.forEachIndexed { index, value ->
            fields["L${40 + index}"] = value
        }
        littleEndian(ink, 6).forEachIndexed { index, value ->
            fields["L${60 + index}"] = value
        }
        littleEndian(pageId.toLong(), 4).forEachIndexed { index, value ->
            fields["L${67 + index}"] = value
        }
        littleEndian(aid.toLong(), 4).forEachIndexed { index, value ->
            fields["L${71 + index}"] = value
        }

        writeChain(fields, QUERY_CHAIN, queryDigest)
        writeChain(fields, BODY_CHAIN, bodyDigest)
        writeChain(fields, UA_CHAIN, agentDigest)
        return fields
    }

    private fun writeChain(
        fields: MutableMap<String, Int>,
        chain: Chain,
        digest: IntArray,
    ) {
        val values =
            intArrayOf(
                digest[chain.firstIndex],
                digest[chain.secondIndex],
                canary(digest, chain.canaryOffset, chain.canarySentinel, chain.canaryFallback),
            )
        chain.slots.forEachIndexed { index, slot ->
            fields[slot] = values[index]
        }
    }

    companion object {
        const val SALT: String = "dhzx"
        const val PAYLOAD_KEY: Int = 0xD3
        const val FORTNIGHT_EPOCH_MS: Long = 1_721_836_800_000L
        const val PAGE_ID: Int = 6241
        const val AID: Int = 6383
        const val ENV_FLAGS: Int = 1
        const val DETECT_FLAGS: Int = 14
        const val NR_FLAGS: Int = 0x21
        const val TRIPWIRE_LOCKED: Int = 3
        const val CALL_BUCKET: Int = 6
        const val DEFAULT_BROWSER_INFO: String =
            "1920|947|1920|1032|1920|1032|1920|1080|Win32"

        const val S3_ALPHABET: String =
            "ckdp1h4ZKsUB80/Mfvw36XIgR25+WQAlEi7NLboqYTOPuzmFjJnryx9HVGDaStCe"
        const val S4_ALPHABET: String =
            "Dkdpgh2ZmsQB80/MfvV36XI1R45-WUAlEixNLwoqYTOPuzKFjJnry79HbGcaStCe"

        val HEADER_MAGIC: IntArray = intArrayOf(3, 82)
        val SDK_VERSION: IntArray = intArrayOf(1, 0, 1, 0)

        private val NOISE_MASKS = intArrayOf(0x91, 0x42, 0x2C)
        private val DATA_MASKS = intArrayOf(0x6E, 0xBD, 0xD3)
        private val NR_TAG = intArrayOf(0, 0, 0, 0)
        private const val FORTNIGHT_MS: Long = 1000L * 60 * 60 * 24 * 14

        val FIELD_ORDER: List<String> =
            listOf(
                "L34", "L44", "L56", "L61", "L73", "L29", "L70", "L45", "L35", "L49",
                "L38", "L66", "L51", "L68", "L28", "L48", "L64", "L47", "L30", "L71",
                "L26", "L55", "L31", "L69", "L59", "L40", "L62", "L63", "L27", "L72",
                "L41", "L74", "L57", "L52", "L42", "L39", "L33", "L67", "L53", "L43",
                "L65", "L46", "L36", "L24", "L60", "L32", "L79", "L80", "L84", "L85",
            )

        private val QUERY_CHAIN =
            Chain(
                slots = listOf("L48", "L49", "L51"),
                firstIndex = 9,
                secondIndex = 18,
                canaryOffset = 3,
                canarySentinel = 11,
                canaryFallback = 12,
            )
        private val BODY_CHAIN =
            Chain(
                slots = listOf("L52", "L53", "L55"),
                firstIndex = 10,
                secondIndex = 19,
                canaryOffset = 4,
                canarySentinel = 8,
                canaryFallback = 9,
            )
        private val UA_CHAIN =
            Chain(
                slots = listOf("L56", "L57", "L59"),
                firstIndex = 11,
                secondIndex = 21,
                canaryOffset = 5,
                canarySentinel = 12,
                canaryFallback = 13,
            )

        fun structureError(value: String): String? {
            val body = value.trimEnd('=')
            if (body.isEmpty() || body.contains('=') || body.any { it !in S4_ALPHABET }) {
                return "alphabet"
            }
            if (value.length % 4 != 0) {
                return "length not a multiple of four"
            }

            val payload = decodeBase64(value)
            if (payload.size < 80) {
                return "payload too short"
            }
            val header = unmaskPair(payload.copyOfRange(0, 4))
            if (header[0] != HEADER_MAGIC[0] || header[1] != HEADER_MAGIC[1]) {
                return "header magic"
            }

            val plain = rc4(byteArrayOf(PAYLOAD_KEY.toByte()), payload.copyOfRange(4, payload.size))
            val version = plain.copyOfRange(0, 8)
            val frame = plain.copyOfRange(8, plain.size)
            val versionPartOne = unmaskPair(version.copyOfRange(0, 4))
            val versionPartTwo = unmaskPair(version.copyOfRange(4, 8))
            if (versionPartOne[0] != SDK_VERSION[0] || versionPartOne[1] != SDK_VERSION[1]) {
                return "sdk version"
            }
            if (versionPartTwo[0] != SDK_VERSION[2] || versionPartTwo[1] != SDK_VERSION[3]) {
                return "sdk version"
            }

            val unpacked = collapseNoise(frame)
            if (unpacked.size < FIELD_ORDER.size + 1) {
                return "frame too short"
            }
            val fields = LinkedHashMap<String, Int>()
            FIELD_ORDER.forEachIndexed { index, name ->
                fields[name] = unpacked[index].toInt() and 0xFF
            }
            val infoLength = fields.getValue("L79") or (fields.getValue("L80") shl 8)
            val tailLength = fields.getValue("L84") or (fields.getValue("L85") shl 8)
            val end = FIELD_ORDER.size + infoLength + tailLength
            if (end + 1 > unpacked.size) {
                return "declared lengths overrun the frame"
            }
            if (unpacked.size - (end + 1) > 2) {
                return "declared lengths leave a tail"
            }

            var checksum = 0
            for (byte in version) {
                checksum = checksum xor (byte.toInt() and 0xFF)
            }
            for (name in FIELD_ORDER) {
                checksum = checksum xor fields.getValue(name)
            }
            if ((unpacked[end].toInt() and 0xFF) != checksum) {
                return "checksum"
            }
            return null
        }

        fun decode(value: String): DecodedABogus {
            val problem = structureError(value)
            require(problem == null) { "not a well-formed a_bogus: $problem" }

            val payload = decodeBase64(value)
            val plain = rc4(byteArrayOf(PAYLOAD_KEY.toByte()), payload.copyOfRange(4, payload.size))
            val unpacked = collapseNoise(plain.copyOfRange(8, plain.size))
            val fields = LinkedHashMap<String, Int>()
            FIELD_ORDER.forEachIndexed { index, name ->
                fields[name] = unpacked[index].toInt() and 0xFF
            }

            val infoLength = fields.getValue("L79") or (fields.getValue("L80") shl 8)
            val tailLength = fields.getValue("L84") or (fields.getValue("L85") shl 8)
            val start = FIELD_ORDER.size
            val infoEnd = start + infoLength
            return DecodedABogus(
                headerMagic = unmaskPair(payload.copyOfRange(0, 4)),
                sdkVersion =
                    intArrayOf(
                        unmaskPair(plain.copyOfRange(0, 4))[0],
                        unmaskPair(plain.copyOfRange(0, 4))[1],
                        unmaskPair(plain.copyOfRange(4, 8))[0],
                        unmaskPair(plain.copyOfRange(4, 8))[1],
                    ),
                nowMs = littleEndianValue(fields, 29, 6),
                inkMs = littleEndianValue(fields, 60, 6),
                pageId = littleEndianValue(fields, 67, 4),
                aid = littleEndianValue(fields, 71, 4),
                envFlags = fields.getValue("L35"),
                detectFlags = littleEndianValue(fields, 44, 4),
                callBucket = fields.getValue("L27"),
                tripwire = fields.getValue("L66"),
                browserInfo = String(unpacked.copyOfRange(start, infoEnd), Charsets.UTF_8),
                tail = String(unpacked.copyOfRange(infoEnd, infoEnd + tailLength), Charsets.UTF_8),
                fields = fields,
            )
        }

        fun rc4(
            key: ByteArray,
            data: ByteArray,
        ): ByteArray {
            val box = IntArray(256)
            for (index in 0 until 256) {
                box[255 - index] = index
            }
            var j = 0
            for (index in 0 until 256) {
                j = (j * box[index] + j + (key[index % key.size].toInt() and 0xFF)) % 256
                val swap = box[index]
                box[index] = box[j]
                box[j] = swap
            }

            val output = ByteArray(data.size)
            var i = 0
            j = 0
            for (index in data.indices) {
                i = (i + 1) % 256
                j = (j + box[i]) % 256
                val swap = box[i]
                box[i] = box[j]
                box[j] = swap
                output[index] =
                    (
                        (data[index].toInt() and 0xFF) xor box[(box[i] + box[j]) % 256]
                    ).toByte()
            }
            return output
        }

        fun encodeBase64(
            data: ByteArray,
            alphabet: String = S4_ALPHABET,
        ): String {
            val output = StringBuilder()
            var offset = 0
            while (offset < data.size) {
                val remaining = minOf(3, data.size - offset)
                var block = 0
                for (index in 0 until 3) {
                    block = block shl 8
                    if (index < remaining) {
                        block = block or (data[offset + index].toInt() and 0xFF)
                    }
                }
                val digits =
                    intArrayOf(
                        (block ushr 18) and 0x3F,
                        (block ushr 12) and 0x3F,
                        (block ushr 6) and 0x3F,
                        block and 0x3F,
                    )
                for (index in 0 until remaining + 1) {
                    output.append(alphabet[digits[index]])
                }
                offset += remaining
            }
            output.append("=".repeat((4 - output.length % 4) % 4))
            return output.toString()
        }

        fun decodeBase64(text: String): ByteArray {
            val index = HashMap<Char, Int>()
            S4_ALPHABET.forEachIndexed { position, char -> index[char] = position }
            val body = text.trimEnd('=')
            val output = ByteArrayOutputStream()
            var offset = 0
            while (offset < body.length) {
                val chunk = body.substring(offset, minOf(offset + 4, body.length))
                var block = 0
                for (char in chunk) {
                    val value =
                        index[char]
                            ?: throw IllegalArgumentException("not a s4 digit: $char")
                    block = (block shl 6) or value
                }
                block = block shl (6 * (4 - chunk.length))
                val bytes =
                    intArrayOf(
                        (block ushr 16) and 0xFF,
                        (block ushr 8) and 0xFF,
                        block and 0xFF,
                    )
                for (indexByte in 0 until chunk.length - 1) {
                    output.write(bytes[indexByte])
                }
                offset += chunk.length
            }
            return output.toByteArray()
        }

        fun digestOf(text: String): IntArray {
            val inner = Sm3.hash((text + SALT).toByteArray(Charsets.UTF_8))
            return Sm3.toArray(inner)
        }

        fun userAgentDigest(userAgent: String): IntArray {
            val key = byteArrayOf(ENV_FLAGS.shr(8).toByte(), ENV_FLAGS.toByte(), DETECT_FLAGS.toByte())
            val sealed = rc4(key, jsBytes(userAgent.trim()))
            return Sm3.toArray(encodeBase64(sealed, S3_ALPHABET).toByteArray(Charsets.UTF_8))
        }

        fun jsBytes(text: String): ByteArray {
            val output = ByteArrayOutputStream()
            for (char in text) {
                val code = char.code
                if ((code and 0xFF00) != 0) {
                    output.write((code ushr 8) and 0xFF)
                }
                output.write(code and 0xFF)
            }
            return output.toByteArray()
        }

        internal fun maskPair(
            pair: IntArray,
            random: Random,
            low: Int? = null,
            high: Int? = null,
        ): IntArray {
            val noise = (random.nextDouble() * 65535).toInt()
            val lowByte = (low ?: (noise and 0xFF)) and 0xFF
            val highByte = (high ?: ((noise shr 8) and 0xFF)) and 0xFF
            return intArrayOf(
                (lowByte and 0xAA) or (pair[0] and 0x55),
                (lowByte and 0x55) or (pair[0] and 0xAA),
                (highByte and 0xAA) or (pair[1] and 0x55),
                (highByte and 0x55) or (pair[1] and 0xAA),
            )
        }

        internal fun unmaskPair(carrier: ByteArray): IntArray =
            intArrayOf(
                (carrier[0].toInt() and 0x55) or (carrier[1].toInt() and 0xAA),
                (carrier[2].toInt() and 0x55) or (carrier[3].toInt() and 0xAA),
            )

        internal fun expandNoise(
            body: ByteArray,
            random: Random,
        ): ByteArray {
            val output = ByteArrayOutputStream()
            var offset = 0
            while (offset < body.size) {
                val remaining = minOf(3, body.size - offset)
                if (remaining < 3) {
                    output.write(body[offset].toInt() and 0xFF)
                    if (remaining > 1 && body[offset + 1].toInt() != 0) {
                        output.write(body[offset + 1].toInt() and 0xFF)
                    }
                    break
                }
                val noise = (random.nextDouble() * 1000).toInt() and 0xFF
                val first = body[offset].toInt() and 0xFF
                val second = body[offset + 1].toInt() and 0xFF
                val third = body[offset + 2].toInt() and 0xFF
                output.write((noise and NOISE_MASKS[0]) or (first and DATA_MASKS[0]))
                output.write((noise and NOISE_MASKS[1]) or (second and DATA_MASKS[1]))
                output.write((noise and NOISE_MASKS[2]) or (third and DATA_MASKS[2]))
                output.write(
                    (first and NOISE_MASKS[0]) or
                        (second and NOISE_MASKS[1]) or
                        (third and NOISE_MASKS[2]),
                )
                offset += 3
            }
            return output.toByteArray()
        }

        internal fun collapseNoise(frame: ByteArray): ByteArray {
            val output = ByteArrayOutputStream()
            var offset = 0
            while (offset < frame.size) {
                val remaining = minOf(4, frame.size - offset)
                if (remaining < 4) {
                    output.write(frame, offset, remaining)
                    break
                }
                val first = frame[offset].toInt() and 0xFF
                val second = frame[offset + 1].toInt() and 0xFF
                val third = frame[offset + 2].toInt() and 0xFF
                val fourth = frame[offset + 3].toInt() and 0xFF
                output.write((first and DATA_MASKS[0]) or (fourth and NOISE_MASKS[0]))
                output.write((second and DATA_MASKS[1]) or (fourth and NOISE_MASKS[1]))
                output.write((third and DATA_MASKS[2]) or (fourth and NOISE_MASKS[2]))
                offset += 4
            }
            return output.toByteArray()
        }

        private fun canary(
            digest: IntArray,
            offset: Int,
            sentinel: Int,
            fallback: Int,
        ): Int {
            for (index in offset until digest.size) {
                if (digest[index] != sentinel) {
                    return digest[index]
                }
            }
            return fallback
        }

        private fun headerNoise(
            userAgent: String,
            random: Random,
        ): Int {
            val base =
                when (familyOf(userAgent)) {
                    "chrome" -> 0
                    "firefox" -> 40
                    "safari" -> 81
                    "edge" -> 125
                    "huawei" -> 170
                    else -> 210
                }
            return (base + (random.nextDouble() * 40).toInt()) and 0xFF
        }

        private fun familyOf(userAgent: String): String {
            val lower = userAgent.lowercase()
            for (name in listOf("edg", "huawei", "firefox", "chrome", "safari")) {
                if (lower.contains(name)) {
                    return if (name == "edg") "edge" else name
                }
            }
            return "other"
        }

        private fun probeNoise(random: Random): Int {
            val value = (random.nextDouble() * 240).toInt()
            return if (value > 109) value + value % 2 + 1 else value
        }

        private fun tripwireNoise(random: Random): Int = ((random.nextDouble() * 255).toInt() and 0x4D) or 0xB2

        private fun littleEndian(
            value: Long,
            count: Int,
        ): IntArray = IntArray(count) { index -> ((value ushr (8 * index)) and 0xFF).toInt() }

        private fun littleEndianValue(
            fields: Map<String, Int>,
            first: Int,
            count: Int,
        ): Long {
            var value = 0L
            for (index in 0 until count) {
                value = value or (fields.getValue("L${first + index}").toLong() shl (8 * index))
            }
            return value
        }

        private fun IntArray.toBytes(): ByteArray = ByteArray(size) { index -> this[index].toByte() }
    }
}

data class DecodedABogus(
    val headerMagic: IntArray,
    val sdkVersion: IntArray,
    val nowMs: Long,
    val inkMs: Long,
    val pageId: Long,
    val aid: Long,
    val envFlags: Int,
    val detectFlags: Long,
    val callBucket: Int,
    val tripwire: Int,
    val browserInfo: String,
    val tail: String,
    val fields: Map<String, Int>,
)

private data class Chain(
    val slots: List<String>,
    val firstIndex: Int,
    val secondIndex: Int,
    val canaryOffset: Int,
    val canarySentinel: Int,
    val canaryFallback: Int,
)
