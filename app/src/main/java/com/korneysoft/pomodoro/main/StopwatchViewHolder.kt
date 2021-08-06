package com.korneysoft.pomodoro.main

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.korneysoft.pomodoro.R
import com.korneysoft.pomodoro.databinding.StopwatchItemBinding
import com.korneysoft.pomodoro.datamodel.Stopwatch
import com.korneysoft.pomodoro.interfaces.StopwatchListener
import com.korneysoft.pomodoro.interfaces.StopwatchPainter
import com.korneysoft.pomodoro.utils.displayTime


class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val painter: StopwatchPainter,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var currentStopwatch: Stopwatch? = null

    fun bind(stopwatch: Stopwatch, payload: Any) {

        initStopwatchListeners(stopwatch)

        initButtonsListeners(stopwatch)


        if (payload !is ArrayList<*>) {
            return
        }

        for (payloadOne in payload) {
            bindOne(stopwatch, payloadOne)
        }
    }

    private fun bindOne(stopwatch: Stopwatch, payloadOne: Any) {
        if (payloadOne == Stopwatch.CHANGED_PERIOD || payloadOne == Stopwatch.CHANGED_ALL) {
            binding.customViewPomodoroUnit.setPeriod(stopwatch.periodMs)
            binding.textPeriod.text =
                stopwatch.periodMs.displayTime(resources.getString(R.string.zerro_text_stopwatch))
        }

        if (payloadOne == Stopwatch.CHANGED_CURRENT_TIME || payloadOne == Stopwatch.CHANGED_ALL) {
            binding.customViewPomodoroUnit.setCurrent(stopwatch.currentMs)
            binding.stopwatchTimer.text =
                stopwatch.currentMs.displayTime(resources.getString(R.string.zerro_text_stopwatch))

        }

        if (payloadOne == Stopwatch.CHANGED_IS_FINISHED || payloadOne == Stopwatch.CHANGED_ALL) {
            binding.customViewPomodoroUnit.setFinished(stopwatch.isFinished)
            binding.textPeriod.isVisible = stopwatch.isFinished
            setBackgroundColor(stopwatch)
            if (stopwatch.isStarted && stopwatch.isFinished) {
                listener.stop(stopwatch)
                setStateBlinkingIndicator(BLINKING_STOP)
            }
        }

        if (payloadOne == Stopwatch.CHANGED_IS_STARTED || payloadOne == Stopwatch.CHANGED_ALL) {

            if (stopwatch.isStarted) {
                setStateBlinkingIndicator(BLINKING_START)
            } else {
                setStateBlinkingIndicator(BLINKING_STOP)
            }
            setTextStartStopButton(stopwatch)
        }

    }


    private fun initButtonsListeners(stopwatch: Stopwatch) {

        binding.startPauseButton.setOnClickListener {
            stopwatch.isStarted = !stopwatch.isStarted
        }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch) }
    }

    private fun initStopwatchListeners(stopwatch: Stopwatch) {

        if (currentStopwatch != stopwatch) {
            currentStopwatch?.onStarted = null
            currentStopwatch?.onChangeCurrentTime = null
            currentStopwatch?.onStopped = null
            currentStopwatch?.onFinished = null
            currentStopwatch = stopwatch
        }

        if (stopwatch.isStarted) {
            stopwatch.onChangeCurrentTime = {
                if (stopwatch.isStarted) {
                    bindOne(stopwatch, Stopwatch.CHANGED_CURRENT_TIME)
                }
            }

            stopwatch.onFinished = {
                bindOne(stopwatch, Stopwatch.CHANGED_IS_FINISHED)
            }

            stopwatch.onStopped = {
                listener.stop(stopwatch)
                bindOne(stopwatch, Stopwatch.CHANGED_IS_STARTED)
                if (!stopwatch.isFinished) {
                    initStopwatchListeners(stopwatch) // reinitialised Listener for stopped stopwatch
                }
            }

        } else {
            stopwatch.onStarted = {
                listener.start(stopwatch)
                bindOne(stopwatch, Stopwatch.CHANGED_IS_FINISHED)
                bindOne(stopwatch, Stopwatch.CHANGED_IS_STARTED)
                initStopwatchListeners(stopwatch) // reinitialised Listener for started stopwatch
            }
        }
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