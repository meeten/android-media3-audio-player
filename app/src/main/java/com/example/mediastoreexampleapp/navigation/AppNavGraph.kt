package com.example.mediastoreexampleapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(
    navController: NavHostController,
    localTracksScreenContent: @Composable () -> Unit,
    remoteTracksScreenContent: @Composable () -> Unit,
    playerScreenContent: @Composable () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.RemoteTracks.route
    ) {
        composable(route = Screen.LocalTracks.route) {
            localTracksScreenContent()
        }
        composable(route = Screen.RemoteTracks.route) {
            remoteTracksScreenContent()
        }
        composable(route = Screen.Player.route) {
            playerScreenContent()
        }
    }
}