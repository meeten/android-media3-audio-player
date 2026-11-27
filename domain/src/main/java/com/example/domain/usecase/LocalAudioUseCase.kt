package com.example.domain.usecase

import com.example.domain.model.Track
import com.example.domain.repository.LocalAudioRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchLocalAudioUseCase @Inject constructor(
    private val repository: LocalAudioRepository
) {

    operator fun invoke(query: String): Flow<List<Track>?> {
        val trimmed = query.trim()

        return if (trimmed.isEmpty()) {
            repository.tracksFlow
        } else {
            repository.tracksFlow.map { tracks ->
                tracks?.filter { track ->
                    track.title.contains(trimmed, ignoreCase = true) ||
                            track.artist.contains(trimmed, ignoreCase = true)
                }
            }
        }
    }
}