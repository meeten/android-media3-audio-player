package com.example.mediastoreexampleapp.domain.repository

import com.example.mediastoreexampleapp.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface LocalAudioRepository {

    val tracksFlow: StateFlow<List<Track>?>

    suspend fun insert(track: Track): Boolean

    fun remove(track:  Track): Boolean
}