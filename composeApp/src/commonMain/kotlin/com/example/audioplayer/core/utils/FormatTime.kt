package com.example.audioplayer.core.utils

fun formatTime(seconds: Float): String {
    if (seconds.isNaN()) {
        return "0:00"
    }
    val minutes = (seconds / 60).toInt()
    val remainingSeconds = (seconds % 60).toInt()
    val secondsStr = remainingSeconds.toString().padStart(2, '0')
    return "$minutes:$secondsStr"
}

