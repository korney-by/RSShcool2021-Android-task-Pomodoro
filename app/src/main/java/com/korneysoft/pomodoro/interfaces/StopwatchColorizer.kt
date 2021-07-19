package com.korneysoft.pomodoro.interfaces

import androidx.annotation.ColorInt

interface StopwatchColorizer {

    @ColorInt
    fun getBackgroundColor(id: Int): Int

}