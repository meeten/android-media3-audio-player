package com.example.mediastoreexampleapp.presentation.player.extension

import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

fun Long.toTimeString(locale: Locale = Locale.getDefault()): String {
    return this.milliseconds.toComponents { minutes, seconds, _ ->
        String.format(locale, "%02d:%02d", minutes, seconds)
    }
}