package com.example.mediastoreexampleapp.core

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediastoreexampleapp.core.extension.toTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSlider(
    modifier: Modifier,
    playbackDurationMs: Long,
    onChangeCurrentPosition: (Long) -> Unit,
    currentPositionMs: Long,
    sliderEnabled: Boolean,
    isMiniPlayer: Boolean,
    activeTrackColor: Color = Color(0XFF6961f4),
    inactiveTrackColor: Color = Color(0xFFE2E5EB)
) {
    var sliderPosition by remember { mutableFloatStateOf(0.0f) }
    var isSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(currentPositionMs) {
        if (!isSeeking) sliderPosition = currentPositionMs.toFloat()
    }

    Slider(
        modifier = modifier.then(
            if (isMiniPlayer) Modifier.height(20.dp) else Modifier
        ),
        value = sliderPosition,
        onValueChange = {
            isSeeking = true
            sliderPosition = it
        },
        onValueChangeFinished = {
            onChangeCurrentPosition(sliderPosition.toLong())
            isSeeking = false
        },
        enabled = sliderEnabled,
        valueRange = 0f..if (playbackDurationMs > 0) playbackDurationMs.toFloat() else 0f,
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState,
                modifier = Modifier.height(6.dp),
                colors = SliderDefaults.colors(
                    activeTrackColor = activeTrackColor,
                    inactiveTrackColor = if (isMiniPlayer) Color(0xFFdbd6fc) else inactiveTrackColor,
                ),
                thumbTrackGapSize = 0.dp,
                drawStopIndicator = {}
            )
        },
        thumb = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White, CircleShape)
                    .border(4.dp, activeTrackColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {}
        }
    )

    if (!isMiniPlayer) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = currentPositionMs.toTimeString(),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = playbackDurationMs.toTimeString(),
                fontSize = 16.sp
            )
        }
    }
}