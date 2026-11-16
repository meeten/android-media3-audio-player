package com.example.mediastoreexampleapp.app.navigation

import com.example.mediastoreexampleapp.R

sealed class NavigationItem(
    val screen: Screen,
    val titleResId: Int,
    val src: Int
) {

    object LocalTracks : NavigationItem(
        screen = Screen.LocalTracks,
        titleResId = R.string.local_tracks,
        src = R.drawable.ic_download
    )

    object RemoteTracks : NavigationItem(
        screen = Screen.RemoteTracks,
        titleResId = R.string.remote_tracks,
        src = R.drawable.ic_queue_music
    )
}