package com.example.mediastoreexampleapp.data.network

import com.example.mediastoreexampleapp.data.network.model.TracksDataDto
import com.example.mediastoreexampleapp.data.network.model.TracksResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("chart")
    suspend fun loadTracks(): TracksResponseDto

    @GET("search")
    suspend fun search(@Query("q") query: String): TracksDataDto
}