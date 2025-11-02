package com.example.audioplayer.audioplayer.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.audioList.data.Song
import com.example.audioplayer.audioplayer.data.AudioPlayer
import com.example.audioplayer.audioplayer.data.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AudioPlayerUiState(
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val duration: Float = 1f,
    val currentSong: Song? = null
)

class AudioPlayerViewModel(context: Any?) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioPlayerUiState())
    val uiState: StateFlow<AudioPlayerUiState> = _uiState.asStateFlow()

    private val audioPlayer: AudioPlayer = AudioPlayer(
        onProgressCallback = { playerState -> onProgressUpdate(playerState) },
        onReadyCallback = { onReady() },
        onErrorCallback = { exception -> onError(exception) },
        playerState = PlayerState(),
        context = context
    )

    init {
        val url = uiState.value.currentSong?.url
        if (url != null)
            audioPlayer.prepare(url)
    }

    private fun onProgressUpdate(playerState: PlayerState) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPlaying = playerState.isPlaying,
                    progress = playerState.currentTime.takeIf { v -> !v.isNaN() } ?: 0f,
                    duration = playerState.duration.takeIf { v -> !v.isNaN() && v > 0f } ?: 1f
                )
            }
        }
    }

    private fun onReady() {
        val duration = audioPlayer.playerState().duration
        _uiState.update {
            it.copy(duration = if (duration > 0) duration else 1f)
        }
    }

    private fun onError(exception: Exception) {
        println("AudioPlayerViewModel Error: $exception")
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun changeSong(song: Song) {
        if (_uiState.value.currentSong == null) {
            audioPlayer.prepare(song.url)
        }
        _uiState.update { it.copy(currentSong = song) }
        audioPlayer.play(song.url)
    }

    fun togglePlayPause() {
        val currentState = _uiState.value

        if (currentState.currentSong == null) {
            return
        }

        if (currentState.isPlaying) {
            audioPlayer.pause()
        } else {
            audioPlayer.play(currentState.currentSong.url)
        }
    }

    fun seek(newPosition: Float) {
        if (newPosition.isFinite()) {
            audioPlayer.seek(newPosition)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.cleanUp()
    }
}