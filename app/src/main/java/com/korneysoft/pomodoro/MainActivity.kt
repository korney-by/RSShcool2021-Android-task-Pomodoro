package com.korneysoft.pomodoro

import android.os.Bundle
import android.os.CountDownTimer
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.korneysoft.pomodoro.databinding.ActivityMainBinding
import com.korneysoft.pomodoro.datamodel.Stopwatch
import com.korneysoft.pomodoro.datamodel.getStopwatch
import com.korneysoft.pomodoro.datamodel.getStopwatchIndex
import com.korneysoft.pomodoro.interfaces.StopwatchPainter
import com.korneysoft.pomodoro.interfaces.StopwatchListener
import java.util.*


class MainActivity : AppCompatActivity(), StopwatchListener, StopwatchPainter {
    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this, this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var runningStopwatchID = STOPWATCHES_NO_RUNNING
    private var mainTimer: CountDownTimer? = null
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

//        binding.recycler.setHasFixedSize(true)
//        binding.recycler.isNestedScrollingEnabled=false

        mainTimer = getMainTimer()
        startMainTimer()

        binding.addNewStopwatchButton.setOnClickListener {
            binding.editTextNumber.text.toString().toLongOrNull()?.apply {
                stopwatches.add(
                    Stopwatch(
                        nextId++,
                        this * 60000,
                        this * 60000, // - 50000, //TODO убрать
                        isStarted = false,
                        isFinished = false
                    )
                )
                stopwatchAdapter.submitList(stopwatches.toList())
                binding.editTextNumber.text.clear()
            }
        }
    }

    private fun startMainTimer() {
        mainTimer?.cancel()
        mainTimer = getMainTimer()
        mainTimer?.start()
    }

    private fun stopMainTimer() {
        mainTimer?.cancel()
    }

    private fun getMainTimer(): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {

            override fun onTick(millisUntilFinished: Long) {
                if (runningStopwatchID == STOPWATCHES_NO_RUNNING) return

                val index = stopwatches.getStopwatchIndex(runningStopwatchID)
                val stopwatch = stopwatches.getStopwatch(runningStopwatchID)?.copy()


                stopwatch?.let {
                    if (it.currentMs <= 0) {
                        stopFinished(it)
                    } else {
                        if (it.isStarted) {
                            it.currentMs = it.restMs - (Date().time - it.startTime)
                        }
                    }
                    showChanges(index, it)
                }

            }

            override fun onFinish() {
                //    binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }
        }
    }

    fun showChanges(index: Int, stopwatch: Stopwatch) {
        if (index >= 0) {
            stopwatches[index] = stopwatch
            stopwatchAdapter.submitList(stopwatches.toList())
        }

        //      stopwatchAdapter.notifyItemChanged(stopwatches.getStopwatchIndex(it))
    }

    override fun getBackgroundColor(id: Int): Int {
        val typedValue = TypedValue()
        stopwatches.getStopwatch(id)?.let {
            if (it.isFinished) {
                theme.resolveAttribute(R.attr.colorSecondaryVariant, typedValue, true)
            } else {
                theme.resolveAttribute(R.attr.backgroundColor, typedValue, true)
            }
        }
        return typedValue.data  // **just add this line to your code!!**
    }

    fun stopFinished(stopwatch: Stopwatch) {
        stopwatch.isFinished = true
    }

    override fun start(stopwatch: Stopwatch) {
        stopwatches.getStopwatch(runningStopwatchID)?.let {
            changeStopwatchState(it, false)
        }
        changeStopwatchState(stopwatch, true) // изменение состояния таймера ?
    }

    override fun stop(stopwatch: Stopwatch) {
        changeStopwatchState(stopwatch, false)
    }

    private fun changeStopwatchState(_stopwatch: Stopwatch, isStart: Boolean) {
        val stopwatch = _stopwatch.copy()

        with(stopwatch) {
            isStarted = isStart
            if (isStart) {
                isFinished = false
                startTime = Date().time
                restMs = currentMs
                runningStopwatchID = id
                startMainTimer()
            } else {
                if (runningStopwatchID == id) {
                    runningStopwatchID = STOPWATCHES_NO_RUNNING
                    stopMainTimer()
                }
                if (isFinished) {
                    currentMs = periodMs
                    restMs = periodMs
                }


            }
        }
        val index = stopwatches.getStopwatchIndex(_stopwatch)
        showChanges(index, stopwatch)
    }


//    override fun reset(id: Int) {
//        changeStopwatch(id, 0L, false)
//    }

    override fun delete(stopwatch: Stopwatch) {
        stopwatches.remove(stopwatch)
        stopwatchAdapter.submitList(stopwatches.toList())
    }


    private companion object {
        private const val STOPWATCHES_NO_RUNNING = -1
        private const val UNIT_TEN_MS = 100L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day

    }
}
