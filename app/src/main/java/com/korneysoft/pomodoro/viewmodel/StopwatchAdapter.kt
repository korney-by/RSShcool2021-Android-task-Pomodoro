package com.korneysoft.pomodoro.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.korneysoft.pomodoro.databinding.StopwatchItemBinding
import com.korneysoft.pomodoro.datamodel.Stopwatch
import com.korneysoft.pomodoro.datamodel.Stopwatches
import com.korneysoft.pomodoro.interfaces.StopwatchListener
import com.korneysoft.pomodoro.interfaces.StopwatchPainter

class StopwatchAdapter(
    private val listener: StopwatchListener,
    private val painter: StopwatchPainter
) : ListAdapter<Stopwatch, StopwatchViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopwatchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = StopwatchItemBinding.inflate(layoutInflater, parent, false)
        return StopwatchViewHolder(binding, listener,painter, binding.root.context.resources)
    }

    override fun onBindViewHolder(holder: StopwatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    private companion object {

        private val itemComparator = object : DiffUtil.ItemCallback<Stopwatch>() {

            override fun areItemsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.equalsContent(newItem)

            }

            override fun getChangePayload(oldItem: Stopwatch, newItem: Stopwatch) = Any()
        }
    }
}