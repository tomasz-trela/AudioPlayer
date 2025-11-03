package com.example.audioplayer.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.audioplayer.app.navigator.AppNavigator
import com.example.audioplayer.audioplayer.domain.AudioPlayerViewModel
import com.example.audioplayer.core.presentation.darkColorScheme
import com.example.audioplayer.core.utils.getContext
import com.example.audioplayer.di.sharedModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf


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
            val viewModel: AudioPlayerViewModel = koinInject { parametersOf(context) }

            AppNavigator(viewModel)
        }
    }
}

