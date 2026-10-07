package com.example.mediastoreexampleapp.presentation.remote_tracks.model

import com.example.mediastoreexampleapp.domain.model.Track

sealed interface RemoteTracksScreenState {

    object Loading : RemoteTracksScreenState

    data class Content(val tracks: List<Track>) : RemoteTracksScreenState

    data class Failure(val message: String?) : RemoteTracksScreenState
}