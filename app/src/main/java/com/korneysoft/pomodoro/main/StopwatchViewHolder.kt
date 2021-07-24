package com.korneysoft.pomodoro.main

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import com.korneysoft.pomodoro.R
import com.korneysoft.pomodoro.datamodel.Stopwatch
import com.korneysoft.pomodoro.databinding.StopwatchItemBinding
import com.korneysoft.pomodoro.interfaces.StopwatchPainter
import com.korneysoft.pomodoro.interfaces.StopwatchListener
import com.korneysoft.pomodoro.utils.*


class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val painter: StopwatchPainter,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.customViewPomodoroUnit.setPeriod(stopwatch.periodMs)
        binding.customViewPomodoroUnit.setCurrent(stopwatch.currentMs)
        binding.customViewPomodoroUnit.setFinished(stopwatch.isFinished)

        binding.textPeriod.text=stopwatch.periodMs.displayTime()
        binding.textPeriod.isVisible=stopwatch.isFinished

        initButtonsListeners(stopwatch)

        setBackgroundColor(stopwatch)
        setTextStartStopButton(stopwatch)

        if (stopwatch.isStarted) {
            runningTimer(stopwatch)
        } else {
            stoppedTimer()
        }

    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {

        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch)
            } else {
                listener.start(stopwatch)
            }
        }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch) }
    }


    private fun runningTimer(stopwatch: Stopwatch) {
        if (stopwatch.isFinished) {
            listener.stop(stopwatch)
        }
          setStateBlinkingIndicator(BLINKING_START)
    }

    private fun stoppedTimer() {
        setStateBlinkingIndicator(BLINKING_STOP)
    }

    private fun setStateBlinkingIndicator(isRunning: Boolean) {
        binding.blinkingIndicator.isInvisible = !isRunning
        if (isRunning) {
            (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        } else {
            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        }
    }

    private fun setTextStartStopButton(stopwatch: Stopwatch) {
        if (stopwatch.isStarted) {
            binding.startPauseButton.text = resources.getString(R.string.button_text_stop)
            return
        }

        binding.startPauseButton.text = if (stopwatch.isFinished) {
            resources.getString(R.string.button_text_restart)
        } else {
            resources.getString(R.string.button_text_start)
        }
    }


    private fun setBackgroundColor(stopwatch: Stopwatch) {
        binding.stopwatchUnit.setBackgroundColor(painter.getBackgroundColor(stopwatch))
    }

    private companion object {
        const val BLINKING_STOP = false
        const val BLINKING_START = true
    }

}