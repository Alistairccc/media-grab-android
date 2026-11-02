package com.joma.mediagrab.data.download

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Stream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.job
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class VideoDownloader(
    private val client: OkHttpClient,
    private val sink: DownloadSink,
) : Downloader {
    override fun download(
        media: MediaInfo,
        stream: Stream,
    ): Flow<DownloadProgress> =
        flow {
            val request =
                Request
                    .Builder()
                    .url(stream.url)
                    .apply { stream.headers.forEach { (name, value) -> header(name, value) } }
                    .build()
            val call = client.newCall(request)
            val cancellation = currentCoroutineContext().job.invokeOnCompletion { call.cancel() }
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}")
                    }
                    val body = response.body ?: throw IOException("empty response body")
                    val total = body.contentLength().takeIf { it > 0 } ?: stream.sizeBytes
                    val file = sink.create(fileName(media), stream.mime)
                    try {
                        body.byteStream().use { input ->
                            file.output().use { output ->
                                copy(input, output, total) { progress -> emit(progress) }
                            }
                        }
                        file.publish()
                    } catch (error: Exception) {
                        file.discard()
                        currentCoroutineContext().ensureActive()
                        throw error
                    }
                }
            } finally {
                cancellation.dispose()
            }
        }.flowOn(Dispatchers.IO)

    private suspend fun copy(
        input: java.io.InputStream,
        output: java.io.OutputStream,
        total: Long?,
        report: suspend (DownloadProgress) -> Unit,
    ) {
        val buffer = ByteArray(BUFFER_SIZE)
        var written = 0L
        var lastReportMs = nowMs()
        var lastReportedBytes = 0L
        var speed = 0L
        while (true) {
            val read = input.read(buffer)
            if (read == -1) {
                break
            }
            output.write(buffer, 0, read)
            written += read
            val now = nowMs()
            val elapsed = now - lastReportMs
            if (elapsed >= REPORT_INTERVAL_MS) {
                speed = if (elapsed > 0) (written - lastReportedBytes) * 1000 / elapsed else 0
                report(progress(written, total, speed))
                lastReportMs = now
                lastReportedBytes = written
            }
        }
        report(DownloadProgress(percent = 100, bytesPerSecond = speed, remainingSeconds = 0))
    }

    private fun progress(
        written: Long,
        total: Long?,
        speed: Long,
    ): DownloadProgress {
        val percent =
            total?.let { ((written * 100) / it).toInt().coerceIn(0, 99) } ?: 0
        val remaining =
            total?.let { if (speed > 0) (it - written) / speed else null }
        return DownloadProgress(
            percent = percent,
            bytesPerSecond = speed,
            remainingSeconds = remaining,
        )
    }

    private fun fileName(media: MediaInfo): String {
        val base =
            media.title
                .replace(Regex("""[\\/:*?"<>|\p{Cntrl}]"""), "_")
                .trim()
                .take(60)
                .trimEnd('.', ' ')
                .ifEmpty { "MediaGrab" }
        return "${base}_${System.currentTimeMillis()}.mp4"
    }

    private fun nowMs(): Long = System.nanoTime() / 1_000_000

    private companion object {
        const val BUFFER_SIZE = 64 * 1024
        const val REPORT_INTERVAL_MS = 150L
    }
}
