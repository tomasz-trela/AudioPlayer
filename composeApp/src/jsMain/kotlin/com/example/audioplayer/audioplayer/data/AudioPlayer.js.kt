package com.example.audioplayer.audioplayer.data

import org.w3c.dom.Audio
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.events.Event

actual class AudioPlayer actual constructor(
    private val onProgressCallback: (PlayerState) -> Unit,
    private val onReadyCallback: () -> Unit,
    private val onErrorCallback: (Exception) -> Unit,
    private var playerState: PlayerState,
    context: Any?,
) {
    private var audio: HTMLAudioElement? = Audio()

    private val timeUpdateListener: (Event) -> Unit = {
        audio?.let {
            val currentTime = it.currentTime.toFloat()

            val newPlayerState = playerState.copy(
                isPlaying = !it.paused,
                duration = it.duration.toFloat(),
                currentTime = currentTime
            )
            if (newPlayerState != playerState) {
                playerState = newPlayerState
                onProgressCallback(playerState)
            }
        }
    }

    private val canPlayThroughListener: (Event) -> Unit = {
        onReadyCallback()
    }

    private val errorListener: (Event) -> Unit = {
        onErrorCallback(Exception("Error playing audio"))
    }

    private val endedListener: (Event) -> Unit = {
        playerState = playerState.copy(isPlaying = false)
        onProgressCallback(playerState)
    }

    init {
        audio?.addEventListener("timeupdate", timeUpdateListener)
        audio?.addEventListener("canplaythrough", canPlayThroughListener)
        audio?.addEventListener("error", errorListener)
        audio?.addEventListener("ended", endedListener)
    }

    actual fun pause() {
        audio?.pause()
        playerState = playerState.copy(isPlaying = false)
        onProgressCallback(playerState)
    }

    actual fun play(url: String) {
        if (audio?.src != url) {
            audio?.src = url
        }

        audio?.play()?.then {
            playerState =
                playerState.copy(
                    isPlaying = true,
                    currentTime = audio?.currentTime?.toFloat() ?: 0f
                )
            onProgressCallback(playerState)
        }?.catch {
            onErrorCallback(Exception("Error playing audio: $it"))
        }
    }

    actual fun playerState(): PlayerState {
        return playerState
    }

    actual fun cleanUp() {
        audio?.removeEventListener("timeupdate", timeUpdateListener)
        audio?.removeEventListener("canplaythrough", canPlayThroughListener)
        audio?.removeEventListener("error", errorListener)
        audio?.removeEventListener("ended", endedListener)
        audio?.pause()
        audio = null
    }

    actual fun seek(position: Float) {
        audio?.let {
            it.currentTime = position.toDouble()

            playerState = playerState.copy(currentTime = it.currentTime.toFloat())

            onProgressCallback(playerState)
        }
    }

    actual fun prepare(url: String) {
    }
}