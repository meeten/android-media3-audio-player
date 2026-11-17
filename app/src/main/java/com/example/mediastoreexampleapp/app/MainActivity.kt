package com.example.mediastoreexampleapp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediastoreexampleapp.app.ui.BottomAppBarCustom
import com.example.mediastoreexampleapp.app.navigation.AppNavGraph
import com.example.mediastoreexampleapp.app.navigation.rememberNavigationState
import com.example.mediastoreexampleapp.data.storage.MediaDeletionCoordinator
import com.example.mediastoreexampleapp.presentation.deletion.MediaDeletionHost
import com.example.mediastoreexampleapp.presentation.local_tracks.LocalTracksScreen
import com.example.mediastoreexampleapp.presentation.player.PlayerScreen
import com.example.mediastoreexampleapp.presentation.remote_tracks.RemoteTracksScreen
import com.example.mediastoreexampleapp.ui.theme.MediaStoreExampleAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var mediaDeletionCoordinator: MediaDeletionCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaStoreExampleAppTheme {
                MediaDeletionHost(mediaDeletionCoordinator)
                val viewModel: MainViewModel = hiltViewModel()
                val playerState = viewModel.playerState.collectAsStateWithLifecycle().value
                val navigationState = rememberNavigationState()

                Scaffold(
                    bottomBar = {
                        BottomAppBarCustom(
                            navigationState = navigationState,
                            playerState = playerState,
                            onPrevious = viewModel::previous,
                            onPause = viewModel::pause,
                            onPlay = viewModel::play,
                            onNext = viewModel::next,
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
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