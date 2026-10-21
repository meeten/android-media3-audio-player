package com.example.mediastoreexampleapp.presentation.player

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediastoreexampleapp.R
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.core.extension.toTimeString


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    PlayerScreen(
        modifier = modifier,
        currentTrack = uiState.currentTrack,
        playbackDurationMs = uiState.durationMs,
        isPlaying = uiState.isPlaying,
        onPlay = viewModel::play,
        onPause = viewModel::pause,
        currentPositionMs = uiState.currentPositionMs,
        onChangeCurrentPosition = viewModel::changeCurrentPosition,
        onNext = viewModel::next,
        onPrevious = viewModel::previous
    )
}

@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    currentTrack: Track?,
    playbackDurationMs: Long,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    currentPositionMs: Long,
    onChangeCurrentPosition: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    if (currentTrack == null) {
        //TODO заменить на friendly user ui
        Log.d("PlayerScreen", "current track is null")
        return
    }

    var sliderPosition by remember { mutableFloatStateOf(0.0f) }
    var isSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(currentPositionMs) {
        if (!isSeeking) sliderPosition = currentPositionMs.toFloat()
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
            onClick = onPlay,
            enabled = !isPlaying
        ) {
            Text(text = stringResource(R.string.play))
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onPause,
            enabled = isPlaying
        ) {
            Text(text = stringResource(R.string.pause))
        }

        if (currentTrack.isPreview) {
            Text(
                text = stringResource(R.string.demo_30_sec),
                color = Color.Blue
            )
        }

        Slider(
            value = sliderPosition,
            onValueChange = {
                isSeeking = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                onChangeCurrentPosition(sliderPosition.toLong())
                isSeeking = false
            },
            enabled = playbackDurationMs > 0,
            valueRange = 0f..if (playbackDurationMs > 0) playbackDurationMs.toFloat() else 0f
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = currentPositionMs.toTimeString())

            Spacer(modifier = Modifier.weight(1f))

            Text(text = playbackDurationMs.toTimeString())
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.previous),
                modifier = Modifier.clickable {
                    onPrevious()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.next),
                modifier = Modifier.clickable {
                    onNext()
                }
            )
        }
    }
}