package com.example.audioplayer.audioList.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.audioplayer.audioList.data.Song
import com.example.audioplayer.audioplayer.domain.AudioPlayerUiState
import com.example.audioplayer.audioplayer.domain.AudioPlayerViewModel

@Composable
fun SongListBottomBar(playerViewModel: AudioPlayerViewModel) {
    val uiState by playerViewModel.uiState.collectAsState()


    Column {
        if (uiState.currentSong != null) {
            MiniPlayer(
                song = uiState.currentSong!!,
                playbackState = uiState,
                onPlayPause = { playerViewModel.togglePlayPause() },
                onSkipNext = { }
            )
        }
    }
}

@Composable
fun MiniPlayer(
    song: Song,
    playbackState: AudioPlayerUiState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://picsum.photos/300",
            contentDescription = null,
            placeholder = rememberVectorPainter(Icons.Default.MusicNote),
            contentScale = ContentScale.Crop,
            error = rememberVectorPainter(Icons.Default.MusicNote),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp)),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.author,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause"
            )
        }
        IconButton(onClick = onSkipNext) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Skip Next"
            )
        }
    }
}