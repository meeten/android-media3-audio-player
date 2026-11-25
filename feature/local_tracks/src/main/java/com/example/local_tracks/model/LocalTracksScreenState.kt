package com.example.local_tracks.model

import com.example.domain.model.Track

sealed interface LocalTracksScreenState {

    object PermissionRequired : LocalTracksScreenState

    object Loading : LocalTracksScreenState

    data class Content(val tracks: List<Track>) : LocalTracksScreenState
}