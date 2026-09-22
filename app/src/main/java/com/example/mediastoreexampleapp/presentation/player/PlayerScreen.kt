package com.example.mediastoreexampleapp.presentation.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.media3.common.MediaItem
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

    LaunchedEffect(uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            modifier = modifier,
            onClick = {
                player.prepare()
                player.play()
            }
        ) {
            Text(text = stringResource(R.string.play))
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
}