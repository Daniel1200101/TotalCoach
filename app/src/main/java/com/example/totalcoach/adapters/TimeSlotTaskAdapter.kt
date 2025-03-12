package com.example.totalcoach.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.totalcoach.R

data class TimeSlotTaskItem(
    val timeSlot: String,
    val title: String, // The title of the task
    val description: String, // The description of the task
    var isChecked: Boolean
)

class TimeSlotTaskAdapter(
    private val timeSlots: MutableList<TimeSlotTaskItem>,
    private val onCheckedChangeListener: (TimeSlotTaskItem, Boolean) -> Unit
) : RecyclerView.Adapter<TimeSlotTaskAdapter.TimeSlotTaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotTaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.time_slot_task_item, parent, false)
        return TimeSlotTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotTaskViewHolder, position: Int) {
        val timeSlotItem = timeSlots[position]
        holder.bind(timeSlotItem)
    }

    override fun getItemCount(): Int = timeSlots.size

    inner class TimeSlotTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeSlotTextView: TextView = itemView.findViewById(R.id.timeSlotTextView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val checkBox: CheckBox = itemView.findViewById(R.id.timeSlotCheckBox)

        fun bind(timeSlotItem: TimeSlotTaskItem) {
            timeSlotTextView.text = timeSlotItem.timeSlot
            titleTextView.text = timeSlotItem.title
            descriptionTextView.text = timeSlotItem.description
            checkBox.isChecked = timeSlotItem.isChecked

            // Set checkbox listener
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                timeSlotItem.isChecked = isChecked
                onCheckedChangeListener(timeSlotItem, isChecked)
            }

            // Toggle description visibility when the title or time slot is clicked
            val toggleVisibility = View.OnClickListener {
                descriptionTextView.visibility = if (descriptionTextView.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            timeSlotTextView.setOnClickListener(toggleVisibility)
            titleTextView.setOnClickListener(toggleVisibility)
        }
    }
}

