package com.example.player.model

import com.example.domain.model.PlayerState

data class PlayerScreenState(
    val playerState: PlayerState,
    val isTrackDownloading: Boolean = false,
)
