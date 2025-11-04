package com.example.audioplayer.audioplayer.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.example.audioplayer.audioplayer.domain.AudioPlayerViewModel

@OptIn(ExperimentalVoyagerApi::class)
class AudioPlayerScreen(private val viewModel: AudioPlayerViewModel) : Screen {
    @Composable
    override fun Content() {
        AudioPlayerView(viewModel = viewModel)
    }
}

@Composable
fun AudioPlayerView(viewModel: AudioPlayerViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp
        val horizontalPadding = if (isCompact) 16.dp else 48.dp
        val albumSize = if (isCompact) maxWidth * 0.6f else 400.dp

        AudioPlayerContent(
            horizontalPadding = horizontalPadding,
            albumSize = albumSize,
            viewModel = viewModel
        )
    }
}

@Composable
fun AudioPlayerContent(
    horizontalPadding: Dp,
    albumSize: Dp,
    viewModel: AudioPlayerViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PlayerTopAppBar(
                onBackClick = { navigator.pop() },
                onMoreClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding)
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeContent)
                .widthIn(max = 800.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AsyncImage(
                model = uiState.currentSong?.imageUrl ?: "",
                contentDescription = null,
                placeholder = rememberVectorPainter(Icons.Default.MusicNote),
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(Icons.Default.MusicNote),
                modifier = Modifier.size(albumSize).aspectRatio(1f).clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            SongDetails(
                title = uiState.currentSong?.title ?: "Unknown title",
                artist = uiState.currentSong?.author ?: "Unknown artist"
            )

            Spacer(modifier = Modifier.height(24.dp))

            PlayerProgressBar(
                progress = uiState.progress,
                totalDuration = uiState.duration,
                onSeek = { viewModel.seek(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PlayerControls(
                isPlaying = uiState.isPlaying,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onSkipPreviousClick = { viewModel.playPreviousSong() },
                onSkipNextClick = { viewModel.playNextSong() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}