package com.example.audioplayer.audioplayer.data

expect class AudioPlayer(
    onProgressCallback: (PlayerState) -> Unit,
    onReadyCallback: () -> Unit,
    onErrorCallback: (Exception) -> Unit,
    playerState: PlayerState,
    context: Any?,
) {
    fun prepare(url: String)
    fun pause()
    fun play(url: String)
    fun playerState(): PlayerState
    fun cleanUp()
    fun seek(position: Float)
}