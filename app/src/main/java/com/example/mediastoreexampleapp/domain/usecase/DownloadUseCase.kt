package com.example.mediastoreexampleapp.domain.usecase

import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.model.TrackAvailabilityResult
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import javax.inject.Inject

class DownloadUseCase @Inject constructor(
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