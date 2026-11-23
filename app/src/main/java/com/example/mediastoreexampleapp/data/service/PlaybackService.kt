package com.example.mediastoreexampleapp.data.service

import android.os.Process
import android.util.Log
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()

        Log.d(
            "PlaybackLifecycle",
            "onCreate(): pid = ${Process.myPid()}, service = ${System.identityHashCode(this)}, " +
                    "session = ${System.identityHashCode(mediaSession)}"
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        Log.d(
            "PlaybackLifecycle",
            "onDestroy(): pid = ${Process.myPid()}, service = ${System.identityHashCode(this)}, " +
                    "session = ${System.identityHashCode(mediaSession)}"
        )

        mediaSession.run {
            player.release()
            release()
        }
        super.onDestroy()
    }
}