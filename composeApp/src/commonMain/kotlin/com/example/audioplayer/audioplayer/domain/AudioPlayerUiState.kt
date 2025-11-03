package com.example.audioplayer.audioplayer.domain

import com.example.audioplayer.audioplayer.data.Song

data class AudioPlayerUiState(
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val duration: Float = 1f,
    val songIndex: Int? = null,
    val songs: List<Song> = listOf()
) {
    val currentSong: Song?
        get() = songIndex?.let { songs.getOrNull(it) }
}