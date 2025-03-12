package com.example.totalcoach.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.totalcoach.R
import com.example.totalcoach.adapters.TimeSlotTaskAdapter
import com.example.totalcoach.adapters.TimeSlotTaskItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TaskListFragment : Fragment() {

    private lateinit var todayDateText: TextView
    private lateinit var taskListRecyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val timeSlots = mutableListOf<TimeSlotTaskItem>()
    private var traineeId: String? = null // This should be set when navigating to the fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)

        todayDateText = view.findViewById(R.id.todayDateText)
        taskListRecyclerView = view.findViewById(R.id.taskListRecyclerView)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        taskListRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        traineeId = arguments?.getString("traineeId")

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh the data when the fragment is resumed
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val todayDate = dateFormat.format(Date())
        todayDateText.text = "Today's Date: $todayDate"

        fetchTimeSlotsForToday(todayDate)
    }

    private fun fetchTimeSlotsForToday(date: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            return
        }

        val calendarRef = when {
            // Case 1: Trainee is logged in and fetching their own calendar
            traineeId != null && currentUserId == traineeId -> {
                firestore.collection("users")
                    .document(traineeId!!)
                    .collection("calendar")
            }

            // Case 2: Trainer is logged in and fetching their trainee's calendar
            traineeId != null -> {
                firestore.collection("users")
                    .document(currentUserId)
                    .collection("trainees")
                    .document(traineeId!!)
                    .collection("calendar")
            }

            // Case 3: Trainer is fetching their own calendar
            else -> {
                firestore.collection("users")
                    .document(currentUserId)
                    .collection("calendar")
            }
        }

        calendarRef.document(date)
            .collection("time_slots")
            .get()
            .addOnSuccessListener { result ->
                timeSlots.clear()
                for (document in result) {
                    val timeSlot = document.id
                    val isChecked = document.getBoolean("isChecked") ?: false
                    val title = document.getString("title") ?: "No Title"
                    val description = document.getString("description") ?: "No Description"

                    timeSlots.add(TimeSlotTaskItem(timeSlot, title, description, isChecked))
                }

                val adapter = TimeSlotTaskAdapter(timeSlots) { timeSlotItem, isChecked ->
                    updateTimeSlotCheckedState(timeSlotItem, isChecked)
                }

                taskListRecyclerView.adapter = adapter
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun updateTimeSlotCheckedState(timeSlotItem: TimeSlotTaskItem, isChecked: Boolean) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            return
        }

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val todayDate = dateFormat.format(Date())

        val calendarRef = when {
            // Case 1: Trainee is updating their own task
            traineeId != null && currentUserId == traineeId -> {
                firestore.collection("users")
                    .document(traineeId!!)
                    .collection("calendar")
            }

            // Case 2: Trainer is updating a task for their trainee
            traineeId != null -> {
                firestore.collection("users")
                    .document(currentUserId)
                    .collection("trainees")
                    .document(traineeId!!)
                    .collection("calendar")
            }

            // Case 3: Trainer is updating their own task
            else -> {
                firestore.collection("users")
                    .document(currentUserId)
                    .collection("calendar")
            }
        }

        calendarRef.document(todayDate)
            .collection("time_slots")
            .document(timeSlotItem.timeSlot)
            .update("isChecked", isChecked)
            .addOnSuccessListener {
                // Successfully updated
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
}
