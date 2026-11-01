package com.example.sleep

import android.content.Context
import com.example.player.RadioPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SleepTimerState(
    val isActive: Boolean = false,
    val selectedMinutes: Int = 30,
    val remainingSeconds: Long = 0L,
    val endTimestampMs: Long = 0L
) {
    val formattedRemaining: String
        get() {
            if (!isActive || remainingSeconds <= 0) return ""
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val displayShort: String
        get() {
            if (!isActive || remainingSeconds <= 0) return ""
            val minutes = (remainingSeconds + 59) / 60
            return "${minutes}m"
        }
}

object SleepTimerManager {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var countdownJob: Job? = null

    private val _timerState = MutableStateFlow(SleepTimerState())
    val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()

    fun startTimer(context: Context, minutes: Int) {
        if (minutes <= 0) {
            cancelTimer()
            return
        }

        countdownJob?.cancel()
        val totalSec = minutes * 60L
        val endMs = System.currentTimeMillis() + (totalSec * 1000L)

        _timerState.value = SleepTimerState(
            isActive = true,
            selectedMinutes = minutes,
            remainingSeconds = totalSec,
            endTimestampMs = endMs
        )

        RadioPlayerManager.updateSleepTimer(totalSec)

        countdownJob = scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val remaining = ((endMs - now) / 1000L).coerceAtLeast(0L)

                _timerState.update {
                    it.copy(
                        remainingSeconds = remaining,
                        isActive = remaining > 0
                    )
                }

                RadioPlayerManager.updateSleepTimer(if (remaining > 0) remaining else null)

                if (remaining <= 0) {
                    // Time is up: stop radio stream smoothly
                    RadioPlayerManager.stop(context)
                    _timerState.value = SleepTimerState()
                    RadioPlayerManager.updateSleepTimer(null)
                    break
                }

                delay(1000L)
            }
        }
    }

    fun addMinutes(context: Context, extraMinutes: Int) {
        val currentRemainingSec = _timerState.value.remainingSeconds
        val newTotalSec = currentRemainingSec + (extraMinutes * 60L)
        val newMinutes = ((newTotalSec + 59) / 60).toInt()
        startTimer(context, newMinutes)
    }

    fun cancelTimer() {
        countdownJob?.cancel()
        countdownJob = null
        _timerState.value = SleepTimerState()
        RadioPlayerManager.updateSleepTimer(null)
    }
}
