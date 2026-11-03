package com.example.mediastoreexampleapp.domain.repository

import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.model.RemovingResult
import com.example.mediastoreexampleapp.domain.model.TrackKey
import com.example.mediastoreexampleapp.domain.model.TrackOperation
import kotlinx.coroutines.flow.StateFlow

interface LocalAudioRepository {

    val tracksFlow: StateFlow<List<Track>?>

    val operationsFlow: StateFlow<Map<TrackKey, TrackOperation>>

    suspend fun insert(track: Track): Boolean

    suspend fun remove(track: Track): RemovingResult
}