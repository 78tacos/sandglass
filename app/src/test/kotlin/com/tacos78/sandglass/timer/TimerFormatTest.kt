package com.tacos78.sandglass.timer

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerFormatTest {
    @Test
    fun formatsExactMinutesWithoutHour() {
        assertEquals("25:00", formatTimer(25L * 60_000L))
        assertEquals("5:00", formatTimer(5L * 60_000L))
    }

    @Test
    fun formatsMinutesAndSeconds() {
        assertEquals("1:01", formatTimer(61_000L))
        assertEquals("0:01", formatTimer(1_000L))
    }

    @Test
    fun ceilsPartialSecondsSoTheLastSecondStaysVisible() {
        assertEquals("0:01", formatTimer(1L))
        assertEquals("0:00", formatTimer(0L))
    }

    @Test
    fun formatsHoursWhenNeeded() {
        assertEquals("1:01:01", formatTimer(3_661_000L))
    }

    @Test
    fun negativeInputIsTreatedAsZero() {
        assertEquals("0:00", formatTimer(-250L))
    }
}
