package com.example.audioplayer.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.Navigator
import com.example.audioplayer.audioList.presentation.AudioListScreen
import com.example.audioplayer.audioplayer.domain.AudioPlayerViewModel
import com.example.audioplayer.core.presentation.darkColorScheme
import com.example.audioplayer.core.utils.getContext
import com.example.audioplayer.di.sharedModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication


@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(sharedModule)
    }) {
        MaterialTheme(
            colorScheme = darkColorScheme,
        ) {
            val context = getContext()
            val viewModel = viewModel { AudioPlayerViewModel(context) }

            Navigator(AudioListScreen(viewModel))

        }
    }
}

