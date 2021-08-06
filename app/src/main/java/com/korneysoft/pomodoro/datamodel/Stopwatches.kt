package com.korneysoft.pomodoro.datamodel

object Stopwatches {
    private const val STOPWATCHES_NO_RUNNING = -1

    private val stopwatches = mutableListOf<Stopwatch>()

    private var nextId = 0

    private var runningStopwatchID = STOPWATCHES_NO_RUNNING
    val isAnyStopwatchRunning: Boolean
        get() {
            return runningStopwatchID != STOPWATCHES_NO_RUNNING
        }


    fun getStopwatchesList(): MutableList<Stopwatch> {
        return stopwatches
    }

    fun getNextID(): Int {
        return nextId++
    }

    fun setRunningStopwatchID(id: Int) {
        runningStopwatchID = id
    }

    private fun setRunningStopwatchIDToStop() {
        runningStopwatchID = STOPWATCHES_NO_RUNNING
    }

    fun getRunningStopwatchID(): Int {
        return runningStopwatchID
    }

    fun setRunningStopwatchIDToStop(id: Int) {
        if (runningStopwatchID == id) {
            runningStopwatchID = STOPWATCHES_NO_RUNNING
        }
    }

    fun getLeftTimeCurrentStopwatch(): Long {
        return if (isAnyStopwatchRunning) {
            stopwatches.getStopwatch(runningStopwatchID)?.leftTime ?: 0
        } else {
            0
        }
    }

    fun getStartTimeCurrentStopwatch(): Long {
        return if (isAnyStopwatchRunning) {
            stopwatches.getStopwatch(runningStopwatchID)?.startTime ?: 0
        } else {
            0
        }
    }

    fun getRunningStopwatchIndex(): Int {
        return stopwatches.getStopwatchIndex(runningStopwatchID)
    }

    fun getRunningStopwatch(): Stopwatch? {
        return if (isAnyStopwatchRunning) {
            return stopwatches.getStopwatch(runningStopwatchID)
        } else {
            null
        }
    }

    fun deleteStopwatch(stopwatch: Stopwatch) {
        setRunningStopwatchIDToStop(stopwatch.id)
        stopwatches.remove(stopwatch)
    }
}

fun MutableList<Stopwatch>.getStopwatch(id: Int): Stopwatch? {
    return this.find { it.id == id }
}

fun MutableList<Stopwatch>.getStopwatchIndex(id: Int): Int {
    return this.indexOf(getStopwatch(id))
}

fun MutableList<Stopwatch>.getStopwatchIndex(stopwatch: Stopwatch): Int {
    return this.indexOf(stopwatch)
}
