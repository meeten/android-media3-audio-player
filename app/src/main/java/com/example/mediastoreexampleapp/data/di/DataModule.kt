package com.example.mediastoreexampleapp.data.di

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.mediastoreexampleapp.data.repository.LocalAudioRepositoryImpl
import com.example.mediastoreexampleapp.data.repository.PlayerControllerImpl
import com.example.mediastoreexampleapp.data.service.PlaybackService
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import com.example.mediastoreexampleapp.domain.repository.PlayerController
import com.google.common.util.concurrent.ListenableFuture
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindLocalAudioRepository(impl: LocalAudioRepositoryImpl): LocalAudioRepository

    @Binds
    fun bindPlayerController(impl: PlayerControllerImpl): PlayerController

    companion object {

        @Provides
        fun provideCoroutineScope(): CoroutineScope {
            return CoroutineScope(SupervisorJob() + Dispatchers.Main)
        }

        @Provides
        @Singleton
        fun provideControllerFuture(@ApplicationContext context: Context): ListenableFuture<MediaController> {
            val sessionToken = SessionToken(
                context,
                ComponentName(context, PlaybackService::class.java)
            )


            return MediaController.Builder(context, sessionToken).buildAsync()
        }
    }
}