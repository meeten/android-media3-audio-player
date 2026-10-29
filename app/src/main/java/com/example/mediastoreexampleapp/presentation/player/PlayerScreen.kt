package com.example.mediastoreexampleapp.presentation.player

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediastoreexampleapp.R
import com.example.mediastoreexampleapp.core.LoadingImage
import com.example.mediastoreexampleapp.domain.model.Track


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
        imageUrl = uiState.currentTrack?.imageUrl,
        isFavorite = uiState.currentTrack?.isDownload ?: false,
        title = uiState.currentTrack?.title ?: stringResource(R.string.unknown),
        artist = uiState.currentTrack?.artist ?: stringResource(R.string.unknown),
        playbackDurationMs = uiState.durationMs,
        isPlaying = uiState.isPlaying,
        onFavorite = viewModel::download,
        onPlay = viewModel::play,
        onPause = viewModel::pause,
        currentPositionMs = uiState.currentPositionMs,
        onChangeCurrentPosition = viewModel::changeCurrentPosition,
        onNext = viewModel::next,
        onPrevious = viewModel::previous
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    currentTrack: Track?,
    imageUrl: String?,
    isFavorite: Boolean,
    title: String,
    artist: String,
    playbackDurationMs: Long,
    currentPositionMs: Long,
    isPlaying: Boolean,
    onFavorite: (Track) -> Unit,
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
        LoadingImage(
            modifier = Modifier
                .height(332.dp)
                .clip(RoundedCornerShape(16.dp))
                .align(Alignment.CenterHorizontally),
            imageUrl = imageUrl,
            loadingProgressIndicatorSize = 332.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    onFavorite(currentTrack)
                }
            ) {
                Image(
                    painter =
                        if (isFavorite) painterResource(R.drawable.ic_favorite_filled)
                        else painterResource(R.drawable.ic_favorite),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
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

        Spacer(modifier = Modifier.height(2.dp))

        PlayerSlider(
            modifier = Modifier.fillMaxWidth(),
            playbackDurationMs = playbackDurationMs,
            onChangeCurrentPosition = onChangeCurrentPosition,
            currentPositionMs = currentPositionMs,
            sliderEnabled = playbackDurationMs > 0,
        )

        Spacer(modifier = Modifier.height(12.dp))

        PlayerControls(
            modifier = Modifier.fillMaxWidth(),
            isPlaying = isPlaying,
            onPrevious = onPrevious,
            onPause = onPause,
            onPlay = onPlay,
            onNext = onNext
        )
    }
}