package com.example.player.model

sealed interface PlayerUiEvent {

    object DownloadingFailed: PlayerUiEvent

    object RemovingFailed: PlayerUiEvent
}