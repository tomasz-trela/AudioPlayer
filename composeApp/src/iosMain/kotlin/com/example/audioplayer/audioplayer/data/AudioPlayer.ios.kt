package com.example.audioplayer.audioplayer.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerItemStatusUnknown
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.currentItem
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
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
    private val onProgressCallback: (PlayerState) -> Unit,
    private val onReadyCallback: () -> Unit,
    private val onErrorCallback: (Exception) -> Unit,
    playerState: PlayerState,
    context: Any?
) {
    private val avPlayer: AVPlayer = AVPlayer()
    private var timeObserver: Any? = null
    private var playbackEndObserver: Any? = null

    private val _playerState = MutableStateFlow(playerState)
    private val playerState = _playerState.asStateFlow()

    private var songs: List<Song> = emptyList()
    private var currentIndex: Int = -1

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null

    init {
        setupAudioSession()
        startStatusPolling()
        addProgressTimeObserver()
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

    actual fun playerState(): PlayerState {
        return playerState.value
    }

    actual fun cleanUp() {
        pollingJob?.cancel()
        removeProgressTimeObserver()
        removePlaybackEndObserver()
        avPlayer.pause()
        avPlayer.replaceCurrentItemWithPlayerItem(null)
    }

    private fun loadTrack(at: Int, andPlay: Boolean) {
        if (at < 0 || at >= songs.size) return
        currentIndex = at

        removePlaybackEndObserver()

        val song = songs[currentIndex]
        val nsUrl =
            NSURL.URLWithString(song.url) ?: return onErrorCallback(Exception("Invalid URL"))
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

    private fun startStatusPolling() {
        pollingJob?.cancel()
        pollingJob = coroutineScope.launch {
            while (isActive) {
                val isPlaying = avPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying
                val currentItem = avPlayer.currentItem

                val currentStatus = currentItem?.status
                val isBuffering = currentStatus == AVPlayerItemStatusUnknown && isPlaying

                val newDuration =
                    if (currentItem?.duration?.let { CMTimeGetSeconds(it) }?.isNaN() == false) {
                        CMTimeGetSeconds(currentItem.duration).toFloat()
                    } else {
                        _playerState.value.duration
                    }

                _playerState.update {
                    it.copy(
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        duration = newDuration
                    )
                }

                when (currentStatus) {
                    AVPlayerItemStatusReadyToPlay -> onReadyCallback()
                    AVPlayerItemStatusFailed -> {
                        onErrorCallback(Exception("Failed to load media. Error: ${currentItem.error}"))
                        pause()
                    }

                    else -> Unit
                }
                delay(100L)
            }
        }
    }

    private fun setupAudioSession() {
        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
            audioSession.setActive(true, null)
        } catch (e: Exception) {
            onErrorCallback(e)
        }
    }

    private fun addProgressTimeObserver() {
        val interval = CMTimeMakeWithSeconds(0.1, NSEC_PER_SEC.toInt())
        timeObserver = avPlayer.addPeriodicTimeObserverForInterval(interval, null) { time ->
            val currentTime = CMTimeGetSeconds(time).toFloat()
            if (currentTime.isFinite() && _playerState.value.currentTime != currentTime) {
                _playerState.update { it.copy(currentTime = currentTime) }
                onProgressCallback(_playerState.value)
            }
        }
    }

    private fun removeProgressTimeObserver() {
        timeObserver?.let {
            avPlayer.removeTimeObserver(it)
            timeObserver = null
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