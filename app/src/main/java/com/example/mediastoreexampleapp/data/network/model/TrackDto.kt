package com.example.mediastoreexampleapp.data.network.model

import com.google.gson.annotations.SerializedName

data class TrackDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("duration") val durationSeconds: Long,
    @SerializedName("picture") val imageUrl: String,
    @SerializedName("preview") val preview: String,
    @SerializedName("artist") val artistDto: ArtistDto
)
