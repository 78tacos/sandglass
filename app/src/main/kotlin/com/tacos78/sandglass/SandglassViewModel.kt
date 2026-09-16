package com.tacos78.sandglass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tacos78.sandglass.timer.FocusTimer
import com.tacos78.sandglass.timer.TimerSnapshot
import com.tacos78.sandglass.timer.TimerStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SandglassViewModel(
    private val timer: FocusTimer = FocusTimer(),
) : ViewModel() {
    private val _snapshot = MutableStateFlow(timer.snapshot())
    val snapshot: StateFlow<TimerSnapshot> = _snapshot.asStateFlow()

    private val _secondTick = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val secondTick: SharedFlow<Unit> = _secondTick.asSharedFlow()

    private var lastAnnouncedSecond: Long = -1L

    init {
        viewModelScope.launch {
            while (isActive) {
                val next = timer.tick()
                _snapshot.value = next
                announceSecondIfNeeded(next)
                delay(TICK_INTERVAL_MS)
            }
        }
    }

    fun startOrPause() {
        when (snapshot.value.status) {
            TimerStatus.Running -> timer.pause()
            TimerStatus.Idle, TimerStatus.Paused, TimerStatus.Finished -> timer.start()
        }
        _snapshot.value = timer.snapshot()
    }

    fun reset() {
        timer.reset()
        lastAnnouncedSecond = -1L
        _snapshot.value = timer.snapshot()
    }

    fun setDurationMinutes(minutes: Int) {
        timer.setDuration(minutes.toLong() * 60_000L)
        lastAnnouncedSecond = -1L
        _snapshot.value = timer.snapshot()
    }

    private fun announceSecondIfNeeded(next: TimerSnapshot) {
        when (next.status) {
            TimerStatus.Running -> {
                val second = next.remainingMillis / 1000L
                if (second != lastAnnouncedSecond) {
                    lastAnnouncedSecond = second
                    _secondTick.tryEmit(Unit)
                }
            }
            TimerStatus.Idle, TimerStatus.Paused, TimerStatus.Finished -> {
                lastAnnouncedSecond = -1L
            }
        }
    }

    private companion object {
        const val TICK_INTERVAL_MS = 50L
    }
}
