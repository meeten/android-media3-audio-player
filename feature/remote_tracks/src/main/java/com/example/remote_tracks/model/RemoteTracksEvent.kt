package com.example.remote_tracks.model

sealed interface RemoteTracksEvent {
    object DownloadFailed : RemoteTracksEvent

    object DeleteFailed : RemoteTracksEvent
}