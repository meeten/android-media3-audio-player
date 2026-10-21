package com.example.mediastoreexampleapp.data.repository

import com.example.mediastoreexampleapp.data.mapper.toTrack
import com.example.mediastoreexampleapp.data.network.ApiService
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.repository.RemoteAudioRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAudioRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RemoteAudioRepository {

    override suspend fun getTracks(): List<Track> {
        val response = apiService.loadTracks()
        return response.tracks.data.map { it.toTrack() }
    }

    override suspend fun search(query: String): List<Track> {
        val dataDto = apiService.search(query)
        return dataDto.data.map { it.toTrack() }
    }
}