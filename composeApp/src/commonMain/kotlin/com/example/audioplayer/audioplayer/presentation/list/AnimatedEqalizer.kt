package com.example.audioplayer.audioplayer.presentation.list

import androidx.compose.animation.core.RepeatMode.Reverse
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedEqualizer(
    modifier: Modifier = Modifier,
    isPlayingNow: Boolean,
    barColor: Color = Color(0xFF4CAF50),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer-transition")

    val heights = (1..3).map { index ->
        val duration = 700 - (index * 150)
        val delay = index * 100

        val height by if (isPlayingNow) {
            infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = duration,
                        delayMillis = delay
                    ),
                    repeatMode = Reverse
                ),
                label = "bar-height-$index"
            )
        } else {
            remember { mutableStateOf(0.3f) }
        }

        height
    }

    Row(
        modifier = modifier.height(20.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { heightFraction ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp * heightFraction)
                    .background(barColor)
            )
            Spacer(modifier = Modifier.width(3.dp))
        }
    }
}
