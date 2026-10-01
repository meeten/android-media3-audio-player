package com.example.mediastoreexampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.mediastoreexampleapp.navigation.AppNavGraph
import com.example.mediastoreexampleapp.navigation.rememberNavigationState
import com.example.mediastoreexampleapp.presentation.player.PlayerScreen
import com.example.mediastoreexampleapp.presentation.tracks.TracksScreen
import com.example.mediastoreexampleapp.ui.theme.MediaStoreExampleAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaStoreExampleAppTheme {
                val navigationState = rememberNavigationState()

                Scaffold() { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavGraph(
                            navController = navigationState.navController,
                            tracksScreenContent = {
                                TracksScreen(
                                    onTrackClick = navigationState::navigateToPlayer
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