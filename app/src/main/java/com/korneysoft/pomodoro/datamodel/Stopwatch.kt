package com.korneysoft.pomodoro.datamodel

data class Stopwatch(
    val id: Int,
    var periodMs: Long,
    var currentMs: Long,
    var isStarted: Boolean,
    var isFinished: Boolean,
    var startTime: Long = 0,
    var leftTime: Long = 0 // rest from last start
)

fun MutableList<Stopwatch>.getStopwatch(id: Int): Stopwatch?  {
    return this.find { it.id == id }
}

fun MutableList<Stopwatch>.getStopwatchIndex(id: Int): Int {
    return this.indexOf(getStopwatch(id))
}

fun MutableList<Stopwatch>.getStopwatchIndex(stopwatch: Stopwatch): Int {
    return this.indexOf(stopwatch)
}


