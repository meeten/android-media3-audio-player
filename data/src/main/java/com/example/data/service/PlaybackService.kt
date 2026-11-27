package com.example.data.service

import android.os.Process
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()

        Log.d(
            "PlaybackLifecycle",
            "onCreate(): pid = ${Process.myPid()}, service = ${System.identityHashCode(this)}, " +
                    "session = ${System.identityHashCode(mediaSession)}, " +
                    "player = ${System.identityHashCode(mediaSession.player)}"
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        Log.d(
            "PlaybackLifecycle",
            "onDestroy(): pid = ${Process.myPid()}, service = ${System.identityHashCode(this)}, " +
                    "session = ${System.identityHashCode(mediaSession)}, " +
                    "player = ${System.identityHashCode(mediaSession.player)}"
        )

        mediaSession.run {
            player.release()
            release()
        }
        super.onDestroy()
    }
}