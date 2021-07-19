package com.example.stopwatch

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.korneysoft.pomodoro.R
import com.korneysoft.pomodoro.Stopwatch
import com.korneysoft.pomodoro.databinding.StopwatchItemBinding
import com.korneysoft.pomodoro.interfaces.StopwatchColorizer
import com.korneysoft.pomodoro.interfaces.StopwatchListener
import java.util.*


class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val colorizer: StopwatchColorizer,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.customViewPomodoroUnit.setPeriod(stopwatch.periodMs)
        binding.customViewPomodoroUnit.setCurrent(stopwatch.currentMs)

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {

        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        //binding.restartButton.setOnClickListener { listener.reset(stopwatch.id) }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = resources.getString(R.string.button_text_stop)
        stopwatch.isFinished = false
        stopwatch.startTime = Date()

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()

        setBackgroundColor(stopwatch.id)
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = resources.getString(R.string.button_text_start)

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()

       // stopwatch.restMs = stopwatch.currentMs

        if (stopwatch.isFinished) {
            stopwatch.isStarted = false
            stopwatch.currentMs = stopwatch.periodMs
            stopwatch.restMs = stopwatch.periodMs

        }
        setBackgroundColor(stopwatch.id)
    }

    fun setBackgroundColor(stopwatchID: Int) {
        binding.stopwatchUnit.setBackgroundColor(colorizer.getBackgroundColor(stopwatchID))
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS

            override fun onTick(millisUntilFinished: Long) {
                if (stopwatch.isFinished) return

                stopwatch.isFinished = (stopwatch.currentMs <= 0)

                if (stopwatch.isFinished) {
                    stopTimer(stopwatch)
                } else {
                    //stopwatch.currentMs -= interval
                    stopwatch.currentMs =
                        stopwatch.restMs - (Date().time - stopwatch.startTime.time)
                }

                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                binding.customViewPomodoroUnit.setPeriod(stopwatch.periodMs)
                binding.customViewPomodoroUnit.setCurrent(stopwatch.currentMs)
            }

            override fun onFinish() {
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }
        }
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return resources.getString(R.string.null_text_stopwatch)
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        //val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
        //return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val UNIT_TEN_MS = 100L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}