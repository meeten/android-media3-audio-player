package com.example.mediastoreexampleapp.domain.usecase

import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import javax.inject.Inject

class DownloadUseCase @Inject constructor(
    private val repository: LocalAudioRepository
) {

    suspend operator fun invoke(track: Track): Boolean {
        return if (track.isDownload) repository.remove(track) else repository.insert(track)
    }
}