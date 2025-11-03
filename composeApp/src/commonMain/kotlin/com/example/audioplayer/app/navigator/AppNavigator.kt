package com.example.audioplayer.app.navigator

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.example.audioplayer.audioplayer.domain.AudioPlayerViewModel
import com.example.audioplayer.audioplayer.presentation.list.AudioListScreen

@Composable
fun AppNavigator(viewModel: AudioPlayerViewModel) {
    Navigator(AudioListScreen(viewModel)) { navigator ->
        val baseScreen = navigator.items.firstOrNull()
        val topScreen = navigator.lastItem
        val isTopDifferent = topScreen != baseScreen

        AnimatedScreenTransition(
            isTopDifferent = isTopDifferent,
            topScreen = { topScreen.Content() },
            baseScreen = { baseScreen?.Content() }
        )
    }
}
