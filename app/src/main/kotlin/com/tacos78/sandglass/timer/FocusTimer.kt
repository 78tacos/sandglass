package com.tacos78.sandglass.timer

/**
 * Wall-clock countdown used by the Sandglass UI.
 *
 * The clock is injected so unit tests can advance time without waiting.
 * Call [tick] to apply elapsed time while [TimerStatus.Running].
 */
class FocusTimer(
    durationMillis: Long = DEFAULT_DURATION_MS,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private var totalMillis: Long = sanitizeDuration(durationMillis)
    private var remainingMillis: Long = totalMillis
    private var status: TimerStatus = TimerStatus.Idle
    private var lastTickAt: Long = 0L

    fun snapshot(): TimerSnapshot = TimerSnapshot(
        totalMillis = totalMillis,
        remainingMillis = remainingMillis,
        status = status,
    )

    fun setDuration(millis: Long) {
        when (status) {
            TimerStatus.Idle, TimerStatus.Finished -> {
                totalMillis = sanitizeDuration(millis)
                remainingMillis = totalMillis
                status = TimerStatus.Idle
            }
            TimerStatus.Running, TimerStatus.Paused -> Unit
        }
    }

    fun start() {
        when (status) {
            TimerStatus.Idle, TimerStatus.Paused -> {
                if (remainingMillis <= 0L) return
                lastTickAt = clock()
                status = TimerStatus.Running
            }
            TimerStatus.Finished -> {
                remainingMillis = totalMillis
                lastTickAt = clock()
                status = TimerStatus.Running
            }
            TimerStatus.Running -> Unit
        }
    }

    fun pause() {
        when (status) {
            TimerStatus.Running -> {
                tick()
                if (status == TimerStatus.Running) {
                    status = TimerStatus.Paused
                }
            }
            TimerStatus.Idle, TimerStatus.Paused, TimerStatus.Finished -> Unit
        }
    }

    fun reset() {
        remainingMillis = totalMillis
        status = TimerStatus.Idle
        lastTickAt = 0L
    }

    fun tick(): TimerSnapshot {
        when (status) {
            TimerStatus.Running -> {
                val now = clock()
                val elapsed = (now - lastTickAt).coerceAtLeast(0L)
                lastTickAt = now
                remainingMillis = (remainingMillis - elapsed).coerceAtLeast(0L)
                if (remainingMillis == 0L) {
                    status = TimerStatus.Finished
                }
            }
            TimerStatus.Idle, TimerStatus.Paused, TimerStatus.Finished -> Unit
        }
        return snapshot()
    }

    private fun sanitizeDuration(millis: Long): Long =
        millis.coerceIn(MIN_DURATION_MS, MAX_DURATION_MS)

    companion object {
        const val DEFAULT_DURATION_MS: Long = 25L * 60_000L
        const val MIN_DURATION_MS: Long = 1_000L
        const val MAX_DURATION_MS: Long = 180L * 60_000L
        val PRESET_MINUTES: List<Int> = listOf(5, 10, 15, 25, 45)
    }
}
