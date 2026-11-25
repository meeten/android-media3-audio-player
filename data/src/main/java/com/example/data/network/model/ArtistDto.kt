package com.example.data.network.model

import com.google.gson.annotations.SerializedName

data class ArtistDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("picture") val smallImageUrl: String?,
    @SerializedName("picture_big") val largetImageUrl: String?,
)