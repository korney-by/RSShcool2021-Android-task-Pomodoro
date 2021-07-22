package com.korneysoft.pomodoro

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.korneysoft.pomodoro.databinding.ActivityMainBinding
import com.korneysoft.pomodoro.datamodel.Stopwatch
import com.korneysoft.pomodoro.datamodel.getStopwatch
import com.korneysoft.pomodoro.datamodel.getStopwatchIndex
import com.korneysoft.pomodoro.interfaces.StopwatchListener
import com.korneysoft.pomodoro.interfaces.StopwatchPainter
import com.korneysoft.pomodoro.services.*
import com.korneysoft.pomodoro.viewmodel.getStopwatchCurrentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), StopwatchListener, StopwatchPainter, LifecycleObserver {
    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this, this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var runningStopwatchID = STOPWATCHES_NO_RUNNING
    private val isStopwatchRunning: Boolean
        get() {
            return runningStopwatchID != STOPWATCHES_NO_RUNNING
        }

    //private var mainTimer: CountDownTimer? = null
    private var nextId = 0

    //private var startTime = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //startTime = System.currentTimeMillis()

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

//        binding.recycler.setHasFixedSize(true)
//        binding.recycler.isNestedScrollingEnabled=false

        // mainTimer = getMainTimer()
        startTimer()

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
                binding.editTextNumber.selectAll()
            }
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (isStopwatchRunning) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, getStartTimeCurrentStopwatch())
            startIntent.putExtra(STARTED_TIMER_LEFT_MS, getLeftTimeCurrentStopwatch())
            startService(startIntent)
        }
    }

    private fun getLeftTimeCurrentStopwatch(): Long {
        if (isStopwatchRunning) {
            return stopwatches.getStopwatch(runningStopwatchID)?.leftTime ?: 0
        } else {
            return 0
        }
    }

    private fun getStartTimeCurrentStopwatch(): Long {
        if (isStopwatchRunning) {
            return stopwatches.getStopwatch(runningStopwatchID)?.startTime ?: 0
        } else {
            return 0
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    private fun startTimer() {
//        mainTimer?.cancel()
//        mainTimer = getMainTimer()
//        mainTimer?.start()

        lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                onTickTimer()
                delay(INTERVAL)
            }
        }
    }

    private fun stopTimer() {
        //mainTimer?.cancel()
    }

    private fun getMainTimer(): CountDownTimer {
        return object : CountDownTimer(PERIOD, INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                onTickTimer();
            }

            override fun onFinish() {
                //    binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }
        }
    }

    private fun onTickTimer() {

        if (runningStopwatchID == STOPWATCHES_NO_RUNNING) return

        val index = stopwatches.getStopwatchIndex(runningStopwatchID)
        val stopwatch = stopwatches.getStopwatch(runningStopwatchID)?.copy()

        stopwatch?.let {
            if (it.currentMs <= 0) {
                stopFinished(it)
            } else {
                if (it.isStarted) {
                    it.currentMs = getStopwatchCurrentTime(it.startTime,it.leftTime)
                }
            }
            showChanges(index, it)
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
                startTime = System.currentTimeMillis()
                leftTime = currentMs
                runningStopwatchID = id
                //startMainTimer()
            } else {
                if (runningStopwatchID == id) {
                    runningStopwatchID = STOPWATCHES_NO_RUNNING
                    //stopMainTimer()
                }
                if (isFinished) {
                    currentMs = periodMs
                    leftTime = periodMs
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
        private const val INTERVAL = 100L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day

    }
}
