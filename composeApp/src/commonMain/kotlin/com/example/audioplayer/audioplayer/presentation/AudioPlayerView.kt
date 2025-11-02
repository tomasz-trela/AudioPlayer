// AudioPlayerView.kt
package com.example.audioplayer.audioplayer.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.example.audioplayer.audioplayer.domain.AudioPlayerViewModel

class AudioPlayerScreen(private val viewModel: AudioPlayerViewModel) : Screen {

    @Composable
    override fun Content() {
        AudioPlayerView(viewModel = viewModel)
    }

}

@Composable
fun AudioPlayerView(viewModel: AudioPlayerViewModel) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isCompact = maxWidth < 600.dp
        val horizontalPadding = if (isCompact) 16.dp else 48.dp
        val albumSize = if (isCompact) maxWidth * 0.8f else 400.dp

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PlayerTopAppBar(onBackClick = {}, onMoreClick = {})
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding)
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeContent)
                .widthIn(max = 800.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AlbumArt(
                url = "https://picsum.photos/600/600",
                modifier = Modifier.size(albumSize)
            )

            Spacer(modifier = Modifier.height(32.dp))

            SongDetails(title = "Lost in the Echo", artist = "Linkin Park")

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
                onSkipPreviousClick = {},
                onSkipNextClick = {}
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
