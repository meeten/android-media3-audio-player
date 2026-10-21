package com.example.mediastoreexampleapp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.mediastoreexampleapp.navigation.AppNavGraph
import com.example.mediastoreexampleapp.navigation.rememberNavigationState
import com.example.mediastoreexampleapp.presentation.local_tracks.LocalTracksScreen
import com.example.mediastoreexampleapp.presentation.player.PlayerScreen
import com.example.mediastoreexampleapp.presentation.remote_tracks.RemoteTracksScreen
import com.example.mediastoreexampleapp.ui.theme.MediaStoreExampleAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaStoreExampleAppTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val navigationState = rememberNavigationState()

                Scaffold { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        AppNavGraph(
                            navController = navigationState.navController,
                            localTracksScreenContent = {
                                LocalTracksScreen(
                                    onTrackClick = { tracks, track ->
                                        viewModel.setPlaylistWithCurrentTrack(tracks, track)
                                        navigationState.navigateToPlayer()
                                    }
                                )
                            },
                            remoteTracksScreenContent = {
                                RemoteTracksScreen(
                                    onTrackClick = { tracks, track ->
                                        viewModel.setPlaylistWithCurrentTrack(tracks, track)
                                        navigationState.navigateToPlayer()
                                    }
                                )
                            },
                            playerScreenContent = {
                                PlayerScreen()
                            },
                        )
                    }
                }
            }
        }
    }
}