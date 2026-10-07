package com.example.mediastoreexampleapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(
    navController: NavHostController,
    tracksScreenContent: @Composable () -> Unit,
    remoteTracksScreenContent: @Composable () -> Unit,
    playerScreenContent: @Composable () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.RemoteTracks.route
    ) {
        composable(route = Screen.Tracks.route) {
            tracksScreenContent()
        }
        composable(route = Screen.Player.route) {
            playerScreenContent()
        }
        composable(route = Screen.RemoteTracks.route) {
            remoteTracksScreenContent()
        }
    }
}