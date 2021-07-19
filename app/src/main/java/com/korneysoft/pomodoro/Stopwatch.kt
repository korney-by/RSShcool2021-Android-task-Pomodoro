package com.korneysoft.pomodoro

import java.util.*

data class Stopwatch(
    val id: Int,
    var periodMs: Long,
    var currentMs: Long,
    var isStarted: Boolean,
    var isFinished: Boolean

)