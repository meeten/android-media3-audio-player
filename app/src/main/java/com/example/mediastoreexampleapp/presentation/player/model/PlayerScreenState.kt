package com.example.mediastoreexampleapp.presentation.player.model

import androidx.core.net.toUri
import com.example.mediastoreexampleapp.domain.model.Track

data class PlayerScreenState(
    val track: Track = Track(
        id = 40,
        title = "Он Не Знает Ничего",
        artist = "Краски",
        duration = 207256,
        uri = "content://media/external/audio/media/40".toUri()
    ),
    val isPlaying: Boolean = false,
)
