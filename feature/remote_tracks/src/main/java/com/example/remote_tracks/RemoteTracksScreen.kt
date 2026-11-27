package com.example.remote_tracks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Track
import com.example.remote_tracks.model.RemoteTracksEvent
import com.example.remote_tracks.model.RemoteTracksScreenState
import com.example.ui.Loading
import com.example.ui.SearchBarCustom
import com.example.ui.TracksScreenContent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RemoteTracksScreen(
    modifier: Modifier = Modifier,
    viewModel: RemoteTracksViewModel = hiltViewModel(),
    onTrackClick: (List<Track>, Track) -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val currentResource = rememberUpdatedState(LocalResources.current)
    val snackbarHostState = remember { SnackbarHostState() }
    val query = viewModel.queryFlow.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest {
            val message = when (it) {
                is RemoteTracksEvent.DeleteFailed -> currentResource.value.getString(com.example.ui.R.string.failed_to_delete_track)

                is RemoteTracksEvent.DownloadFailed -> currentResource.value.getString(com.example.ui.R.string.failed_to_download_track)
            }
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            when (val currentState = uiState.value) {
                RemoteTracksScreenState.Loading -> {
                    Loading(modifier = Modifier.fillMaxSize())
                }

                is RemoteTracksScreenState.Content -> {
                    Column(modifier) {
                        SearchBarCustom(
                            modifier = Modifier.padding(8.dp),
                            query = query.value,
                            onSearch = viewModel::onChangeQuery
                        )

                        TracksScreenContent(
                            tracks = currentState.tracks,
                            onTrackClick = { track ->
                                onTrackClick(currentState.tracks, track)
                            },
                            onDownload = viewModel::download
                        )
                    }
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
    }
}