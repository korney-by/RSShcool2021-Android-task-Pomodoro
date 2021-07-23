package com.korneysoft.pomodoro

//import android.os.CountDownTimer
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
import com.korneysoft.pomodoro.databinding.ActivityMainBinding
import com.korneysoft.pomodoro.datamodel.Stopwatch
import com.korneysoft.pomodoro.datamodel.Stopwatches
import com.korneysoft.pomodoro.datamodel.getStopwatch
import com.korneysoft.pomodoro.datamodel.getStopwatchIndex
import com.korneysoft.pomodoro.interfaces.StopwatchListener
import com.korneysoft.pomodoro.interfaces.StopwatchPainter
import com.korneysoft.pomodoro.services.*
import com.korneysoft.pomodoro.viewmodel.StopwatchAdapter
import com.korneysoft.pomodoro.viewmodel.getCurrentTime
import com.korneysoft.pomodoro.viewmodel.getStopwatchCurrentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), StopwatchListener, StopwatchPainter, LifecycleObserver {
    private val stopwatches=Stopwatches.getStopwatchesList()

    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this, this)


    //private var mainTimer: CountDownTimer? = null


//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putInt(RUNNING_STOPWATCH_ID, runningStopwatchID)
//
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        runningStopwatchID = savedInstanceState.getInt(RUNNING_STOPWATCH_ID)
//    }

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
        stopwatchAdapter.submitList(stopwatches.toList())

        hideKeyboard(this)

        binding.addNewStopwatchButton.setOnClickListener {
            binding.editTextNumber.text.toString().toLongOrNull()?.apply {
                stopwatches.add(
                    Stopwatch(
                        Stopwatches.getNextID(),
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
        if (Stopwatches.isStopwatchRunning) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS,Stopwatches.getStartTimeCurrentStopwatch())
            startIntent.putExtra(STARTED_TIMER_LEFT_MS, Stopwatches.getLeftTimeCurrentStopwatch())
            startService(startIntent)
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // check orientation
        if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            hideKeyboard(this)
        }
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

//    private fun stopTimer() {
//        mainTimer?.cancel()
//    }

//    private fun getMainTimer(): CountDownTimer {
//        return object : CountDownTimer(PERIOD, INTERVAL) {
//
//            override fun onTick(millisUntilFinished: Long) {
//                onTickTimer()
//            }
//
//            override fun onFinish() {
//                //    binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
//            }
//        }
//    }

    private fun onTickTimer() {
        if (!Stopwatches.isStopwatchRunning) return

        val index = Stopwatches.getRunningStopwatchIndex()
        val stopwatch = Stopwatches.getRunningStopwatch()?.copy()

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

    private fun showChanges(index: Int, stopwatch: Stopwatch) {
        if (index >= 0) {
            stopwatches[index] = stopwatch
            stopwatchAdapter.submitList(stopwatches.toList())
        }

        //stopwatchAdapter.notifyItemChanged(stopwatches.getStopwatchIndex(stopwatch))
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

    private fun stopFinished(stopwatch: Stopwatch) {
        stopwatch.isFinished = true
    }

    override fun start(stopwatch: Stopwatch) {
        Stopwatches.getRunningStopwatch()?.let {
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
                startTime = getCurrentTime()
                leftTime = currentMs
                Stopwatches.setRunningStopwatchID(id)
            } else {
                Stopwatches.setRunningStopwatchID(id)
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
        Stopwatches.deleteStopwatch(stopwatch)
        stopwatchAdapter.submitList(stopwatches.toList())
    }


    private companion object {
        private const val INTERVAL = 100L
       // private const val RUNNING_STOPWATCH_ID ="RUNNING_STOPWATCH_ID"
    }
}
