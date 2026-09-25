package com.example.mediastoreexampleapp.presentation.player

import android.content.ComponentName
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.compose.material3.indicator.ProgressSlider
import com.example.mediastoreexampleapp.R
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.presentation.player.extension.toTimeString
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val context = LocalContext.current

    val sessionToken = remember {
        SessionToken(
            context.applicationContext, ComponentName(
                context,
                PlaybackService::class.java
            )
        )
    }
    val controllerFuture = remember {
        MediaController.Builder(context.applicationContext, sessionToken).buildAsync()
    }

    var mediaController by remember {
        mutableStateOf<MediaController?>(null)
    }

    LaunchedEffect(controllerFuture) {
        mediaController = controllerFuture.await()
    }

    PlayerScreen(
        modifier = modifier,
        mediaController = mediaController,
        playlist = uiState.playlist,
        startIndex = uiState.currentIndex,
        isPlaying = uiState.isPlaying,
        onChangePlaying = viewModel::changePlaying,
        onChangeCurrentIndex = viewModel::changeCurrentIndex
    )

    DisposableEffect(controllerFuture) {
        onDispose {
            MediaController.releaseFuture(controllerFuture)
        }
    }
}

@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    mediaController: MediaController?,
    playlist: List<Track>,
    startIndex: Int,
    isPlaying: Boolean,
    onChangePlaying: (Boolean) -> Unit,
    onChangeCurrentIndex: (Int) -> Unit
) {
    if (mediaController == null) return

    var currentPosition by remember { mutableLongStateOf(0L) }

    LaunchedEffect(playlist) {
        mediaController.setMediaItems(
            playlist.map { MediaItem.fromUri(it.uri) },
            startIndex,
            0
        )
        mediaController.prepare()
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = mediaController.currentPosition
            delay(1000.milliseconds)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                mediaController.play()
            },
            enabled = !isPlaying
        ) {
            Text(text = stringResource(R.string.play))
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                mediaController.pause()
            },
            enabled = isPlaying
        ) {
            Text(text = stringResource(R.string.pause))
        }

        val currentTrack = playlist[mediaController.currentMediaItemIndex]
        Text(text = "${currentPosition.toTimeString()} / ${currentTrack.duration.toTimeString()}")

        ProgressSlider(
            mediaController,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(
                R.string.left,
                (currentTrack.duration - currentPosition).toTimeString()
            )
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.previous),
                modifier = modifier.clickable {
                    mediaController.seekToPrevious()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.next),
                modifier = modifier.clickable {
                    mediaController.seekToNext()
                }
            )
        }
    }

    DisposableEffect(mediaController) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playining: Boolean) {
                onChangePlaying(playining)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                onChangeCurrentIndex(mediaController.currentMediaItemIndex)
            }
        }

        mediaController.addListener(listener)

        onDispose {
            mediaController.removeListener(listener)
        }
    }
}