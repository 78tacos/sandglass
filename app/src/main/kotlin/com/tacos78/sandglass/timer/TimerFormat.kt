package com.tacos78.sandglass.timer

/**
 * Formats remaining time for the focus display.
 *
 * Minutes never pad to two digits on the left so a 5-minute session reads
 * `5:00` rather than `05:00`. Hours appear only when needed.
 */
fun formatTimer(remainingMillis: Long): String {
    val totalSeconds = (remainingMillis.coerceAtLeast(0L) + 999L) / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
