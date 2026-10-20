package com.example.mediastoreexampleapp.domain.model

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val uri: String,
    val isPreview: Boolean = false
)
