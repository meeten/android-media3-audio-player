package com.example.data.repository

import com.example.data.mapper.toTrack
import com.example.data.network.ApiService
import com.example.domain.model.Track
import com.example.domain.repository.RemoteAudioRepository
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