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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mediastoreexampleapp.R
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.presentation.player.extension.toTimeString
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    track: Track = Track(
        id = 40,
        title = "Он Не Знает Ничего",
        artist = "Краски",
        duration = 207256,
        uri = "content://media/external/audio/media/40".toUri()
    ) // TODO: убрать дефолт значение, используется для разработки первой рабочей версии
) {
    val context = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(context.applicationContext).build()
    }

    var isPlaying by remember {
        mutableStateOf(player.isPlaying)
    }

    var currentDuration by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(track.uri) {
        val mediaItem = MediaItem.fromUri(track.uri)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentDuration = player.currentPosition
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

        Text(text = "${currentDuration.toTimeString()} / ${track.duration.toTimeString()}")
        Text(text = stringResource(R.string.left, (track.duration - currentDuration).toTimeString()))
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