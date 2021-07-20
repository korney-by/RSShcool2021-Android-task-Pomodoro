package com.korneysoft.pomodoro.interfaces

import com.korneysoft.pomodoro.datamodel.Stopwatch


interface StopwatchListener {

    fun start(stopwatch:Stopwatch)

    fun stop(stopwatch: Stopwatch)

  //  fun reset(id: Int)

    fun delete(stopwatch:Stopwatch)
}