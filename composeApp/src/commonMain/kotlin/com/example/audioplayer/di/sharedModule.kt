package com.example.audioplayer.di

import com.example.audioplayer.audioplayer.data.SongRepository
import com.example.audioplayer.audioplayer.domain.AudioPlayerViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sharedModule = module {
    singleOf(::SongRepository)
    factory { (context: Any?) ->
        AudioPlayerViewModel(context, get())
    }
}
