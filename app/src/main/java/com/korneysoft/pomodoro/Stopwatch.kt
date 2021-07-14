package com.korneysoft.pomodoro

import java.util.*

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean,

)