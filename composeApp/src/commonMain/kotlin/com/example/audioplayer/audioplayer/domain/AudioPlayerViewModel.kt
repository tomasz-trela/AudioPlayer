package com.example.audioplayer.audioplayer.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.audioplayer.data.AudioPlayer
import com.example.audioplayer.audioplayer.data.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AudioPlayerViewModel(context: Any?, songRepository: SongRepository) : ViewModel() {

    private val songs = songRepository.getSongs()
    private val audioPlayer: AudioPlayer = AudioPlayer(
        context = context
    )

    val uiState: StateFlow<AudioPlayerUiState> = audioPlayer.playerState
        .map { playerState ->
            val currentIndex = songs.indexOfFirst { song ->
                song.url == playerState.currentPlayingResource
            }.takeIf { it != -1 }

            AudioPlayerUiState(
                songs = songs,
                isPlaying = playerState.isPlaying,
                progress = playerState.currentTime.takeIf { it.isFinite() } ?: 0f,
                duration = playerState.duration.takeIf { it.isFinite() && it > 0f } ?: 1f,
                songIndex = currentIndex
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AudioPlayerUiState(songs = songs)
        )

    init {
        audioPlayer.initPlaylist(songs)
    }

    fun changeSong(index: Int) {
        if (index < 0 || index >= songs.size) return
        audioPlayer.play(index)
    }

    fun playNextSong() {
        audioPlayer.playNext()
    }

    fun playPreviousSong() {
        audioPlayer.playPrevious()
    }

    fun togglePlayPause() {
        val state = uiState.value
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