package com.example.mediastoreexampleapp.domain.usecase

import com.example.mediastoreexampleapp.domain.model.OperationResult
import com.example.mediastoreexampleapp.domain.model.Track
import com.example.mediastoreexampleapp.domain.model.RemovingResult
import com.example.mediastoreexampleapp.domain.repository.LocalAudioRepository
import javax.inject.Inject

class DownloadUseCase @Inject constructor(
    private val repository: LocalAudioRepository
) {

    suspend operator fun invoke(track: Track): OperationResult {
        return if (track.isDownload) {
            getDeleteResult(track)
        } else {
            getDownloadResult(track)
        }
    }

    private suspend fun getDeleteResult(track: Track): OperationResult {
        val removingResult = repository.remove(track)
        return when (removingResult) {
            RemovingResult.Failed -> OperationResult.Delete.Failure

            RemovingResult.Deleted -> OperationResult.Delete.Success

            else -> OperationResult.Delete.Other
        }
    }

    private suspend fun getDownloadResult(track: Track): OperationResult {
        val isInsert = repository.insert(track)
        return if (isInsert) OperationResult.Download.Success else OperationResult.Download.Failure
    }

}