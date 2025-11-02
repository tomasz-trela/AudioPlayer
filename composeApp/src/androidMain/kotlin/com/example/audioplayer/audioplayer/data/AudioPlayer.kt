package com.example.audioplayer.audioplayer.data

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

actual class AudioPlayer actual constructor(
    private val onProgressCallback: (PlayerState) -> Unit,
    private val onReadyCallback: () -> Unit,
    private val onErrorCallback: (Exception) -> Unit,
    playerState: PlayerState,
    context: Any?,
) {

    private val androidContext: Context = when (context) {
        is Context -> context
        else -> throw IllegalArgumentException("Expected a valid Android Context for 'context' parameter.")
    }

    private var mediaPlayer: ExoPlayer = ExoPlayer.Builder(androidContext).build()

    private val mediaItems = mutableListOf<MediaItem>()

    private var currentItemIndex = -1

    private val _playerState = MutableStateFlow(PlayerState())

    private val playerState = _playerState.asStateFlow()

    private var currentPlayingResource: String? = null

    private var progressJob: Job? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val listener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {
                }

                Player.STATE_BUFFERING -> {
                    _playerState.update { it.copy(isBuffering = true) }
                    updateMediaStatus()
                }

                Player.STATE_READY -> {
                    onReadyCallback()

                    val durationMs = mediaPlayer.duration
                    val durationSec = if (durationMs != C.TIME_UNSET) durationMs / 1000f else 0f

                    _playerState.update {
                        it.copy(
                            isBuffering = false,
                            duration = durationSec
                        )
                    }
                    updateMediaStatus()
                }

                Player.STATE_ENDED -> {
                    stopProgressUpdates()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.update { it.copy(isPlaying = isPlaying) }
            updateMediaStatus()

            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playerState.update { it.copy(isBuffering = false) }
            onErrorCallback(error)
        }
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        mediaPlayer.setAudioAttributes(audioAttributes, true)
        mediaPlayer.addListener(listener)
    }


    actual fun play(url: String) {
        if (currentPlayingResource == url && !_playerState.value.isPlaying) {
            mediaPlayer.play()

        }

        prepare(url)
        mediaPlayer.play()
    }


    actual fun pause() {
        mediaPlayer.pause()
        _playerState.update { it.copy(isPlaying = false) }
        updateMediaStatus()
    }

    actual fun cleanUp() {
        mediaPlayer.stop()
        mediaPlayer.release()
        mediaPlayer.removeListener(listener)
        currentPlayingResource = null
        stopProgressUpdates()
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = coroutineScope.launch {
            while (_playerState.value.isPlaying) {
                val currentPos = mediaPlayer.currentPosition.toFloat()

                _playerState.update {
                    it.copy(currentTime = currentPos / 1000f)
                }

                onProgressCallback(_playerState.value)
                delay(timeMillis = 100L)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateMediaStatus() {
        val currentPosSec = mediaPlayer.currentPosition / 1000f
        _playerState.update { it.copy(currentTime = currentPosSec) }
        onProgressCallback(_playerState.value)
    }

    actual fun seek(position: Float) {
        mediaPlayer.seekTo((position * 1000).toLong())
    }


    actual fun playerState(): PlayerState {
        return playerState.value
    }

    actual fun prepare(url: String) {
        currentPlayingResource = url
        val mediaItem = MediaItem.fromUri(url.toUri())

        mediaPlayer.setMediaItem(mediaItem)
        mediaPlayer.prepare()

        _playerState.update {
            it.copy(
                currentPlayingResource = url,
            )
        }
        updateMediaStatus()
    }
}