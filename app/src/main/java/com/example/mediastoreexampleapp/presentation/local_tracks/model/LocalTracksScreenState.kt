package com.example.mediastoreexampleapp.presentation.local_tracks.model

import com.example.mediastoreexampleapp.domain.model.Track

sealed interface LocalTracksScreenState {

    object PermissionRequired : LocalTracksScreenState

    object Loading : LocalTracksScreenState

    data class Content(val tracks: List<Track>) : LocalTracksScreenState
}