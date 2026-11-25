package com.example.domain.repository

import com.example.domain.model.Track

interface RemoteAudioRepository {

    suspend fun getTracks(): List<Track>

    suspend fun search(query: String): List<Track>
}