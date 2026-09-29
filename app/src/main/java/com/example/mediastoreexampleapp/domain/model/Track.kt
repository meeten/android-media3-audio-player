package com.example.mediastoreexampleapp.domain.model

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: String
)
