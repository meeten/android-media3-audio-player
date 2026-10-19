package com.example.mediastoreexampleapp.data.network.model

import com.google.gson.annotations.SerializedName

data class TracksResponseDto(
    @SerializedName("tracks") val tracks: DataDto
)

data class DataDto(
    @SerializedName("data") val data: List<TrackDto>
)