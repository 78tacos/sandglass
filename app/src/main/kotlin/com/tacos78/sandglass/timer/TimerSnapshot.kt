package com.tacos78.sandglass.timer

data class TimerSnapshot(
    val totalMillis: Long,
    val remainingMillis: Long,
    val status: TimerStatus,
) {
    val remainingFraction: Float
        get() {
            if (totalMillis <= 0L) return 0f
            return (remainingMillis.toDouble() / totalMillis.toDouble())
                .toFloat()
                .coerceIn(0f, 1f)
        }

    val elapsedFraction: Float
        get() = 1f - remainingFraction

    val selectedMinutes: Int
        get() = (totalMillis / 60_000L).toInt().coerceAtLeast(0)
}
