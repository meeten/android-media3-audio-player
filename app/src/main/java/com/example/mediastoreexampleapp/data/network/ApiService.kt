package com.example.mediastoreexampleapp.data.network

import com.example.mediastoreexampleapp.data.network.model.TracksResponseDto
import retrofit2.http.GET

interface ApiService {

    @GET("chart")
    suspend fun loadTracks(): TracksResponseDto
}