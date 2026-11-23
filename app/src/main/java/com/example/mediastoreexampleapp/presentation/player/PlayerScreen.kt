package com.example.mediastoreexampleapp.presentation.player

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediastoreexampleapp.R
import com.example.mediastoreexampleapp.core.Loading
import com.example.mediastoreexampleapp.core.LoadingPlayerImage
import com.example.mediastoreexampleapp.core.PlayerControls
import com.example.mediastoreexampleapp.core.PlayerSlider
import com.example.mediastoreexampleapp.core.extension.getSmallImageCacheKey
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.presentation.player.model.PlayerUiEvent
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val currentResource = rememberUpdatedState(LocalResources.current)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { playerUiEvent ->
            val message = when (playerUiEvent) {
                PlayerUiEvent.DownloadingFailed -> currentResource.value.getString(R.string.failed_to_download_track)

                PlayerUiEvent.RemovingFailed -> currentResource.value.getString(R.string.failed_to_delete_track)
            }

            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true
            )
        }
    }

    Scaffold(snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }) {
        PlayerScreen(
            modifier = modifier.padding(it),
            currentTrack = uiState.playerState.currentTrack,
            smallImageUrl = uiState.playerState.currentTrack?.smallImageUrl,
            largerImageUrl = uiState.playerState.currentTrack?.largetImageUrl,
            isDownloading = uiState.isTrackDownloading,
            isDownload = uiState.playerState.currentTrack?.isDownload ?: false,
            title = uiState.playerState.currentTrack?.title ?: stringResource(R.string.unknown),
            artist = uiState.playerState.currentTrack?.artist ?: stringResource(R.string.unknown),
            playbackDurationMs = uiState.playerState.durationMs,
            isPlaying = uiState.playerState.isPlaying,
            onDownload = viewModel::download,
            onPlay = viewModel::play,
            onPause = viewModel::pause,
            currentPositionMs = uiState.playerState.currentPositionMs,
            onChangeCurrentPosition = viewModel::changeCurrentPosition,
            onNext = viewModel::next,
            onPrevious = viewModel::previous
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    currentTrack: Track?,
    smallImageUrl: String?,
    largerImageUrl: String?,
    isDownloading: Boolean,
    isDownload: Boolean,
    title: String,
    artist: String,
    playbackDurationMs: Long,
    currentPositionMs: Long,
    isPlaying: Boolean,
    onDownload: (Track) -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onChangeCurrentPosition: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    if (currentTrack == null) {
        //TODO заменить на friendly user ui
        Log.d("PlayerScreen", "current track is null")
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        LoadingPlayerImage(
            modifier = Modifier
                .height(332.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.CenterHorizontally),
            largeImageUrl = largerImageUrl,
            trackListImageMemoryCacheKey = smallImageUrl?.getSmallImageCacheKey()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = {
                    onDownload(currentTrack)
                },
                enabled = !isDownloading
            ) {
                if (isDownloading) {
                    Loading(modifier = Modifier.size(28.dp))
                } else {
                    Icon(
                        painter =
                            if (isDownload) painterResource(R.drawable.ic_check_circle)
                            else painterResource(R.drawable.ic_download),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = if (isDownload) Color(0XFF6961f4) else Color.Black
                    )
                }
            }
        }

        Text(text = artist)

        Spacer(modifier = Modifier.height(8.dp))

        if (currentTrack.isPreview) {
            Box(
                modifier = Modifier.background(
                    color = Color(0xFFE9E5FF),
                    shape = RoundedCornerShape(32.dp)
                )
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_headphones),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF6855DE)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = stringResource(R.string.demo_30_sec),
                        color = Color(0xFF6855DE),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(if (currentTrack.isPreview) 2.dp else 16.dp))

        PlayerSlider(
            modifier = Modifier.fillMaxWidth(),
            playbackDurationMs = playbackDurationMs,
            onChangeCurrentPosition = onChangeCurrentPosition,
            currentPositionMs = currentPositionMs,
            sliderEnabled = playbackDurationMs > 0,
            isMiniPlayer = false
        )

        Spacer(modifier = Modifier.weight(1f))

        PlayerControls(
            modifier = Modifier.fillMaxWidth(),
            isPlaying = isPlaying,
            isMiniPlayer = false,
            onPrevious = onPrevious,
            onPause = onPause,
            onPlay = onPlay,
            onNext = onNext
        )
    }
}