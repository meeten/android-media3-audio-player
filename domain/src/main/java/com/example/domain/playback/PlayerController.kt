package com.example.domain.playback

import com.example.domain.model.PlayerState
import com.example.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {

    val playerState: StateFlow<PlayerState>

    suspend fun setPlaylist(playlist: List<Track>, startIndex: Int)

    suspend fun play()

    suspend fun pause()

    suspend fun setCurrentPosition(currentPositionMs: Long)

    suspend fun next()

    suspend fun previous()
}