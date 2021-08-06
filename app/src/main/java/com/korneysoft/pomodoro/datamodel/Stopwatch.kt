package com.korneysoft.pomodoro.datamodel

import com.korneysoft.pomodoro.utils.getCurrentTime
import com.korneysoft.pomodoro.utils.getStopwatchCurrentTime

data class Stopwatch(
    val id: Int,
    var periodMs: Long,
    private val _currentMs: Long,
    private var _isStarted: Boolean,
    private var _isFinished: Boolean,
    var startTime: Long = 0,
    var leftTime: Long = 0 // left time to the finish
) {

    var onChangeCurrentTime: (() -> Unit)? = null
    var onFinished: (() -> Unit)? = null
    var onAfterFinished: (() -> Unit)? = null
    var onStarted: (() -> Unit)? = null
    var onStopped: (() -> Unit)? = null

    var currentMs = _currentMs
        set(value) {
            field = if (value <= 0) {
                0
            } else {
                value
            }
            onChangeCurrentTime?.invoke()
            if (field == 0L) {
                finish()
            }
        }

    var isFinished = _isFinished
        set(value) {
            if (field != value) {
                if (value) {
                    field = true
                    stop()
                    onFinished?.invoke()
                    onAfterFinished?.invoke()
                } else {
                    field = false
                }
            }
        }


    var isStarted = _isStarted
        set(value) {
            if (field != value) {
                if (value) {
                    doBeforeStart()
                    isFinished = false
                    field = true
                    startTime = getCurrentTime()
                    onStarted?.invoke()
                } else {
                    field = false
                    onStopped?.invoke()
                }
            }
        }

private fun doBeforeStart() {
    if (isFinished) {   // if stopwatch was finished early
        leftTime = periodMs
        currentMs = periodMs
    } else {
        leftTime = currentMs
    }
}

fun stop() {
    isStarted = false
}

private fun finish() {
    isFinished = true
}

fun doTickTimer() {
    if (isStarted) {
        currentMs = getStopwatchCurrentTime(startTime, leftTime)
    }
}

override fun equals(other: Any?): Boolean {
    //return super.equals(other)
    return if (other is Stopwatch) {
        equalsID(other) && equalsContent(other)
    } else {
        false
    }
}


fun equalsID(other: Stopwatch): Boolean {
    return (id == other.id)
}

fun equalsContent(other: Stopwatch): Boolean {
    return (periodMs == other.periodMs) &&
            (currentMs == other.currentMs) && (isStarted == other.isStarted) &&
            (isFinished == other.isFinished) && (startTime == other.startTime) &&
            (leftTime == other.leftTime)
}

fun getChanges(other: Stopwatch): MutableList<Any> {
    val result = mutableListOf<Any>()
    if (periodMs != other.periodMs) {
        result.add(CHANGED_PERIOD)
    }
    if (currentMs != other.currentMs) {
        result.add(CHANGED_CURRENT_TIME)
    }
    if (isStarted != other.isStarted) {
        result.add(CHANGED_IS_STARTED)
    }
    if (isFinished != other.isFinished) {
        result.add(CHANGED_IS_FINISHED)
    }
    if (startTime != other.startTime) {
        result.add(CHANGED_START_TIME)
    }
    if (leftTime != other.leftTime) {
        result.add(CHANGED_LEFT_TIME)
    }
    return result
}

override fun hashCode(): Int {
    var result = id
    result = 31 * result + periodMs.hashCode()
    result = 31 * result + currentMs.hashCode()
    result = 31 * result + isStarted.hashCode()
    result = 31 * result + isFinished.hashCode()
    result = 31 * result + startTime.hashCode()
    result = 31 * result + leftTime.hashCode()
    return result
}


companion object {
    const val CHANGED_PERIOD = "PERIOD"
    const val CHANGED_CURRENT_TIME = "CURRENT_TIME"
    const val CHANGED_IS_STARTED = "IS_STARTED"
    const val CHANGED_IS_FINISHED = "IS_FINISHED"
    const val CHANGED_START_TIME = "START_TIME"
    const val CHANGED_LEFT_TIME = "LEFT_TIME"
    const val CHANGED_ALL = "ALL"
}
}



