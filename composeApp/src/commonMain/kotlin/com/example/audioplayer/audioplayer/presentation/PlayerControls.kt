package com.example.audioplayer.audioplayer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.audioplayer.core.presentation.md_primary

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSkipPreviousClick: () -> Unit,
    onSkipNextClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSkipPreviousClick, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Poprzedni utwór",
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(md_primary)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pauza" else "Odtwarzaj",
                modifier = Modifier.size(48.dp)
            )
        }

        IconButton(onClick = onSkipNextClick, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Następny utwór",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}