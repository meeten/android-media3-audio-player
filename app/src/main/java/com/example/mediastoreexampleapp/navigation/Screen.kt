package com.example.mediastoreexampleapp.navigation

sealed class Screen(val route: String) {

    object LocalTracks : Screen(LOCAL_TRACKS_ROUTE)

    object RemoteTracks : Screen(REMOTE_TRACKS_ROUTE)

    object Player : Screen(PLAYER_ROUTE)

    private companion object {

        const val LOCAL_TRACKS_ROUTE = "local_tracks"
        const val REMOTE_TRACKS_ROUTE = "remote_tracks"
        const val PLAYER_ROUTE = "player"
    }
}