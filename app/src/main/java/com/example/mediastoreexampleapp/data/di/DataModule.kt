package com.example.mediastoreexampleapp.data.di

import android.content.Context
import com.example.mediastoreexampleapp.data.repository.TracksRepositoryImpl
import com.example.mediastoreexampleapp.domain.repository.TracksRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun provideTracksRepository(@ApplicationContext context: Context): TracksRepository {
        return TracksRepositoryImpl(context.contentResolver)
    }
}