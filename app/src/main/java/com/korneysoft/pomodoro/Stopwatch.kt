package com.korneysoft.pomodoro

import java.util.*

data class Stopwatch(
    val id: Int,
    var periodMs: Long,
    var currentMs: Long,
    var isStarted: Boolean,
    var isFinished: Boolean,
    var startTime: Date = Date(),
    var restMs: Long = 0 // rest from last start
) {
    init {
        restMs = currentMs
    }
}



