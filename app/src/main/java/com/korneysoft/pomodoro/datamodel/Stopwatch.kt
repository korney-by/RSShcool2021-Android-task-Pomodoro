package com.korneysoft.pomodoro.datamodel

data class Stopwatch(
    val id: Int,
    var periodMs: Long,
    var currentMs: Long,
    var isStarted: Boolean,
    var isFinished: Boolean,
    var startTime: Long = 0,
    var leftTime: Long = 0 // left time to the finish
) {

    override fun equals(other: Any?): Boolean {
        //return super.equals(other)
        return if (other is Stopwatch) {
            (id == other.id) && equalsContent(other)
        } else {
            false
        }
    }

    fun equalsContent(other: Stopwatch): Boolean {
        return (periodMs == other.periodMs) &&
                (currentMs == other.currentMs) && (isStarted == other.isStarted) &&
                (isFinished == other.isFinished) && (startTime == other.startTime) &&
                (leftTime == other.leftTime)
    }
}





