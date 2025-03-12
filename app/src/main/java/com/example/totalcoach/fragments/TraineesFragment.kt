package com.example.totalcoach.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.totalcoach.AddTraineeActivity
import com.example.totalcoach.R
import com.example.totalcoach.TraineeMainActivity
import com.example.totalcoach.adapters.TraineeAdapter
import com.example.totalcoach.models.Trainee
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TraineesFragment : Fragment(R.layout.fragment_trainee) {

    private lateinit var traineeRecyclerView: RecyclerView
    private lateinit var addTraineeButton: FloatingActionButton
    private lateinit var traineeAdapter: TraineeAdapter
    private val traineeList = mutableListOf<Trainee>()

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        traineeRecyclerView = view.findViewById(R.id.traineesRecyclerView)
        traineeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize Floating Action Button
        addTraineeButton = view.findViewById(R.id.addTraineeButton)

        // Set the adapter for the RecyclerView with a click handler
        traineeAdapter = TraineeAdapter(traineeList) { trainee ->
            navigateToTraineeMainPage(trainee)
        }

        traineeRecyclerView.adapter = traineeAdapter

        // Fetch trainees from Firestore
        fetchTrainees()

        // Handle Floating Action Button click
        addTraineeButton.setOnClickListener {
            val intent = Intent(requireContext(), AddTraineeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchTrainees() {
        val trainerId = auth.currentUser?.uid
        if (trainerId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch trainees from Firestore under the current trainer
        db.collection("users")
            .document(trainerId)
            .collection("trainees")
            .get()
            .addOnSuccessListener { result ->
                traineeList.clear() // Clear the existing list

                for (document in result) {
                    try {
                        // Convert Firestore document to Trainee object
                        val trainee = document.toObject(Trainee::class.java).copy(id = document.id)

                        traineeList.add(trainee)
                    } catch (e: Exception) {
                        Log.e("TraineesFragment", "Error parsing trainee: ${e.message}")
                    }
                }

                // Update the adapter with the new list of trainees
                traineeAdapter.updateTrainees(traineeList)
            }
            .addOnFailureListener { e ->
                Log.e("TraineesFragment", "Error fetching trainees: ${e.message}")
                Toast.makeText(requireContext(), "Error fetching trainees", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun navigateToTraineeMainPage(trainee: Trainee) {
        val intent = Intent(requireContext(), TraineeMainActivity::class.java)
        intent.putExtra("traineeId", trainee.id) // Consistent key name
        startActivity(intent)
    }
}
