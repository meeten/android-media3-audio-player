package com.example.mediastoreexampleapp.domain.model

data class PlayerState(
    val playlist: List<Track> = emptyList(),
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L
)