package com.example.mediastoreexampleapp.domain.model

sealed interface TrackAvailabilityResult {

    data class Download(val isSuccess: Boolean) : TrackAvailabilityResult

    data class Delete(val removingResult: RemovingResult) : TrackAvailabilityResult
}