package com.example.audioplayer.audioplayer.data

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
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
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.darwin.NSEC_PER_SEC
import platform.darwin.NSObject
import platform.darwin.NSObjectProtocol

actual class AudioPlayer actual constructor(
    onProgressCallback: (PlayerState) -> Unit,
    onReadyCallback: () -> Unit,
    onErrorCallback: (Exception) -> Unit,
    playerState: PlayerState,
    context: Any?
) : NSObject() {

    private val onProgressCallback: (PlayerState) -> Unit = onProgressCallback
    private val onReadyCallback: () -> Unit = onReadyCallback
    private val onErrorCallback: (Exception) -> Unit = onErrorCallback
    private val avAudioPlayer: AVPlayer = AVPlayer()
    private var currentPlayingResource: String? = null
    private lateinit var timeObserver: Any
    private var playbackEndObserver: NSObjectProtocol? = null
    private val _playerState = MutableStateFlow(PlayerState())

    @OptIn(ExperimentalForeignApi::class)
    private val observer: (CValue<CMTime>) -> Unit = { time: CValue<CMTime> ->
        val currentTimeInSeconds = CMTimeGetSeconds(time)
        val totalDurationInSeconds =
            avAudioPlayer.currentItem?.duration?.let { CMTimeGetSeconds(it) } ?: Double.NaN

        if (!currentTimeInSeconds.isNaN() &&
            !totalDurationInSeconds.isNaN() &&
            totalDurationInSeconds > 0
        ) {
            _playerState.value = _playerState.value.copy(
                currentTime = currentTimeInSeconds.toFloat(),
                duration = totalDurationInSeconds.toFloat(),
                isPlaying = _playerState.value.isPlaying,
                isBuffering = _playerState.value.isBuffering,
                currentPlayingResource = currentPlayingResource
            )
            onProgressCallback(_playerState.value)
        } else {
            println("Skipping progress update due to invalid time values.")
        }
    }

    init {
        setUpAudioSession()
        _playerState.value = _playerState.value.copy(
            isPlaying = (avAudioPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying)
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun play(url: String) {
        if (currentPlayingResource == url && avAudioPlayer.currentItem != null &&
            avAudioPlayer.timeControlStatus != AVPlayerTimeControlStatusPlaying
        ) {
            avAudioPlayer.play()
            _playerState.value = _playerState.value.copy(
                isPlaying = true,
                isBuffering = false
            )
            onProgressCallback(_playerState.value)
            return
        }
    }

    actual fun pause() {
        avAudioPlayer.pause()
        _playerState.value = _playerState.value.copy(
            isPlaying = false,
            isBuffering = false,
            currentPlayingResource = currentPlayingResource
        )
        onProgressCallback(_playerState.value)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun setUpAudioSession() {
        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, null)
            audioSession.setActive(true, null)
        } catch (e: Exception) {
            println("Error setting up audio session: ${e.message}")
            onErrorCallback(e)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun startTimeObserver() {
        val interval = CMTimeMakeWithSeconds(0.1, NSEC_PER_SEC.toInt())
        timeObserver = avAudioPlayer.addPeriodicTimeObserverForInterval(interval, null, observer)

        playbackEndObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = avAudioPlayer.currentItem,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { _ ->
                _playerState.value = _playerState.value.copy(
                    isPlaying = false,
                    isBuffering = false,
                    currentPlayingResource = currentPlayingResource
                )
                onProgressCallback(_playerState.value)
            }
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun stop() {
        if (::timeObserver.isInitialized) {
            avAudioPlayer.removeTimeObserver(timeObserver)
        }
        playbackEndObserver?.let {
            NSNotificationCenter.defaultCenter.removeObserver(it)
            playbackEndObserver = null
        }

        avAudioPlayer.pause()
        _playerState.value = _playerState.value.copy(
            isPlaying = false,
            isBuffering = false,
            currentTime = 0f,
            currentPlayingResource = null
        )
        onProgressCallback(_playerState.value)

    }

    actual fun cleanUp() {
        stop()
    }

    actual fun playerState(): PlayerState {
        return _playerState.value
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun seek(position: Float) {
        val time = CMTimeMakeWithSeconds(position.toDouble(), NSEC_PER_SEC.toInt())
        avAudioPlayer.seekToTime(time)
    }

    actual fun prepare(url: String) {
        currentPlayingResource = url
        _playerState.value = _playerState.value.copy(
            isBuffering = true,
            currentPlayingResource = url
        )
        stop()
        startTimeObserver()

        try {
            val nsUrl = NSURL.URLWithString(url) ?: throw IllegalArgumentException("Invalid URL")
            val playItem = AVPlayerItem(uRL = nsUrl)

            avAudioPlayer.replaceCurrentItemWithPlayerItem(playItem)
            _playerState.value = _playerState.value.copy(
                isPlaying = true,
                isBuffering = false
            )
            onReadyCallback()
        } catch (e: Exception) {
            onErrorCallback(e)
            if (::timeObserver.isInitialized) {
                avAudioPlayer.removeTimeObserver(timeObserver)
            }
        }
    }
}