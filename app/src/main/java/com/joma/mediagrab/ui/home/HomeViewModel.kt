package com.joma.mediagrab.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joma.mediagrab.core.model.Stream
import com.joma.mediagrab.core.result.AppError
import com.joma.mediagrab.core.result.AppResult
import com.joma.mediagrab.core.util.LinkParser
import com.joma.mediagrab.data.MediaRepository
import com.joma.mediagrab.download.DownloadController
import com.joma.mediagrab.download.DownloadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: MediaRepository,
    private val downloadController: DownloadController,
) : ViewModel() {
    private val _link = MutableStateFlow("")
    val link: StateFlow<String> = _link.asStateFlow()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var parseJob: Job? = null

    init {
        viewModelScope.launch {
            downloadController.state.collect(::applyDownloadState)
        }
    }

    fun onLinkChange(text: String) {
        if (_uiState.value is HomeUiState.Downloading || _uiState.value is HomeUiState.Parsing) {
            return
        }
        _link.value = text
        if (_uiState.value !is HomeUiState.Idle) {
            _uiState.value = HomeUiState.Idle
        }
    }

    fun onParse() {
        if (_uiState.value is HomeUiState.Downloading || _uiState.value is HomeUiState.Parsing) {
            return
        }
        val url = LinkParser.extractFirstSupported(_link.value)
        if (url == null) {
            _uiState.value = HomeUiState.Failed(AppError.InvalidLink)
            return
        }
        _link.value = url
        parse(url)
    }

    fun onRetry() {
        if (_uiState.value is HomeUiState.Downloading) {
            return
        }
        val url = _link.value
        if (url.isNotEmpty()) {
            parse(url)
        }
    }

    fun onSave() {
        val ready = _uiState.value as? HomeUiState.Ready ?: return
        _uiState.value =
            HomeUiState.Downloading(
                media = ready.media,
                selectedStream = ready.selectedStream,
                percent = 0,
            )
        downloadController.start(ready.media, ready.selectedStream)
    }

    fun onCancel() {
        downloadController.cancel()
    }

    fun onSelectStream(stream: Stream) {
        val current = _uiState.value as? HomeUiState.Ready ?: return
        _uiState.value = current.copy(selectedStream = stream)
    }

    private fun applyDownloadState(state: DownloadState) {
        _uiState.value =
            when (state) {
                is DownloadState.Running ->
                    HomeUiState.Downloading(
                        media = state.media,
                        selectedStream = state.stream,
                        percent = state.progress.percent,
                        bytesPerSecond = state.progress.bytesPerSecond,
                        remainingSeconds = state.progress.remainingSeconds,
                    )

                is DownloadState.Completed -> HomeUiState.Done(state.media, state.stream)
                is DownloadState.Failed -> HomeUiState.Failed(state.error)
                is DownloadState.Cancelled -> HomeUiState.Ready(state.media, state.stream)
                DownloadState.Idle -> return
            }
    }

    private fun parse(url: String) {
        parseJob?.cancel()
        _uiState.value = HomeUiState.Parsing
        parseJob =
            viewModelScope.launch {
                when (val result = repository.parse(url)) {
                    is AppResult.Ok -> {
                        val media = result.value
                        val stream = media.streams.firstOrNull()
                        _uiState.value =
                            if (stream == null) {
                                HomeUiState.Failed(AppError.NoStream)
                            } else {
                                HomeUiState.Ready(media, stream)
                            }
                    }

                    is AppResult.Err -> {
                        _uiState.value = HomeUiState.Failed(result.error)
                    }
                }
            }
    }

    companion object {
        fun factory(
            repository: MediaRepository,
            downloadController: DownloadController,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repository, downloadController) as T
            }
    }
}
