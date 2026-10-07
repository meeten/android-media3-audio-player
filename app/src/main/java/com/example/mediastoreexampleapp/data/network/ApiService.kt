package com.example.mediastoreexampleapp.data.network

import com.example.mediastoreexampleapp.data.network.model.TracksResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("chart")
    suspend fun loadTracks(): TracksResponseDto
}