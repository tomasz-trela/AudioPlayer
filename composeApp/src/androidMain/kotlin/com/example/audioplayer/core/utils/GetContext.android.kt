package com.example.audioplayer.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getContext(): Any? {
    return LocalContext.current
}