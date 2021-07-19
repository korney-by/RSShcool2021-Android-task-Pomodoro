package com.korneysoft.pomodoro

import android.os.Bundle
import android.os.CountDownTimer
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stopwatch.StopwatchAdapter
import com.korneysoft.pomodoro.databinding.ActivityMainBinding
import com.korneysoft.pomodoro.interfaces.StopwatchColorizer
import com.korneysoft.pomodoro.interfaces.StopwatchListener


class MainActivity : AppCompatActivity(), StopwatchListener, StopwatchColorizer {
    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this,this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var runningStopwatch = STOPWATCHES_NO_RUNNING
    private var timer: CountDownTimer? = null
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }


        binding.addNewStopwatchButton.setOnClickListener {
            binding.editTextNumber.text.toString().toLongOrNull()?.apply {
                stopwatches.add(
                    Stopwatch(
                        nextId++,
                        this * 60000,
                        this * 60000 - 50000, //TODO убрать
                        false,
                        false
                    )


                )
                stopwatchAdapter.submitList(stopwatches.toList())
                //binding.editTextNumber.text.clear()
            }
        }
    }

    @ColorInt
    override fun getBackgroundColor(id: Int): Int {
        val typedValue = TypedValue()
        if (stopwatches[id].isFinished) {
            //theme.resolveAttribute(R.attr.colorSecondaryVariant, typedValue, true)
            theme.resolveAttribute(R.attr.colorSecondaryVariant, typedValue, true)
        }else {
            theme.resolveAttribute(R.attr.backgroundColor, typedValue, true)
        }
        return typedValue.data  // **just add this line to your code!!**
    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

//    override fun reset(id: Int) {
//        changeStopwatch(id, 0L, false)
//    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }


    private fun setRunningStopwatch(id: Int) {
        runningStopwatch = id
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(
                    Stopwatch(
                        it.id,
                        it.periodMs,
                        currentMs ?: it.currentMs,
                        isStarted,
                        it.isFinished
                    )
                )
            } else {
                newTimers.add(it)
            }

            if (isStarted) {
                setRunningStopwatch(id)
            } else {
                if (it.id == id) {
                    setRunningStopwatch(STOPWATCHES_NO_RUNNING)
                }
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)


    }

    private companion object {
        private const val STOPWATCHES_NO_RUNNING = -1
    }
}