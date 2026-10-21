package com.example.mediastoreexampleapp.domain.usecase

import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchLocalAudioUseCase @Inject constructor(
    private val repository: LocalAudioRepository
) {

    operator fun invoke(query: String): Flow<List<Track>> {
        val trimmed = query.trim()

        return if (trimmed.isEmpty()) {
            repository.tracksFlow
        } else {
            repository.tracksFlow.map { tracks ->
                tracks.filter { track ->
                    track.title.contains(trimmed, ignoreCase = true) ||
                            track.artist.contains(trimmed, ignoreCase = true)
                }
            }
        }
    }
}