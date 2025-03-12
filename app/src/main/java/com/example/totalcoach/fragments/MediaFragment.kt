package com.example.totalcoach.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.totalcoach.PostActivity
import com.example.totalcoach.R
import com.example.totalcoach.adapters.MediaAdapter
import com.example.totalcoach.models.MediaItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MediaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: MaterialButton
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var filterTrainerButton: MaterialButton
    private lateinit var filterTraineeButton: MaterialButton

    private var traineeId: String? = null
    private var trainerId: String? = null
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var currentUser: String? = null
    private val storageRef = Firebase.storage.reference
    private var fileUri: Uri? = null

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            fileUri = uri
            navigateToPostActivity(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trainee_media, container, false)
        recyclerView = view.findViewById(R.id.mediaRecyclerView)
        addButton = view.findViewById(R.id.addMediaButton)
        filterTrainerButton = view.findViewById(R.id.filterTrainer)
        filterTraineeButton = view.findViewById(R.id.filterTrainee)

        recyclerView.layoutManager = GridLayoutManager(context, 3)
        mediaAdapter = MediaAdapter(emptyList())
        recyclerView.adapter = mediaAdapter

        traineeId = arguments?.getString("traineeId")

        if (currentUserId == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return view
        }

        determineUserRoleAndFetchData()

        addButton.setOnClickListener {
            openImagePicker()
        }

        // Filter buttons logic
        filterTrainerButton.setOnClickListener {
            fetchMedia(isTrainerFilter = true)
        }

        filterTraineeButton.setOnClickListener {
            fetchMedia(isTrainerFilter = false)
        }

        return view
    }

    private fun determineUserRoleAndFetchData() {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(currentUserId!!)
            .collection("trainees")
            .document(traineeId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    trainerId = currentUserId // The user is the trainer
                    currentUser = "trainer"
                }else
                    currentUser = "trainee"
                // Fetch media based on the user's role (default to trainer's media)
                fetchMedia(isTrainerFilter = true)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to determine user role", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchMedia(isTrainerFilter: Boolean) {
        if (trainerId == null || traineeId == null) {
            Toast.makeText(context, "Error fetching media", Toast.LENGTH_SHORT).show()
            return
        }

        val query = if (isTrainerFilter) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(trainerId!!)
                .collection("trainees")
                .document(traineeId!!)
                .collection("media").
                whereEqualTo("uploadedBy", "trainer")
                .orderBy("timestamp", Query.Direction.DESCENDING)
        } else {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(trainerId!!)
                .collection("trainees")
                .document(traineeId!!)
                .collection("media")
                .whereEqualTo("uploadedBy", "trainee") // Assuming there's a field to filter trainee's media
                .orderBy("timestamp", Query.Direction.DESCENDING)
        }

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(context, "Error fetching media: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("MediaFragment", "Error fetching media: ${e.message}", e)
                return@addSnapshotListener
            }
            val mediaList = snapshot?.documents?.mapNotNull { document ->
                document.toObject(MediaItem::class.java)
            } ?: emptyList()
            mediaAdapter = MediaAdapter(mediaList)
            recyclerView.adapter = mediaAdapter
        }
    }

    private fun openImagePicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }

    private fun navigateToPostActivity(uri: Uri) {
        val intent = Intent(context, PostActivity::class.java)
        intent.putExtra("imageUri", uri.toString())
        intent.putExtra("trainerId", trainerId)
        intent.putExtra("traineeId", traineeId)
        intent.putExtra("currentUser", currentUser)
        startActivity(intent)
    }
}
