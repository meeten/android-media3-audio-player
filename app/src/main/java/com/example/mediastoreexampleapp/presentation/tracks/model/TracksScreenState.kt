package com.example.mediastoreexampleapp.presentation.tracks.model

import com.example.mediastoreexampleapp.domain.model.Track

sealed interface TracksScreenState {

    object PermissionRequired : TracksScreenState

    object Loading : TracksScreenState

    data class Content(val tracks: List<Track>) : TracksScreenState
}