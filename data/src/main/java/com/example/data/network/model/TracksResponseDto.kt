package com.example.data.network.model

import com.google.gson.annotations.SerializedName

data class TracksResponseDto(
    @SerializedName("tracks") val tracks: TracksDataDto
)

data class TracksDataDto(
    @SerializedName("data") val data: List<TrackDto>
)