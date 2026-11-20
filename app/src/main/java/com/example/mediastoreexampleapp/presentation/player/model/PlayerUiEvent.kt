package com.example.mediastoreexampleapp.presentation.player.model

sealed interface PlayerUiEvent {

    object DownloadingFailed: PlayerUiEvent

    object RemovingFailed: PlayerUiEvent
}