package com.example.audioplayer.audioplayer.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun AlbumArt(modifier: Modifier = Modifier, url: String) {
    AsyncImage(
        model = url,
        contentDescription = null,
        placeholder = rememberVectorPainter(Icons.Default.MusicNote),
        contentScale = ContentScale.Crop,
        error = rememberVectorPainter(Icons.Default.MusicNote),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
    )

}
