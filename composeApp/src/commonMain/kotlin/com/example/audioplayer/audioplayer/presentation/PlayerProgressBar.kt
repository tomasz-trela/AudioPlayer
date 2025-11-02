package com.example.audioplayer.audioplayer.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.audioplayer.core.utils.formatTime

@Composable
fun PlayerProgressBar(
    progress: Float,
    totalDuration: Float,
    onSeek: (Float) -> Unit
) {
    var sliderPosition by remember { mutableStateOf(progress) }
    var isUserSeeking by remember { mutableStateOf(false) }

    val onSeekUpdated by rememberUpdatedState(onSeek)

    LaunchedEffect(progress) {
        if (!isUserSeeking) {
            sliderPosition = progress
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = sliderPosition,
            onValueChange = {
                isUserSeeking = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                isUserSeeking = false
                onSeekUpdated(sliderPosition)
            },
            valueRange = 0f..totalDuration
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(sliderPosition), fontSize = 12.sp)
            Text(text = formatTime(totalDuration), fontSize = 12.sp)
        }
    }
}
