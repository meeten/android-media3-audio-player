package com.example.mediastoreexampleapp.presentation.player.extension

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class TimerFormatterTest {

    @Test
    fun formatsZeroAsZeroZero() {
        assertEquals("00:00", 0L.toTimeString(Locale.US))
    }

    @Test
    fun zeroPadsSeconds() {
        assertEquals("00:05", 5_000L.toTimeString(Locale.US))
    }

    @Test
    fun formatsMinutesAndSeconds() {
        assertEquals("01:05", 65_000L.toTimeString(Locale.US))
    }

    @Test
    fun truncatesSubSecondRemainder() {
        assertEquals("00:29", 29_988L.toTimeString(Locale.US))
    }
}
