package com.example.mediastoreexampleapp.presentation.player.model

import com.example.mediastoreexampleapp.domain.model.PlayerState

data class PlayerScreenState(
    val playerState: PlayerState,
    val isTrackDownloading: Boolean = false,
)
