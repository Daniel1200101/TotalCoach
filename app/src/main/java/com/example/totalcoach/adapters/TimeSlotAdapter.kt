package com.example.totalcoach.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.totalcoach.R

data class TimeSlotItem(
    var id: String = "",
    val date: String = "",
    val timeSlot: String = "",
    var title: String = "",
    var description: String = ""
) {
    // Firebase needs a no-argument constructor
    constructor() : this("", "", "", "", "")
}


class TimeSlotAdapter(private val timeSlots: MutableList<TimeSlotItem>) :
    RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private var onItemClickListener: ((TimeSlotItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (TimeSlotItem) -> Unit) {
        onItemClickListener = listener
    }

    fun updateTimeSlots(newTimeSlots: MutableList<TimeSlotItem>) {
        timeSlots.clear()
        timeSlots.addAll(newTimeSlots)
        notifyDataSetChanged()
    }

    fun addTimeSlot(newSlot: TimeSlotItem) {
        timeSlots.add(newSlot)
        notifyItemInserted(timeSlots.size - 1)
    }

    fun setTimeSlots(newSlots: List<TimeSlotItem>) {
        timeSlots.clear()
        timeSlots.addAll(newSlots)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.time_slot_item, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }

    override fun getItemCount(): Int = timeSlots.size

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeSlotTextView: TextView = itemView.findViewById(R.id.timeSlotTextView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        fun bind(timeSlotItem: TimeSlotItem) {
            timeSlotTextView.text = timeSlotItem.timeSlot
            titleTextView.text = timeSlotItem.title.ifEmpty { "No Title" }
            descriptionTextView.text = timeSlotItem.description.ifEmpty { "No Description" }

            itemView.setOnClickListener {
                onItemClickListener?.invoke(timeSlotItem)
            }
        }
    }
}
