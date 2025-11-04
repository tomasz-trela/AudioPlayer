package com.example.audioplayer.audioplayer.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.w3c.dom.Audio
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.events.Event

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AudioPlayer actual constructor(
    private val onProgressCallback: (PlayerState) -> Unit,
    private val onReadyCallback: () -> Unit,
    private val onErrorCallback: (Exception) -> Unit,
    playerState: PlayerState,
    context: Any?,
) {
    private var audio: HTMLAudioElement? = Audio()

    private val _playerState = MutableStateFlow(playerState)
    private val playerState = _playerState.asStateFlow()

    private var songs: List<Song> = emptyList()
    private var currentIndex: Int = -1

    private val timeUpdateListener: (Event) -> Unit = {
        audio?.let {
            _playerState.update { state ->
                state.copy(currentTime = it.currentTime.toFloat())
            }
            onProgressCallback(_playerState.value)
        }
    }

    private val loadedMetadataListener: (Event) -> Unit = {
        audio?.let {
            _playerState.update { state ->
                state.copy(duration = it.duration.toFloat())
            }
        }
    }

    private val canPlayThroughListener: (Event) -> Unit = {
        _playerState.update { it.copy(isBuffering = false) }
        onReadyCallback()
    }

    private val waitingListener: (Event) -> Unit = {
        _playerState.update { it.copy(isBuffering = true) }
    }

    private val playingListener: (Event) -> Unit = {
        _playerState.update { it.copy(isPlaying = true, isBuffering = false) }
    }

    private val pauseListener: (Event) -> Unit = {
        _playerState.update { it.copy(isPlaying = false) }
    }

    private val errorListener: (Event) -> Unit = {
        _playerState.update { it.copy(isPlaying = false, isBuffering = false) }
        onErrorCallback(Exception("Error playing audio."))
    }

    private val endedListener: (Event) -> Unit = {
        playNext()
    }

    init {
        audio?.addEventListener("timeupdate", timeUpdateListener)
        audio?.addEventListener("loadedmetadata", loadedMetadataListener)
        audio?.addEventListener("canplaythrough", canPlayThroughListener)
        audio?.addEventListener("error", errorListener)
        audio?.addEventListener("ended", endedListener)
        audio?.addEventListener("waiting", waitingListener)
        audio?.addEventListener("playing", playingListener)
        audio?.addEventListener("pause", pauseListener)
        audio?.loop = false
    }

    actual fun initPlaylist(songs: List<Song>) {
        this.songs = songs
        currentIndex = if (songs.isNotEmpty()) 0 else -1
        _playerState.update {
            it.copy(
                currentPlayingResource = null,
                duration = 0f,
                currentTime = 0f,
                isPlaying = false
            )
        }
        if (currentIndex != -1) {
            audio?.src = songs[currentIndex].url
        }
    }

    actual fun play(index: Int?) {
        val targetIndex = index ?: currentIndex
        if (targetIndex < 0 || targetIndex >= songs.size) return

        if (currentIndex == targetIndex && !_playerState.value.isPlaying) {
            audio?.play()?.catch { e ->
                onErrorCallback(Exception("Error playing audio: $e"))
            }
            return
        }

        if (currentIndex != targetIndex || audio?.src != songs[targetIndex].url) {
            currentIndex = targetIndex
            val song = songs[currentIndex]
            audio?.src = song.url
            _playerState.update { it.copy(currentPlayingResource = song.url) }
        }

        audio?.play()?.catch { e ->
            onErrorCallback(Exception("Error playing audio: $e"))
        }
    }

    actual fun pause() {
        audio?.pause()
    }

    actual fun seek(position: Float) {
        audio?.let {
            it.currentTime = position.toDouble()
            _playerState.update { state -> state.copy(currentTime = it.currentTime.toFloat()) }
            onProgressCallback(_playerState.value)
        }
    }

    actual fun playNext() {
        if (songs.isEmpty()) return
        val nextIndex = if (currentIndex + 1 >= songs.size) 0 else currentIndex + 1
        play(nextIndex)
    }

    actual fun playPrevious() {
        if (songs.isEmpty()) return
        val prevIndex = if (currentIndex - 1 < 0) songs.size - 1 else currentIndex - 1
        play(prevIndex)
    }

    actual fun playerState(): PlayerState {
        return playerState.value
    }

    actual fun cleanUp() {
        audio?.removeEventListener("timeupdate", timeUpdateListener)
        audio?.removeEventListener("loadedmetadata", loadedMetadataListener)
        audio?.removeEventListener("canplaythrough", canPlayThroughListener)
        audio?.removeEventListener("error", errorListener)
        audio?.removeEventListener("ended", endedListener)
        audio?.removeEventListener("waiting", waitingListener)
        audio?.removeEventListener("playing", playingListener)
        audio?.removeEventListener("pause", pauseListener)
        audio?.pause()
        audio = null
    }
}