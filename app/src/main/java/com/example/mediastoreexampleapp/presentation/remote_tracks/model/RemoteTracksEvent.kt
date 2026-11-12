package com.example.mediastoreexampleapp.presentation.remote_tracks.model

sealed interface RemoteTracksEvent {
    object DownloadFailed : RemoteTracksEvent

    object DeleteFailed : RemoteTracksEvent
}