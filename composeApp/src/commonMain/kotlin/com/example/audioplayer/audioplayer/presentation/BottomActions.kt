package com.example.audioplayer.audioplayer.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BottomActions(
    onShuffleClick: () -> Unit,
    onQueueClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffleClick) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Losowo",
            )
        }
        IconButton(onClick = onQueueClick) {
            Icon(
                imageVector = Icons.Default.QueuePlayNext,
                contentDescription = "Kolejka",
            )
        }
        IconButton(onClick = onRepeatClick) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "Powtarzaj",
            )
        }
    }
}
