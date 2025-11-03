package com.example.audioplayer.audioplayer.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.audioplayer.data.AudioPlayer
import com.example.audioplayer.audioplayer.data.PlayerState
import com.example.audioplayer.audioplayer.data.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AudioPlayerViewModel(context: Any?, songRepository: SongRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioPlayerUiState(songs = songRepository.getSongs()))
    val uiState: StateFlow<AudioPlayerUiState> = _uiState.asStateFlow()

    private val audioPlayer: AudioPlayer = AudioPlayer(
        onProgressCallback = { playerState -> onProgressUpdate(playerState) },
        onReadyCallback = { onReady() },
        onErrorCallback = { exception -> onError(exception) },
        playerState = PlayerState(),
        context = context
    )

    init {
        audioPlayer.initPlaylist(_uiState.value.songs)
        if (_uiState.value.songs.isNotEmpty()) {
            _uiState.update { it.copy(songIndex = 0) }
        }
    }

    private fun onProgressUpdate(playerState: PlayerState) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val newIndex = currentState.songs.indexOfFirst { song ->
                    song.url == playerState.currentPlayingResource
                }.takeIf { it != -1 } ?: currentState.songIndex

                currentState.copy(
                    isPlaying = playerState.isPlaying,
                    progress = playerState.currentTime.takeIf { v -> v.isFinite() } ?: 0f,
                    duration = playerState.duration.takeIf { v -> v.isFinite() && v > 0f } ?: 1f,
                    songIndex = newIndex
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
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun changeSong(index: Int) {
        if (index < 0 || index >= _uiState.value.songs.size) return
        _uiState.update { it.copy(songIndex = index) }
        audioPlayer.play(index)
    }

    fun playNextSong() {
        audioPlayer.playNext()
    }

    fun playPreviousSong() {
        audioPlayer.playPrevious()
    }

    fun togglePlayPause() {
        val state = _uiState.value
        if (state.isPlaying) {
            audioPlayer.pause()
        } else {
            audioPlayer.play(state.songIndex ?: 0)
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