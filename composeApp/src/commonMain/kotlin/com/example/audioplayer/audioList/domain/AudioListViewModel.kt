package com.example.audioplayer.audioList.domain

import androidx.lifecycle.ViewModel
import com.example.audioplayer.audioList.data.Song
import com.example.audioplayer.audioList.data.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioListState(val songs: List<Song>)

class AudioListViewModel(songRepository: SongRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AudioListState(songRepository.getSongs()))
    val uiState: StateFlow<AudioListState> = _uiState.asStateFlow()
}