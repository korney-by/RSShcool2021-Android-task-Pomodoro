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
            equalsID(other) && equalsContent(other)
        } else {
            false
        }
    }


    fun equalsID(other: Stopwatch): Boolean {
        return (id == other.id)
    }

    fun equalsContent(other: Stopwatch): Boolean {
        return (periodMs == other.periodMs) &&
                (currentMs == other.currentMs) && (isStarted == other.isStarted) &&
                (isFinished == other.isFinished) && (startTime == other.startTime) &&
                (leftTime == other.leftTime)
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + periodMs.hashCode()
        result = 31 * result + currentMs.hashCode()
        result = 31 * result + isStarted.hashCode()
        result = 31 * result + isFinished.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + leftTime.hashCode()
        return result
    }
}





