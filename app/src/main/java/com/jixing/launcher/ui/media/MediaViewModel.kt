package com.jixing.launcher.ui.media

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jixing.launcher.data.repository.MediaRepository
import com.jixing.launcher.model.MediaInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 媒体中心 ViewModel
 */
@HiltViewModel
class MediaViewModel @Inject constructor(
    application: Application,
    private val mediaRepository: MediaRepository
) : AndroidViewModel(application) {

    private val _currentMedia = MutableStateFlow<MediaInfo?>(null)
    val currentMedia: StateFlow<MediaInfo?> = _currentMedia.asStateFlow()

    private val _playlist = MutableStateFlow<List<MediaInfo>>(emptyList())
    val playlist: StateFlow<List<MediaInfo>> = _playlist.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _audioFiles = MutableStateFlow<List<MediaInfo>>(emptyList())
    val audioFiles: StateFlow<List<MediaInfo>> = _audioFiles.asStateFlow()

    private val _videoFiles = MutableStateFlow<List<MediaInfo>>(emptyList())
    val videoFiles: StateFlow<List<MediaInfo>> = _videoFiles.asStateFlow()

    init {
        loadMedia()
    }

    private fun loadMedia() {
        viewModelScope.launch {
            mediaRepository.loadAudioFiles()
            _audioFiles.value = mediaRepository.audioFiles.let { flow ->
                val list = mutableListOf<MediaInfo>()
                flow.collect { list.addAll(it) }
                list
            }
            _playlist.value = _audioFiles.value.take(20)
        }
    }

    fun selectMedia(media: MediaInfo) {
        _currentMedia.value = media
        _playlist.value = _audioFiles.value
        _isPlaying.value = true
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
    }

    fun playNext() {
        mediaRepository.playNext()
        _currentMedia.value = mediaRepository.currentMedia.let {
            // 从 flow 获取最新值
            val list = _playlist.value
            val current = _currentMedia.value
            val index = list.indexOf(current)
            if (index >= 0 && index < list.size - 1) list[index + 1] else list.firstOrNull()
        }
    }

    fun playPrevious() {
        mediaRepository.playPrevious()
        _currentMedia.value = _currentMedia.value?.let { current ->
            val list = _playlist.value
            val index = list.indexOf(current)
            if (index > 0) list[index - 1] else list.lastOrNull()
        }
    }

    fun searchMedia(query: String) {
        _playlist.value = _audioFiles.value.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true)
        }
    }
}
