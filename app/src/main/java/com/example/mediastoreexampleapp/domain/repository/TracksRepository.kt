package com.example.mediastoreexampleapp.domain.repository

import com.example.mediastoreexampleapp.domain.model.Track

interface TracksRepository {

    suspend fun getTracks(): List<Track>
}