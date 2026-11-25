package com.example.domain.usecase

import com.example.domain.model.Track
import com.example.domain.model.TrackAvailabilityResult
import com.example.domain.repository.LocalAudioRepository
import jakarta.inject.Inject

class ToggleTrackAvailabilityUseCase @Inject constructor(
    private val repository: LocalAudioRepository
) {

    suspend operator fun invoke(track: Track): TrackAvailabilityResult {
        return if (track.isDownload) {
            val removingResult = repository.remove(track)
            TrackAvailabilityResult.Delete(
                removingResult = removingResult
            )
        } else {
            val isInsert = repository.insert(track)
            TrackAvailabilityResult.Download(
                isSuccess = isInsert
            )
        }
    }
}