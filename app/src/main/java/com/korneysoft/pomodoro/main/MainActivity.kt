package com.korneysoft.pomodoro.main

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.korneysoft.pomodoro.R
import com.korneysoft.pomodoro.databinding.ActivityMainBinding
import com.korneysoft.pomodoro.datamodel.*
import com.korneysoft.pomodoro.interfaces.*
import com.korneysoft.pomodoro.services.*
import com.korneysoft.pomodoro.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


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
                addNewStopwatch(this)
            }
        }

        //AddTimersForTest(500_000,30000)

    }


//    private fun AddTimersForTest(count:Int,timeMS:Long){
//        for (i in 0 until count){
//            stopwatches.add(
//                Stopwatch(
//                    Stopwatches.getNextID(),
//                     timeMS,
//                     timeMS,
//                     false,
//                     false
//                )
//            )
//        }
//        stopwatchAdapter.submitList(stopwatches.toList())
//    }

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

    private fun addNewStopwatch(timeMs:Long) {
        stopwatches.add(
            Stopwatch(
                Stopwatches.getNextID(),
                timeMs * 60000,
                timeMs * 60000,//-50000,
                _isStarted = false,
                _isFinished = false
            )
        )
        stopwatchAdapter.submitList(stopwatches.toList()) {
            showStopwatch(stopwatchAdapter.itemCount - 1)
        }

        binding.editTextNumber.selectAll()
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
        Stopwatches.getRunningStopwatch()?.doTickTimer()
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

    private fun infoAboutFinished(stopwatch: Stopwatch) {
        if (stopwatch.isFinished) {
            //playFinishedSound(applicationContext)
            playSound(applicationContext,R.raw.alldone)
            showToast(applicationContext, resources.getString(R.string.message_timer_expired))
        }
    }

    override fun start(stopwatch: Stopwatch) {
        Stopwatches.getRunningStopwatch()?.stop()
        Stopwatches.setRunningStopwatchID(stopwatch.id)
        playSound(applicationContext,R.raw.start)
        stopwatch.onAfterFinished = {
            finish(stopwatch)
        }
    }

    override fun stop(stopwatch: Stopwatch) {
        Stopwatches.setRunningStopwatchIDToStop(stopwatch.id)
        if (!stopwatch.isFinished){
            stopwatch.onAfterFinished=null
        }
    }

    override fun finish(stopwatch: Stopwatch) {
        Stopwatches.setRunningStopwatchIDToStop(stopwatch.id)
        stopwatch.onAfterFinished=null
        infoAboutFinished(stopwatch)
    }

    override fun delete(stopwatch: Stopwatch) {
        Stopwatches.deleteStopwatch(stopwatch)
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private var backPressed: Long = 0
    override fun onBackPressed() {
        if (backPressed + 2000 > System.currentTimeMillis()) {
            exitProcess(-1)
        } else {
            Toast.makeText(
                baseContext,
                resources.getString(R.string.double_pressed_exit),
                Toast.LENGTH_SHORT
            ).show()
        }
        backPressed = System.currentTimeMillis()
    }

    private companion object {
        private const val INTERVAL = 10L
    }

}
