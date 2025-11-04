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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AudioPlayer actual constructor(
    context: Any?,
) {
    private val androidContext: Context

    init {
        require(context is Context) { "Expected a valid Android Context for 'context' parameter." }
        androidContext = context
    }

    private val mediaPlayer: ExoPlayer = ExoPlayer.Builder(androidContext).build()

    private val _playerState = MutableStateFlow(PlayerState())
    actual val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var progressJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updateStateFromPlayer()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateStateFromPlayer()
            if (isPlaying) startProgressUpdates() else stopProgressUpdates()
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e("AudioPlayer", "Playback error: ${error.errorCodeName}: ${error.message}")
            updateStateFromPlayer()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateStateFromPlayer()
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

    private fun updateStateFromPlayer() {
        val durationMs = mediaPlayer.duration
        val currentPosMs = mediaPlayer.currentPosition

        _playerState.update {
            it.copy(
                isPlaying = mediaPlayer.isPlaying,
                isBuffering = mediaPlayer.playbackState == Player.STATE_BUFFERING,
                duration = if (durationMs != C.TIME_UNSET) durationMs / 1000f else 0f,
                currentTime = if (currentPosMs != C.TIME_UNSET) currentPosMs / 1000f else 0f,
                currentPlayingResource = mediaPlayer.currentMediaItem?.mediaId
            )
        }
    }

    actual fun initPlaylist(songs: List<Song>) {
        val mediaItems = songs.mapIndexed { i, song ->
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
    }

    actual fun play(index: Int?) {
        if (index == null) {
            mediaPlayer.play()
            return
        }

        if (index < 0 || index >= mediaPlayer.mediaItemCount) return

        if (mediaPlayer.currentMediaItemIndex == index) {
            mediaPlayer.play()
        } else {
            mediaPlayer.seekToDefaultPosition(index)
            mediaPlayer.play()
        }
    }

    actual fun pause() {
        mediaPlayer.pause()
    }

    actual fun cleanUp() {
        stopProgressUpdates()
        mediaPlayer.removeListener(listener)
        mediaPlayer.release()
    }

    actual fun seek(position: Float) {
        mediaPlayer.seekTo((position * 1000).toLong())
    }

    actual fun playNext() {
        if (mediaPlayer.hasNextMediaItem()) {
            mediaPlayer.seekToNext()
            mediaPlayer.play()
        }
    }

    actual fun playPrevious() {
        if (mediaPlayer.hasPreviousMediaItem()) {
            mediaPlayer.seekToPrevious()
            mediaPlayer.play()
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = coroutineScope.launch {
            while (true) {
                updateStateFromPlayer()
                delay(200L)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }
}