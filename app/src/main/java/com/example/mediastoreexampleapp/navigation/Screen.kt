package com.example.mediastoreexampleapp.navigation

sealed class Screen(val route: String) {

    object Tracks : Screen(TRACKS_ROUTE)

    object RemoteTracks : Screen(REMOTE_TRACKS_ROUTE)

    object Player : Screen(PLAYER_ROUTE)

    private companion object {

        const val TRACKS_ROUTE = "tracks"
        const val REMOTE_TRACKS_ROUTE = "remote_tracks"
        const val PLAYER_ROUTE = "player"
    }
}