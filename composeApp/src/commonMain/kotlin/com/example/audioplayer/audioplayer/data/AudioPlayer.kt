package com.example.audioplayer.audioplayer.data

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class AudioPlayer(
    onProgressCallback: (PlayerState) -> Unit,
    onReadyCallback: () -> Unit,
    onErrorCallback: (Exception) -> Unit,
    playerState: PlayerState,
    context: Any?,
) {
    fun initPlaylist(songs: List<Song>)
    fun pause()
    fun play(index: Int? = null)
    fun playerState(): PlayerState
    fun cleanUp()
    fun seek(position: Float)
    fun playNext()
    fun playPrevious()
}