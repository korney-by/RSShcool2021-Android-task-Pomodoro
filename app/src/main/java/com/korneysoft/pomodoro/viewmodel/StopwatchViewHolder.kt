package com.korneysoft.pomodoro.viewmodel

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.korneysoft.pomodoro.R
import com.korneysoft.pomodoro.datamodel.Stopwatch
import com.korneysoft.pomodoro.databinding.StopwatchItemBinding
import com.korneysoft.pomodoro.interfaces.StopwatchPainter
import com.korneysoft.pomodoro.interfaces.StopwatchListener


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
       // if (stopwatch.isStarted) {binding.startPauseButton.text=

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
                listener.stop(stopwatch)
            } else {
                listener.start(stopwatch)
            }
        }

        //binding.restartButton.setOnClickListener { listener.reset(stopwatch.id) }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch) }
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = resources.getString(R.string.button_text_start)

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()

        setBackgroundColor(stopwatch)
    }


    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = resources.getString(R.string.button_text_stop)

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()

        if (stopwatch.isFinished) {
            listener.stop(stopwatch)
        }
        setBackgroundColor(stopwatch)

    }


    private fun setBackgroundColor(stopwatch: Stopwatch) {
        binding.stopwatchUnit.setBackgroundColor(painter.getBackgroundColor(stopwatch.id))
    }


}