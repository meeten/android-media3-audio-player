package com.example.remote_tracks.model

import com.example.domain.model.Track

sealed interface RemoteTracksScreenState {

    object Loading : RemoteTracksScreenState

    data class Content(val tracks: List<Track>) : RemoteTracksScreenState

    data class Failure(val message: String?) : RemoteTracksScreenState
}