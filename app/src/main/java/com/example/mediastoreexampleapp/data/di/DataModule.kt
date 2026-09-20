package com.example.mediastoreexampleapp.data.di

import android.content.Context
import com.example.mediastoreexampleapp.data.repository.LocalAudioRepositoryImpl
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun provideTracksRepository(@ApplicationContext context: Context): LocalAudioRepository {
        return LocalAudioRepositoryImpl(context.contentResolver)
    }
}