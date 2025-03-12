package com.example.totalcoach.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.totalcoach.R
import com.example.totalcoach.enums.TrainingType
import com.example.totalcoach.adapters.TimeSlotAdapter
import com.example.totalcoach.adapters.TimeSlotItem
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private var traineeId: String? = null // Variable to hold traineeId
    private lateinit var selectedDateText: TextView
    private lateinit var timeSlotRecyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private val adapter = TimeSlotAdapter(mutableListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Retrieve the traineeId from the arguments bundle
        traineeId = arguments?.getString("traineeId")

        selectedDateText = view.findViewById(R.id.selectedDateText)
        timeSlotRecyclerView = view.findViewById(R.id.timeSlotRecyclerView)
        firestore = FirebaseFirestore.getInstance()

        timeSlotRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        timeSlotRecyclerView.adapter = adapter

        view.findViewById<View>(R.id.datePickerButton).setOnClickListener {
            showDatePicker()
        }
        return view
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Date(selection)
            displayTimeSlots(selectedDate)
        }

        datePicker.show(childFragmentManager, "DATE_PICKER")
    }

    private fun displayTimeSlots(selectedDate: Date) {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = sdf.format(selectedDate)
        selectedDateText.text = "Selected Date: $formattedDate"

        val defaultTimeSlots = mutableListOf<TimeSlotItem>()
        for (hour in 6..20) {
            val timeSlotStr = String.format("%02d:00", hour)
            defaultTimeSlots.add(TimeSlotItem(date = formattedDate, timeSlot = timeSlotStr))
        }

        adapter.setOnItemClickListener { timeSlotItem ->
            showInputForm(formattedDate, timeSlotItem)
        }

        fetchTimeSlotsFromFirestore(formattedDate, defaultTimeSlots)
    }

    private fun fetchTimeSlotsFromFirestore(selectedDate: String, timeSlots: MutableList<TimeSlotItem>) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val calendarRef = when {
            // Case 1: Trainee is logged in and viewing their own calendar
            traineeId != null && currentUserId == traineeId -> {
                firestore.collection("users")
                    .document(traineeId!!)
                    .collection("calendar")
            }

            // Case 2: Trainer is logged in and viewing a specific trainee's calendar
            traineeId != null -> {
                firestore.collection("users")
                    .document(currentUserId) // Trainer's ID
                    .collection("trainees")
                    .document(traineeId!!) // Specific Trainee's ID
                    .collection("calendar")
            }

            // Case 3: Trainer is logged in and viewing their own calendar
            else -> {
                firestore.collection("users")
                    .document(currentUserId)
                    .collection("calendar")
            }
        }

        // Fetch time slots
        calendarRef.document(selectedDate)
            .collection("time_slots")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val timeSlotId = document.id
                    val title = document.getString("title") ?: "No Title"
                    val description = document.getString("description") ?: "No Description"

                    val existingSlot = timeSlots.find { it.timeSlot == timeSlotId }
                    if (existingSlot != null) {
                        existingSlot.title = title
                        existingSlot.description = description
                    }
                }
                adapter.updateTimeSlots(timeSlots)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showInputForm(selectedDate: String, timeSlotItem: TimeSlotItem) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if the current user is the trainer
        if (traineeId != null && currentUserId == traineeId) {
            Toast.makeText(requireContext(), "Trainees cannot edit the calendar.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.input_form_layout, null)
        val titleEditText = dialogView.findViewById<AutoCompleteTextView>(R.id.eventTitleEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.eventDescriptionEditText)
        val selectTitleButton = dialogView.findViewById<ImageButton>(R.id.selectTitleButton)

        titleEditText.setText(timeSlotItem.title)
        descriptionEditText.setText(timeSlotItem.description)

        val trainingTypes = TrainingType.entries.map { it.displayName }.toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, trainingTypes)
        titleEditText.setAdapter(adapter)

        selectTitleButton.setOnClickListener {
            titleEditText.showDropDown()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Enter Information for ${timeSlotItem.timeSlot}")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                timeSlotItem.title = titleEditText.text.toString()
                timeSlotItem.description = descriptionEditText.text.toString()
                timeSlotRecyclerView.adapter?.notifyDataSetChanged()

                saveTimeSlotToFirestore(selectedDate, timeSlotItem.timeSlot, timeSlotItem.title, timeSlotItem.description)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun saveTimeSlotToFirestore(date: String, timeSlot: String, title: String, description: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val calendarRef = when {
            // Case 1: Trainee is logged in and saving a time slot in their own calendar
            traineeId != null && currentUserId == traineeId -> {
                firestore.collection("users")
                    .document(traineeId!!)
                    .collection("calendar")
            }

            // Case 2: Trainer is logged in and saving a time slot for a specific trainee
            traineeId != null -> {
                firestore.collection("users")
                    .document(currentUserId) // Trainer's ID
                    .collection("trainees")
                    .document(traineeId!!) // Specific Trainee's ID
                    .collection("calendar")
            }

            // Case 3: Trainer is logged in and saving a time slot in their own calendar
            else -> {
                firestore.collection("users")
                    .document(currentUserId)
                    .collection("calendar")
            }
        }

        val timeSlotData = hashMapOf(
            "title" to title,
            "description" to description,
            "isChecked" to false,
            "timeSlot" to timeSlot
        )

        calendarRef.document(date)
            .collection("time_slots")
            .document(timeSlot)
            .set(timeSlotData)
            .addOnSuccessListener {
                Log.d("CalendarFragment", "Time slot saved successfully.")
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarFragment", "Error saving time slot", exception)
            }
    }
}
