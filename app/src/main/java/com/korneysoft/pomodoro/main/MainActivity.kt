package com.korneysoft.pomodoro.main

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.View

import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.korneysoft.pomodoro.R
import com.korneysoft.pomodoro.databinding.ActivityMainBinding
import com.korneysoft.pomodoro.datamodel.Stopwatch
import com.korneysoft.pomodoro.datamodel.Stopwatches
import com.korneysoft.pomodoro.datamodel.getStopwatchIndex
import com.korneysoft.pomodoro.interfaces.StopwatchListener
import com.korneysoft.pomodoro.interfaces.StopwatchPainter
import com.korneysoft.pomodoro.services.*
import com.korneysoft.pomodoro.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), StopwatchListener, StopwatchPainter, LifecycleObserver {
    private val stopwatches = Stopwatches.getStopwatchesList()

    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this, this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        startTimer()
        stopwatchAdapter.submitList(stopwatches.toList())

        binding.addNewStopwatchButton.setOnClickListener {
            binding.editTextNumber.text.toString().toLongOrNull()?.apply {
                stopwatches.add(
                    Stopwatch(
                        Stopwatches.getNextID(),
                        this * 60000,
                        this * 60000,
                        isStarted = false,
                        isFinished = false
                    )
                )
                stopwatchAdapter.submitList(stopwatches.toList()) {
                    showStopwatch(stopwatchAdapter.itemCount - 1)
                }

                binding.editTextNumber.selectAll()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (Stopwatches.isAnyStopwatchRunning) {
            binding.editTextNumber.clearFocus() // to show all List when back to App
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, Stopwatches.getStartTimeCurrentStopwatch())
            startIntent.putExtra(STARTED_TIMER_LEFT_MS, Stopwatches.getLeftTimeCurrentStopwatch())
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
        showStopwatch(Stopwatches.getRunningStopwatchIndex())
    }

    private fun showStopwatch(position: Int) {
        if (position >= 0) {
            binding.recycler.smoothScrollToPosition(position)
        }
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var view: View? = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // check orientation
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideKeyboard(this)
        }
    }

    private fun startTimer() {
        lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                onTickTimer()
                delay(INTERVAL)
            }
        }
    }

    private fun onTickTimer() {
        if (!Stopwatches.isAnyStopwatchRunning) return

        val stopwatchRunning = Stopwatches.getRunningStopwatch()
        val stopwatchNew = stopwatchRunning?.copy()

        stopwatchNew?.let {
            if (it.currentMs <= 0) {
                stopFinished(it)
            } else {
                if (it.isStarted) {
                    it.currentMs = getStopwatchCurrentTime(it.startTime, it.leftTime)
                }
            }
            showStopwatchChanges(stopwatchRunning, it)
        }
    }

    private fun showStopwatchChanges(stopwatchOld: Stopwatch?, stopwatchNew: Stopwatch) {
        if (stopwatchOld != null) {
            val index = stopwatches.getStopwatchIndex(stopwatchOld)
            if (index >= 0) {
                stopwatches[index] = stopwatchNew
                stopwatchAdapter.submitList(stopwatches.toList()) {
                }
            }
        }
    }

    override fun getBackgroundColor(stopwatch: Stopwatch): Int {
        val typedValue = TypedValue()
        if (stopwatch.isFinished) {
            theme.resolveAttribute(R.attr.colorSecondaryVariant, typedValue, true)
        } else {
            theme.resolveAttribute(R.attr.backgroundColor, typedValue, true)
        }
        return typedValue.data
    }

    private fun stopFinished(stopwatch: Stopwatch) {
        if (!stopwatch.isFinished) {
            playFinishedSound(applicationContext)
            stopwatch.isFinished = true
        }
    }

    override fun start(stopwatch: Stopwatch) {
        Stopwatches.getRunningStopwatch()?.let {
            changeStopwatchState(it, false)
        }
        changeStopwatchState(stopwatch, true) // change state Timer
    }

    override fun stop(stopwatch: Stopwatch) {
        changeStopwatchState(stopwatch, false)
    }


    private fun changeStopwatchState(stopwatch: Stopwatch, isStart: Boolean) {
        val stopwatchNew = stopwatch.copy()

        with(stopwatchNew) {
            isStarted = isStart
            if (isStart) {
                if (isFinished) {
                    leftTime = periodMs
                    currentMs = periodMs
                } else {
                    leftTime = currentMs
                }
                isFinished = false
                startTime = getCurrentTime()
                Stopwatches.setRunningStopwatchID(id)
            } else {
                Stopwatches.setRunningStopwatchIDToStop(id)
                if (isFinished) {
                    currentMs = 0
                }
            }
        }
        showStopwatchChanges(stopwatch, stopwatchNew)
    }


//    override fun reset(id: Int) {
//        changeStopwatch(id, 0L, false)
//    }

    override fun delete(stopwatch: Stopwatch) {
        Stopwatches.deleteStopwatch(stopwatch)
        stopwatchAdapter.submitList(stopwatches.toList())
    }


    private companion object {
        private const val INTERVAL = 100L
        // private const val RUNNING_STOPWATCH_ID ="RUNNING_STOPWATCH_ID"
    }
}
