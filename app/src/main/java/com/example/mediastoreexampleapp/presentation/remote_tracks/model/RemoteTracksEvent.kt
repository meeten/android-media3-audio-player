package com.example.mediastoreexampleapp.presentation.remote_tracks.model

sealed interface RemoteTracksEvent {

    data class Error(
        val messageResId: Int
    ) : RemoteTracksEvent
}