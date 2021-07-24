package com.korneysoft.pomodoro.interfaces

import com.korneysoft.pomodoro.datamodel.Stopwatch

interface StopwatchPainter {
    fun getBackgroundColor(stopwatch: Stopwatch): Int
}