package com.example.mediastoreexampleapp.domain.repository

import com.example.mediastoreexampleapp.domain.model.Track

interface RemoteAudioRepository {

    suspend fun getTracks(): List<Track>
}