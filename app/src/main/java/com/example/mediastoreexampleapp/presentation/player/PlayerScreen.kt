package com.example.mediastoreexampleapp.presentation.player

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
import androidx.media3.exoplayer.ExoPlayer
import com.example.mediastoreexampleapp.R
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.presentation.player.extension.toTimeString
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val context = LocalContext.current

    //TODO: решить проблему с remember для создания ExoPlayer(case: смена конфигурации)
    val player = remember {
        ExoPlayer.Builder(context.applicationContext).build()
    }

    PlayerScreen(
        modifier = modifier,
        player = player,
        track = uiState.track,
        isPlaying = uiState.isPlaying,
        currentPosition = uiState.currentPosition,
        onChangePlaying = viewModel::changePlaying,
        onChangeCurrentPosition = viewModel::changeCurrentPosition,
    )
}

@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    player: ExoPlayer,
    track: Track,
    isPlaying: Boolean,
    currentPosition: Long,
    onChangePlaying: (Boolean) -> Unit,
    onChangeCurrentPosition: (Long) -> Unit,
) {
    var sliderPosition by remember {
        mutableFloatStateOf(0.0f)
    }

    var isSeeking by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(track.uri) {
        val mediaItem = MediaItem.fromUri(track.uri)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val position = player.currentPosition
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
                player.play()
            },
            enabled = !isPlaying
        ) {
            Text(text = stringResource(R.string.play))
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                player.pause()
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
                player.seekTo(positionMs)
                onChangeCurrentPosition(positionMs)
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

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playining: Boolean) {
                onChangePlaying(playining)
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
}