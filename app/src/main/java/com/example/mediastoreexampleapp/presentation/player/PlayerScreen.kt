package com.example.mediastoreexampleapp.presentation.player

import android.content.ComponentName
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
        track = uiState.track,
        isPlaying = uiState.isPlaying,
        currentPosition = uiState.currentPosition,
        onChangePlaying = viewModel::changePlaying,
        onChangeCurrentPosition = viewModel::changeCurrentPosition,
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
    track: Track,
    isPlaying: Boolean,
    currentPosition: Long,
    onChangePlaying: (Boolean) -> Unit,
    onChangeCurrentPosition: (Long) -> Unit,
) {
    if (mediaController == null) return

    var sliderPosition by remember {
        mutableFloatStateOf(0.0f)
    }

    var isSeeking by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(track.uri) {
        val mediaItem = MediaItem.fromUri(track.uri)
        mediaController.setMediaItem(mediaItem)
        mediaController.prepare()
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val position = mediaController.currentPosition
            onChangeCurrentPosition(position)
            if (!isSeeking) {
                sliderPosition = position.toFloat()
            }
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

        Text(text = "${currentPosition.toTimeString()} / ${track.duration.toTimeString()}")

        Slider(
            value = sliderPosition,
            onValueChange = {
                isSeeking = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                val positionMs = sliderPosition.toLong()
                mediaController.seekTo(positionMs)
                isSeeking = false
            },
            valueRange = 0f..track.duration.toFloat()
        )

        Text(
            text = stringResource(
                R.string.left,
                (track.duration - currentPosition).toTimeString()
            )
        )
    }

    DisposableEffect(mediaController) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playining: Boolean) {
                onChangePlaying(playining)
            }

            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) {
                    val currentPosition = player.currentPosition
                    onChangeCurrentPosition(currentPosition)
                }
            }
        }

        mediaController.addListener(listener)

        onDispose {
            mediaController.removeListener(listener)
        }
    }
}