package com.example.ui.extension

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

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