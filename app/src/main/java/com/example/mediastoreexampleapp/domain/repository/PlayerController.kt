package com.example.mediastoreexampleapp.domain.repository

import com.example.mediastoreexampleapp.domain.model.PlayerState
import com.example.mediastoreexampleapp.domain.model.Track
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