package com.example.audioplayer.audioplayer.data

import kotlinx.coroutines.flow.StateFlow

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class AudioPlayer(
    context: Any?,
) {

    val playerState: StateFlow<PlayerState>
    fun initPlaylist(songs: List<Song>)
    fun pause()
    fun play(index: Int? = null)
    fun cleanUp()
    fun seek(position: Float)
    fun playNext()
    fun playPrevious()
}