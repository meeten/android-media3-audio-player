package com.example.mediastoreexampleapp.domain.model

import android.net.Uri

data class Track(
    val id: Long,
    val title: String?,
    val artist: String?,
    val duration: Long,
    val uri: Uri
)
