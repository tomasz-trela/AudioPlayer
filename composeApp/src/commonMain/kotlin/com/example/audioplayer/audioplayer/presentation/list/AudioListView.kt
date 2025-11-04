package com.example.audioplayer.audioplayer.presentation.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.audioplayer.audioplayer.domain.AudioPlayerViewModel
import com.example.audioplayer.audioplayer.presentation.detail.AudioPlayerScreen

class AudioListScreen(private val playerViewModel: AudioPlayerViewModel) : Screen {
    @Composable
    override fun Content() {
        SongListView(playerViewModel)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListView(
    playerViewModel: AudioPlayerViewModel,
) {
    val uiState by playerViewModel.uiState.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Playlist"
                    )
                }
            )
        },
        bottomBar = {
            if (uiState.currentSong != null) {
                SongListBottomBar(
                    song = uiState.currentSong!!,
                    playbackState = uiState,
                    onPlayPause = { playerViewModel.togglePlayPause() },
                    onSkipNext = { playerViewModel.playNextSong() },
                    onClick = { navigator.push(AudioPlayerScreen(playerViewModel)) }
                )
            }

        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(uiState.songs.size) { index ->
                val song = uiState.songs[index]
                SongTile(
                    song,
                    index == uiState.songIndex,
                    uiState.isPlaying,
                    { playerViewModel.changeSong(index) })
            }
        }
    }
}

