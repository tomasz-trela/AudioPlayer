package com.example.audioplayer.audioplayer.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.darwin.NSEC_PER_SEC

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual class AudioPlayer actual constructor(
    context: Any?
) {
    private val avPlayer: AVPlayer = AVPlayer()
    private var playbackEndObserver: Any? = null

    private val _playerState = MutableStateFlow(PlayerState())
    actual val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var songs: List<Song> = emptyList()
    private var currentIndex: Int = -1

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var statePollingJob: Job? = null

    init {
        setupAudioSession()
        startStatePolling()
    }

    actual fun initPlaylist(songs: List<Song>) {
        this.songs = songs
        currentIndex = if (songs.isNotEmpty()) 0 else -1
        if (currentIndex != -1) {
            loadTrack(at = currentIndex, andPlay = false)
        }
    }

    actual fun play(index: Int?) {
        val targetIndex = index ?: currentIndex
        if (targetIndex < 0 || targetIndex >= songs.size) return

        if (currentIndex == targetIndex && avPlayer.timeControlStatus != AVPlayerTimeControlStatusPlaying) {
            avPlayer.play()
            return
        }

        if (currentIndex != targetIndex) {
            loadTrack(at = targetIndex, andPlay = true)
        } else {
            avPlayer.play()
        }
    }

    actual fun pause() {
        avPlayer.pause()
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

    actual fun seek(position: Float) {
        val time = CMTimeMakeWithSeconds(position.toDouble(), NSEC_PER_SEC.toInt())
        avPlayer.seekToTime(time)
    }

    actual fun cleanUp() {
        statePollingJob?.cancel()
        removePlaybackEndObserver()
        avPlayer.pause()
        avPlayer.replaceCurrentItemWithPlayerItem(null)
    }

    private fun loadTrack(at: Int, andPlay: Boolean) {
        if (at < 0 || at >= songs.size) return
        currentIndex = at
        removePlaybackEndObserver()

        val song = songs[currentIndex]
        val nsUrl = NSURL.URLWithString(song.url) ?: return
        val playerItem = AVPlayerItem(uRL = nsUrl)

        _playerState.update {
            it.copy(currentPlayingResource = song.url, isBuffering = true)
        }

        addPlaybackEndObserver(playerItem)
        avPlayer.replaceCurrentItemWithPlayerItem(playerItem)

        if (andPlay) {
            avPlayer.play()
        }
    }

    private fun startStatePolling() {
        statePollingJob?.cancel()
        statePollingJob = coroutineScope.launch {
            while (isActive) {
                val currentItem = avPlayer.currentItem
                val isPlaying = avPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying
                val status = currentItem?.status
                val isBuffering =
                    status == null || (status == platform.AVFoundation.AVPlayerItemStatusUnknown && isPlaying)

                val duration = currentItem?.duration?.let { CMTimeGetSeconds(it) }
                    ?.takeIf { !it.isNaN() }?.toFloat() ?: _playerState.value.duration

                val currentTime = currentItem?.currentTime()?.let { CMTimeGetSeconds(it) }
                    ?.takeIf { !it.isNaN() }?.toFloat() ?: 0f

                if (status == AVPlayerItemStatusFailed) {
                    println("Player item failed to load: ${currentItem.error}")
                }

                _playerState.update {
                    it.copy(
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        duration = duration,
                        currentTime = currentTime
                    )
                }
                delay(200L)
            }
        }
    }

    private fun setupAudioSession() {
        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
            audioSession.setActive(true, null)
        } catch (e: Exception) {
            println("Failed to setup audio session: $e")
        }
    }

    private fun addPlaybackEndObserver(playerItem: AVPlayerItem) {
        playbackEndObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = playerItem,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            playNext()
        }
    }

    private fun removePlaybackEndObserver() {
        playbackEndObserver?.let {
            NSNotificationCenter.defaultCenter.removeObserver(it)
            playbackEndObserver = null
        }
    }
}