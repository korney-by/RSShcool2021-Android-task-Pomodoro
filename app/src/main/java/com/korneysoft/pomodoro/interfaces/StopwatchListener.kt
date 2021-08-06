package com.korneysoft.pomodoro.interfaces

import com.korneysoft.pomodoro.datamodel.Stopwatch


interface StopwatchListener {

    fun start(stopwatch:Stopwatch)

    fun stop(stopwatch: Stopwatch)

    fun delete(stopwatch:Stopwatch)

    fun finish(stopwatch:Stopwatch)

}