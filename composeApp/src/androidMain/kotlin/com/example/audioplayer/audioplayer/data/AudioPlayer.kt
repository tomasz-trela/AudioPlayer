package com.example.audioplayer.audioplayer.data

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
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
    private val _playerState = MutableStateFlow(playerState)
    private val playerState = _playerState.asStateFlow()
    private var currentPlayingResource: String? = null
    private var progressJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val listener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _playerState.update { it.copy(isBuffering = true) }
                    updateMediaStatus()
                }

                Player.STATE_READY -> {
                    onReadyCallback()
                    val durationMs = mediaPlayer.duration
                    val durationSec = if (durationMs != C.TIME_UNSET) durationMs / 1000f else 0f
                    _playerState.update {
                        it.copy(isBuffering = false, duration = durationSec)
                    }
                    updateMediaStatus()
                }

                Player.STATE_ENDED -> {
                    stopProgressUpdates()
                    _playerState.update { it.copy(isPlaying = false) }
                }

                Player.STATE_IDLE -> {
                    _playerState.update { it.copy(isPlaying = false, isBuffering = false) }
                    updateMediaStatus()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.update { it.copy(isPlaying = isPlaying) }
            updateMediaStatus()
            if (isPlaying) startProgressUpdates() else stopProgressUpdates()
        }

        override fun onPlayerError(error: PlaybackException) {
            _playerState.update { it.copy(isBuffering = false, isPlaying = false) }
            onErrorCallback(Exception("Playback error: ${error.errorCodeName}: ${error.message}"))
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val uri = mediaItem?.localConfiguration?.uri

            currentPlayingResource = uri.toString()
            _playerState.update { it.copy(currentPlayingResource = currentPlayingResource) }
            updateMediaStatus()
        }
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        mediaPlayer.setAudioAttributes(audioAttributes, true)
        mediaPlayer.addListener(listener)
        mediaPlayer.repeatMode = Player.REPEAT_MODE_ALL
    }

    actual fun initPlaylist(songs: List<Song>) {
        val mediaItems = songs.mapIndexed { i, song ->
            Log.d("AudioPlayer", "[$i] uri=${song.url}")
            MediaItem.Builder()
                .setUri(song.url.toUri())
                .setMediaId(song.url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.author)
                        .build()
                )
                .build()
        }

        mediaPlayer.setMediaItems(mediaItems)
        mediaPlayer.prepare()
        mediaPlayer.playWhenReady = true

        currentPlayingResource = null
        _playerState.update {
            it.copy(currentPlayingResource = null, duration = 0f, currentTime = 0f)
        }
    }

    actual fun play(index: Int?) {
        if (index == null) {
            mediaPlayer.playWhenReady = true
            return
        }

        if (index < 0 || index >= mediaPlayer.mediaItemCount) return

        val currentIndex = mediaPlayer.currentMediaItemIndex
        if (currentIndex == index) {
            if (!_playerState.value.isPlaying) {
                mediaPlayer.playWhenReady = true
            }
            return
        }

        mediaPlayer.seekToDefaultPosition(index)
        mediaPlayer.playWhenReady = true

        val currentItem = mediaPlayer.getMediaItemAt(index)
        currentPlayingResource = currentItem.mediaId
        _playerState.update { it.copy(currentPlayingResource = currentItem.mediaId) }
        updateMediaStatus()
    }

    actual fun pause() {
        mediaPlayer.playWhenReady = false
        _playerState.update { it.copy(isPlaying = false) }
        updateMediaStatus()
    }

    actual fun cleanUp() {
        stopProgressUpdates()
        mediaPlayer.removeListener(listener)
        mediaPlayer.stop()
        mediaPlayer.release()
        currentPlayingResource = null
    }

    actual fun seek(position: Float) {
        mediaPlayer.seekTo((position * 1000).toLong())
    }

    actual fun playerState(): PlayerState {
        return playerState.value
    }

    actual fun playNext() {
        if (mediaPlayer.hasNextMediaItem()) {
            mediaPlayer.seekToNextMediaItem()
            mediaPlayer.playWhenReady = true
        } else if (mediaPlayer.repeatMode == Player.REPEAT_MODE_ALL) {
            mediaPlayer.seekToDefaultPosition(0)
            mediaPlayer.playWhenReady = true
        } else {
            mediaPlayer.playWhenReady = false
        }
    }

    actual fun playPrevious() {
        if (mediaPlayer.hasPreviousMediaItem()) {
            mediaPlayer.seekToPreviousMediaItem()
            mediaPlayer.playWhenReady = true
        } else if (mediaPlayer.repeatMode == Player.REPEAT_MODE_ALL) {
            val lastIndex = mediaPlayer.mediaItemCount - 1
            if (lastIndex >= 0) {
                mediaPlayer.seekToDefaultPosition(lastIndex)
                mediaPlayer.playWhenReady = true
            }
        } else {
            mediaPlayer.seekToDefaultPosition(mediaPlayer.currentMediaItemIndex)
        }
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
                delay(100L)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateMediaStatus() {
        val currentPosSec =
            if (mediaPlayer.currentPosition != C.TIME_UNSET) mediaPlayer.currentPosition / 1000f else 0f
        val durationSec =
            if (mediaPlayer.duration != C.TIME_UNSET) mediaPlayer.duration / 1000f else 0f
        val currentItem = mediaPlayer.currentMediaItem
        val currentMediaId = currentItem?.mediaId

        _playerState.update {
            it.copy(
                currentTime = currentPosSec,
                duration = durationSec,
                currentPlayingResource = currentMediaId,
                isPlaying = mediaPlayer.isPlaying
            )
        }

        onProgressCallback(_playerState.value)
    }
}