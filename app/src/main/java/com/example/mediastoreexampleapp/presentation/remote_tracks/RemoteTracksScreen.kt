package com.example.mediastoreexampleapp.presentation.remote_tracks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediastoreexampleapp.R
import com.example.mediastoreexampleapp.core.Loading
import com.example.mediastoreexampleapp.core.TracksScreenContent
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.presentation.remote_tracks.model.RemoteTracksScreenState

@Composable
fun RemoteTracksScreen(
    modifier: Modifier = Modifier,
    viewModel: RemoteTracksViewModel = hiltViewModel(),
    onTrackClick: (List<Track>, Track) -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    when (val currentState = uiState.value) {
        RemoteTracksScreenState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        is RemoteTracksScreenState.Content -> {
            TracksScreenContent(
                modifier = modifier,
                tracks = currentState.tracks,
                onTrackClick = { track ->
                    onTrackClick(currentState.tracks, track)
                }
            )
        }

        is RemoteTracksScreenState.Failure -> {
            Column {
                Text(
                    text = currentState.message ?: stringResource(R.string.unknown_error),
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    viewModel.retry()
                }) {
                    Text(text = stringResource(R.string.retry))
                }
            }
        }
    }
}