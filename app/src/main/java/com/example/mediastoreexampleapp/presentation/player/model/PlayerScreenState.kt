package com.example.mediastoreexampleapp.presentation.player.model

import androidx.core.net.toUri
import com.example.mediastoreexampleapp.domain.model.Track

val tracks = listOf(
    Track(
        id = 33,
        title = "Пора домой",
        artist = "Сектор Газа",
        duration = 234449,
        uri = "content://media/external/audio/media/33".toUri()
    ),
    Track(
        id = 34,
        title = "Позови меня с собой",
        artist = "Palina",
        duration = 168464,
        uri = "content://media/external/audio/media/34".toUri()
    ),
    Track(
        id = 35,
        title = "Я свободен",
        artist = "Кипелов",
        duration = 433136,
        uri = "content://media/external/audio/media/35".toUri()
    ),
    Track(
        id = 36,
        title = "Ты меня не ищи",
        artist = "Вирус",
        duration = 290429,
        uri = "content://media/external/audio/media/36".toUri()
    ),
    Track(
        id = 37,
        title = "Равнодушие",
        artist = "МАЛЬБЭК/Сюзанна",
        duration = 188578,
        uri = "content://media/external/audio/media/37".toUri()
    ),
    Track(
        id = 40,
        title = "Он Не Знает Ничего",
        artist = "Краски",
        duration = 207256,
        uri = "content://media/external/audio/media/40".toUri()
    ),
    Track(
        id = 42,
        title = "Крошка моя",
        artist = "Руки Вверх!",
        duration = 250723,
        uri = "content://media/external/audio/media/42".toUri()
    )
)

data class PlayerScreenState(
    val playlist: List<Track> = tracks,
    val currentIndex: Int = 3,
    val isPlaying: Boolean = false,
)
