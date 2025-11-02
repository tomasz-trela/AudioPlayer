package com.example.audioplayer.audioplayer.data

data class PlayerState(
    var isPlaying: Boolean = false,
    var isBuffering: Boolean = false,
    var currentTime: Float = 0f,
    var duration: Float = 0f,
    var currentPlayingResource: String? = null

) {
    val progress = currentTime / duration
}