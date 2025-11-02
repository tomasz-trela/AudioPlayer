package com.example.audioplayer.di

import com.example.audioplayer.audioList.data.SongRepository
import com.example.audioplayer.audioList.domain.AudioListViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    singleOf(::SongRepository)
    viewModelOf(::AudioListViewModel)
}
