package com.example.mediastoreexampleapp.presentation.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediastoreexampleapp.R
import com.example.mediastoreexampleapp.ui.theme.MediaStoreExampleAppTheme

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Image(
                painter = painterResource(R.drawable.ic_skip_previous),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color = Color(0XFF6961f4), shape = CircleShape)
                .clickable { if (isPlaying) onPause() else onPlay() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter =
                    if (isPlaying) painterResource(R.drawable.ic_pause)
                    else painterResource(R.drawable.ic_play_arrow),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onNext) {
            Image(
                painter = painterResource(R.drawable.ic_skip_next),
                contentDescription = null
            )
        }
    }
}

@Preview(name = "Playing State", showBackground = true)
@Composable
private fun PlayerControlsPlayingPreview() {
    MediaStoreExampleAppTheme {
        PlayerControls(
            isPlaying = true,
            onPrevious = {},
            onPause = {},
            onPlay = {},
            onNext = {}
        )
    }
}

@Preview(name = "Paused State", showBackground = true)
@Composable
private fun PlayerControlsPausedPreview() {
    MediaStoreExampleAppTheme {
        PlayerControls(
            isPlaying = false,
            onPrevious = {},
            onPause = {},
            onPlay = {},
            onNext = {}
        )
    }
}