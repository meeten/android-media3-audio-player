package com.example.mediastoreexampleapp.presentation.player

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    override

    fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(applicationContext).build()
        mediaSession = MediaSession.Builder(applicationContext, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        player.release()
        mediaSession.release()
        super.onDestroy()
    }
}