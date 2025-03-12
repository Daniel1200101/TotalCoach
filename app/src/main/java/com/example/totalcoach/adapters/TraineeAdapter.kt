package com.example.totalcoach.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.totalcoach.R
import com.example.totalcoach.models.Trainee
import com.example.totalcoach.TraineeMainActivity

class TraineeAdapter(
    private var trainees: List<Trainee>,
    private val onTraineeClick: (Trainee) -> Unit
) : RecyclerView.Adapter<TraineeAdapter.TraineeViewHolder>() {

    // ViewHolder to hold the trainee item view
    inner class TraineeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val traineeName: TextView = itemView.findViewById(R.id.traineeName)
        val traineeLocation: TextView = itemView.findViewById(R.id.traineeLocation)

        fun bind(trainee: Trainee) {
            // Access the personalInfo inside the Trainee object
            val personalInfo = trainee.personalInfo
            traineeName.text = personalInfo?.name ?: "N/A"
            traineeLocation.text =
                personalInfo?.location ?: "N/A" // Access location from personalInfo

            // Set a click listener to navigate to the trainee's main page
            itemView.setOnClickListener {
                onTraineeClick(trainee) // Trigger the click callback when a trainee is clicked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TraineeViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_trainee, parent, false)
        return TraineeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TraineeViewHolder, position: Int) {
        holder.bind(trainees[position])
    }

    override fun getItemCount(): Int {
        return trainees.size
    }

    // This function allows you to update the list of trainees in the adapter
    fun updateTrainees(newTrainees: List<Trainee>) {
        trainees = newTrainees
        notifyDataSetChanged()  // Notify adapter that the data has changed
    }
}
