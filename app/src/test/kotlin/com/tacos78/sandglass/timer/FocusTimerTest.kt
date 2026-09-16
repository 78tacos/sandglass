package com.tacos78.sandglass.timer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeClock(var now: Long = 0L) {
    fun advance(millis: Long) {
        now += millis
    }
}

class FocusTimerTest {
    @Test
    fun startsIdleWithFullDuration() {
        val timer = FocusTimer(durationMillis = 25_000L)
        val snap = timer.snapshot()
        assertEquals(TimerStatus.Idle, snap.status)
        assertEquals(25_000L, snap.totalMillis)
        assertEquals(25_000L, snap.remainingMillis)
        assertEquals(1f, snap.remainingFraction, 0.0001f)
        assertEquals(0f, snap.elapsedFraction, 0.0001f)
    }

    @Test
    fun setDurationReplacesIdleSession() {
        val timer = FocusTimer(durationMillis = 10_000L)
        timer.setDuration(45_000L)
        val snap = timer.snapshot()
        assertEquals(TimerStatus.Idle, snap.status)
        assertEquals(45_000L, snap.totalMillis)
        assertEquals(45_000L, snap.remainingMillis)
    }

    @Test
    fun setDurationIsIgnoredWhileRunning() {
        val clock = FakeClock()
        val timer = FocusTimer(durationMillis = 10_000L, clock = { clock.now })
        timer.start()
        timer.setDuration(60_000L)
        val snap = timer.snapshot()
        assertEquals(TimerStatus.Running, snap.status)
        assertEquals(10_000L, snap.totalMillis)
        assertEquals(10_000L, snap.remainingMillis)
    }

    @Test
    fun tickAdvancesRemainingWhileRunning() {
        val clock = FakeClock()
        val timer = FocusTimer(durationMillis = 10_000L, clock = { clock.now })
        timer.start()
        clock.advance(4_000L)
        val snap = timer.tick()
        assertEquals(TimerStatus.Running, snap.status)
        assertEquals(6_000L, snap.remainingMillis)
        assertEquals(0.6f, snap.remainingFraction, 0.0001f)
    }

    @Test
    fun pauseFreezesRemaining() {
        val clock = FakeClock()
        val timer = FocusTimer(durationMillis = 10_000L, clock = { clock.now })
        timer.start()
        clock.advance(3_000L)
        timer.pause()
        clock.advance(5_000L)
        val snap = timer.tick()
        assertEquals(TimerStatus.Paused, snap.status)
        assertEquals(7_000L, snap.remainingMillis)
    }

    @Test
    fun resumeContinuesFromPausedRemaining() {
        val clock = FakeClock()
        val timer = FocusTimer(durationMillis = 10_000L, clock = { clock.now })
        timer.start()
        clock.advance(3_000L)
        timer.pause()
        clock.advance(8_000L)
        timer.start()
        clock.advance(2_000L)
        val snap = timer.tick()
        assertEquals(TimerStatus.Running, snap.status)
        assertEquals(5_000L, snap.remainingMillis)
    }

    @Test
    fun resetRestoresFullDurationAndIdle() {
        val clock = FakeClock()
        val timer = FocusTimer(durationMillis = 10_000L, clock = { clock.now })
        timer.start()
        clock.advance(4_000L)
        timer.tick()
        timer.reset()
        val snap = timer.snapshot()
        assertEquals(TimerStatus.Idle, snap.status)
        assertEquals(10_000L, snap.remainingMillis)
    }

    @Test
    fun completingClampsAtZeroAndFinishes() {
        val clock = FakeClock()
        val timer = FocusTimer(durationMillis = 5_000L, clock = { clock.now })
        timer.start()
        clock.advance(8_000L)
        val snap = timer.tick()
        assertEquals(TimerStatus.Finished, snap.status)
        assertEquals(0L, snap.remainingMillis)
        assertEquals(0f, snap.remainingFraction, 0.0001f)
        assertEquals(1f, snap.elapsedFraction, 0.0001f)
    }

    @Test
    fun startFromFinishedRestartsTheSession() {
        val clock = FakeClock()
        val timer = FocusTimer(durationMillis = 5_000L, clock = { clock.now })
        timer.start()
        clock.advance(5_000L)
        timer.tick()
        assertEquals(TimerStatus.Finished, timer.snapshot().status)
        timer.start()
        val snap = timer.snapshot()
        assertEquals(TimerStatus.Running, snap.status)
        assertEquals(5_000L, snap.remainingMillis)
    }

    @Test
    fun tickWhileIdleDoesNotConsumeTime() {
        val clock = FakeClock(now = 50_000L)
        val timer = FocusTimer(durationMillis = 10_000L, clock = { clock.now })
        clock.advance(9_000L)
        val snap = timer.tick()
        assertEquals(TimerStatus.Idle, snap.status)
        assertEquals(10_000L, snap.remainingMillis)
    }

    @Test
    fun durationIsClampedToSupportedRange() {
        val tooSmall = FocusTimer(durationMillis = 0L)
        assertEquals(FocusTimer.MIN_DURATION_MS, tooSmall.snapshot().totalMillis)
        val tooLarge = FocusTimer(durationMillis = FocusTimer.MAX_DURATION_MS + 60_000L)
        assertEquals(FocusTimer.MAX_DURATION_MS, tooLarge.snapshot().totalMillis)
    }

    @Test
    fun selectedMinutesMatchesWholeMinutes() {
        val timer = FocusTimer(durationMillis = 25L * 60_000L)
        assertEquals(25, timer.snapshot().selectedMinutes)
    }

    @Test
    fun remainingFractionStaysWithinUnitInterval() {
        val clock = FakeClock()
        val timer = FocusTimer(durationMillis = 1_000L, clock = { clock.now })
        timer.start()
        clock.advance(1_000L)
        val snap = timer.tick()
        assertTrue(snap.remainingFraction in 0f..1f)
        assertTrue(snap.elapsedFraction in 0f..1f)
    }
}
