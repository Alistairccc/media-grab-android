package com.joma.mediagrab.download

import com.joma.mediagrab.core.model.MediaInfo
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.data.download.DownloadProgress
import com.joma.mediagrab.data.download.Downloader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DownloadManager(
    private val downloader: Downloader,
    private val scope: CoroutineScope,
    private val startService: () -> Unit,
) : DownloadController {
    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    override val state: StateFlow<DownloadState> = _state.asStateFlow()

    private var job: Job? = null
    private var pending: PendingDownload? = null
    private var generation = 0L

    override fun start(
        media: MediaInfo,
        stream: Stream,
    ) {
        generation++
        job?.cancel()
        job = null
        pending = PendingDownload(media, stream)
        Timber.d("Download started platform=%s", media.platform)
        _state.value = DownloadState.Running(media, stream, DownloadProgress(0))
        startService()
    }

    internal fun execute() {
        val request = pending ?: return
        pending = null
        if (job?.isActive == true) {
            return
        }
        val currentGeneration = generation
        job =
            scope.launch {
                try {
                    downloader
                        .download(request.media, request.stream)
                        .collect { progress ->
                            if (currentGeneration == generation) {
                                _state.value =
                                    DownloadState.Running(request.media, request.stream, progress)
                            }
                        }
                    if (currentGeneration != generation) {
                        return@launch
                    }
                    Timber.d("Download completed platform=%s", request.media.platform)
                    _state.value = DownloadState.Completed(request.media, request.stream)
                } catch (error: CancellationException) {
                    Timber.d("Download cancelled")
                    if (currentGeneration == generation) {
                        _state.value = DownloadState.Cancelled(request.media, request.stream)
                    }
                    throw error
                } catch (error: Exception) {
                    Timber.w(error, "Download failed")
                    if (currentGeneration == generation) {
                        _state.value =
                            DownloadState.Failed(
                                request.media,
                                request.stream,
                                AppError.Download(error),
                            )
                    }
                }
            }
    }

    override fun cancel() {
        val current = _state.value
        Timber.d("Download cancel requested")
        generation++
        pending = null
        job?.cancel()
        job = null
        if (current is DownloadState.Running) {
            _state.value = DownloadState.Cancelled(current.media, current.stream)
        }
    }

    override fun reset() {
        if (_state.value is DownloadState.Running) {
            return
        }
        Timber.d("Download state reset")
        generation++
        pending = null
        job?.cancel()
        job = null
        _state.value = DownloadState.Idle
    }

    private data class PendingDownload(
        val media: MediaInfo,
        val stream: Stream,
    )
}
