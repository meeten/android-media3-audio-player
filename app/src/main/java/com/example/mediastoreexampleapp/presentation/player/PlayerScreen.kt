package com.example.mediastoreexampleapp.presentation.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mediastoreexampleapp.R

@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    uri: String = "content://media/external/audio/media/40" // TODO: будет заменен на Track
) {
    val context = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(context.applicationContext).build()
    }

    var isPlaying by remember {
        mutableStateOf(player.isPlaying)
    }

    LaunchedEffect(uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
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
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playining: Boolean) {
                isPlaying = playining
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
}