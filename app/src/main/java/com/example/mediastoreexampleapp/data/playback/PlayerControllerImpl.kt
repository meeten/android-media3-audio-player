package com.example.mediastoreexampleapp.data.playback

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.example.mediastoreexampleapp.domain.model.PlayerState
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.playback.PlayerController
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class PlayerControllerImpl @Inject constructor(
    private val listenableFuture: ListenableFuture<MediaController>,
    private val coroutineScope: CoroutineScope
) : PlayerController {

    private val mediaControllerDeferred =
        coroutineScope.async {
            listenableFuture.await().also(::setMediaControllerListener)
        }

    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var timerJob: Job? = null


    override suspend fun setPlaylist(
        playlist: List<Track>,
        startIndex: Int
    ) {
        if (startIndex !in playlist.indices) return

        val controller = mediaControllerDeferred.await()

        _playerState.update {
            it.copy(
                currentTrack = playlist[startIndex],
                playlist = playlist,
            )
        }

        controller.setMediaItems(
            playlist.map {
                MediaItem.Builder()
                    .setMediaId(it.id.toString())
                    .setUri(it.uri.toUri())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(it.title)
                            .setArtist(it.artist)
                            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                            .build()
                    )
                    .build()
            },
            startIndex,
            START_POSITION_MS,
        )
        controller.prepare()
        controller.playWhenReady = true
    }

    override suspend fun play() {
        mediaControllerDeferred.await().play()
    }


    override suspend fun pause() {
        mediaControllerDeferred.await().pause()
    }

    override suspend fun setCurrentPosition(currentPositionMs: Long) {
        mediaControllerDeferred.await().seekTo(currentPositionMs)
    }

    override suspend fun next() {
        mediaControllerDeferred.await().seekToNext()
    }

    override suspend fun previous() {
        mediaControllerDeferred.await().seekToPrevious()
    }

    private fun setMediaControllerListener(controller: MediaController) {
        val listener = object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                _playerState.update {
                    it.copy(isPlaying = playWhenReady)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    startTimer(controller)
                } else {
                    timerJob?.cancel()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _playerState.update {
                        it.copy(durationMs = controller.duration)
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (mediaItem == null) return

                val currentTrackIndex = controller.currentMediaItemIndex

                _playerState.update { state ->
                    if (currentTrackIndex !in state.playlist.indices) {
                        state
                    } else {
                        state.copy(
                            currentTrack = state.playlist[currentTrackIndex],
                            currentPositionMs = START_POSITION_MS
                        )
                    }
                }
            }
        }

        controller.addListener(listener)
    }

    private fun startTimer(controller: MediaController) {
        timerJob?.cancel()

        timerJob = coroutineScope.launch {
            while (isActive) {
                _playerState.update {
                    it.copy(
                        currentPositionMs = controller.currentPosition
                    )
                }
                delay(ONE_SECOND_MS.milliseconds)
            }
        }
    }

    private companion object {

        const val START_POSITION_MS = 0L
        const val ONE_SECOND_MS = 1000L
    }
}