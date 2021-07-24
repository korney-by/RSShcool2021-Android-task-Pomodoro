package com.korneysoft.pomodoro.utils

import android.os.SystemClock

fun Long.displayTime(zeroTimeText:String): String {
    if (this <= 0L) {
        return zeroTimeText
    }

    val h = (this + 999) / 1000 / 3600
    val m = (this + 999) / 1000 % 3600 / 60
    val s = (this + 999) / 1000 % 60

    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
}

fun Long.displayTimeForService(zeroTimeText:String): String {
    if (this <= 0L) {
        return zeroTimeText
    }
    return this.displayTime(zeroTimeText)
}

private fun displaySlot(count: Long): String {
    return if (count / 10L > 0) {
        "$count"
    } else {
        "0$count"
    }
}

fun getCurrentTime(): Long {
    return SystemClock.elapsedRealtime()
}

fun getStopwatchCurrentTime(startTimeMs: Long, leftTimeMs: Long): Long {
    return leftTimeMs - (getCurrentTime() - startTimeMs)
}