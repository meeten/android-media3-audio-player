package com.example.domain.model

sealed interface RemovingResult {

    data object Deleted : RemovingResult

    data class NeedsConfirmation(val requestId: String) : RemovingResult

    data object NotFound : RemovingResult

    data object Busy : RemovingResult

    data object Failed : RemovingResult
}