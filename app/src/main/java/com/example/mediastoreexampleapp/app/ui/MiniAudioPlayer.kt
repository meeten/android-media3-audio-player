package com.example.mediastoreexampleapp.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mediastoreexampleapp.core.LoadingTrackListImage
import com.example.mediastoreexampleapp.core.TrackInfoLayout
import com.example.mediastoreexampleapp.core.PlayerControls
import com.example.mediastoreexampleapp.core.PlayerSlider

@Composable
fun MiniAudioPlayer(
    imageUrl: String?,
    title: String,
    artist: String,
    isPlaying: Boolean,
    playbackDurationMs: Long,
    currentPositionMs: Long,
    modifier: Modifier,
    onPrevious: () -> Unit,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit,
    onChangeCurrentPosition: (Long) -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingTrackListImage(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    smallImageUrl = imageUrl,
                    loadingProgressIndicatorSize = 0.dp,
                )

                Spacer(modifier = Modifier.width(8.dp))

                TrackInfoLayout(
                    title = title,
                    artist = artist
                )
            }

            PlayerControls(
                isPlaying = isPlaying,
                isMiniPlayer = true,
                onPrevious = onPrevious,
                onPause = onPause,
                onPlay = onPlay,
                onNext = onNext,
            )
        }

        PlayerSlider(
            modifier = Modifier.fillMaxWidth(),
            playbackDurationMs = playbackDurationMs,
            currentPositionMs = currentPositionMs,
            sliderEnabled = playbackDurationMs > 0,
            isMiniPlayer = true,
            onChangeCurrentPosition = onChangeCurrentPosition
        )
    }
}