package com.example.mediastoreexampleapp.data.network.model

import kotlinx.serialization.SerialName

data class TracksResponseDto(
    @SerialName("tracks") val tracks: DataDto
)

data class DataDto(
    @SerialName("data") val data: List<TrackDto>
)