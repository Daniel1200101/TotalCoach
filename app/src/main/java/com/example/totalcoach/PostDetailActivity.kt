package com.example.totalcoach

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.totalcoach.utilities.ActivityNavigation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class PostDetailActivity : AppCompatActivity() {

    private lateinit var returnButton: ImageButton
    private lateinit var editButton: TextView
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var fullImageView: ImageView
    private lateinit var fullVideoView: VideoView
    private lateinit var navigationHelper: ActivityNavigation

    private var mediaId: String? = null
    private var trainerId: String? = null
    private var traineeId: String? = null
    private var mediaUrl: String? = null
    private var mediaType: String? = null
    private var title: String? = null
    private var description: String? = null
    private lateinit var editModeButtons: LinearLayout  // Declare it

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // Initialize views
        returnButton = findViewById(R.id.returnButton)
        editButton = findViewById(R.id.editButton)
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
        titleTextView = findViewById(R.id.titleTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        fullImageView = findViewById(R.id.fullImageView)
        fullVideoView = findViewById(R.id.fullVideoView)
        navigationHelper = ActivityNavigation(this)
        editModeButtons = findViewById(R.id.editModeButtons)  // Initialize it

        returnButton.setOnClickListener {
            navigationHelper.navigateToTraineeMainActivity()
            finish()
        }

        editButton.setOnClickListener { enterEditMode() }
        cancelButton.setOnClickListener { exitEditMode() }
        saveButton.setOnClickListener { savePostToFirestore() }
        deleteButton.setOnClickListener { confirmDeletePost() }

        // Retrieve data from intent
        mediaUrl = intent.getStringExtra("imageUrl")
        mediaId = intent.getStringExtra("mediaId")
        trainerId = intent.getStringExtra("trainerId")
        traineeId = intent.getStringExtra("traineeId")
        title = intent.getStringExtra("caption")
        description = intent.getStringExtra("description")
        mediaType = intent.getStringExtra("mediaType")

        // Load the correct media type
        loadMedia()

        titleTextView.text = title
        descriptionTextView.text = description

        Log.e("PostDetailActivity", "Error: Missing data - Media ID: $mediaId, Trainer ID: $trainerId, Trainee ID: $traineeId, Media URL: $mediaUrl, Media Type: $mediaType")
    }
    override fun onPause() {
        super.onPause()
        // Pause video playback when the activity goes into the background
        fullVideoView.pause()
    }

    override fun onResume() {
        super.onResume()
        // Resume video playback when the activity comes back to the foreground
        if (fullVideoView.isPlaying) {
            fullVideoView.start() // Make sure the video resumes if it's playing
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop video playback if the activity is destroyed
        fullVideoView.stopPlayback()
    }


    private fun loadMedia() {
        if (mediaType == "video") {
            fullImageView.visibility = View.GONE
            fullVideoView.visibility = View.VISIBLE

            val videoUri = Uri.parse(mediaUrl)

            fullVideoView.setVideoURI(videoUri)
            fullVideoView.setOnPreparedListener { mp ->
                mp.isLooping = true
                fullVideoView.start()
            }

            fullVideoView.setOnErrorListener { mp, what, extra ->
                Log.e("PostDetailActivity", "Video error: $what, $extra")
                Toast.makeText(this, "Failed to play video", Toast.LENGTH_SHORT).show()
                return@setOnErrorListener true
            }
        } else {
            fullVideoView.visibility = View.GONE
            fullImageView.visibility = View.VISIBLE
            Glide.with(this).load(mediaUrl).into(fullImageView)
        }
    }


    private fun enterEditMode() {
        titleTextView.visibility = View.GONE
        descriptionTextView.visibility = View.GONE
        titleEditText.visibility = View.VISIBLE
        descriptionEditText.visibility = View.VISIBLE
        editButton.visibility = View.GONE
        editModeButtons.visibility = View.VISIBLE
        deleteButton.visibility = View.VISIBLE

        titleEditText.setText(title)
        descriptionEditText.setText(description)
    }

    private fun exitEditMode() {
        titleTextView.visibility = View.VISIBLE
        descriptionTextView.visibility = View.VISIBLE
        titleEditText.visibility = View.GONE
        descriptionEditText.visibility = View.GONE
        editButton.visibility = View.VISIBLE
        editModeButtons.visibility = View.GONE
        deleteButton.visibility = View.GONE
    }

    private fun savePostToFirestore() {
        val newTitle = titleEditText.text.toString().trim()
        val newDescription = descriptionEditText.text.toString().trim()

        if (mediaId == null || trainerId == null || traineeId == null) {
            Toast.makeText(this, "Error: Missing media details", Toast.LENGTH_SHORT).show()
            return
        }

        val mediaRef = db.collection("trainers").document(trainerId!!)
            .collection("trainees").document(traineeId!!)
            .collection("media").document(mediaId!!)

        val updates = mapOf(
            "caption" to newTitle,
            "description" to newDescription
        )

        mediaRef.update(updates)
            .addOnSuccessListener {
                title = newTitle
                description = newDescription
                titleTextView.text = newTitle
                descriptionTextView.text = newDescription
                exitEditMode()
                Toast.makeText(this, "Post updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDeletePost() {
        AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                deletePostFromFirestore()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePostFromFirestore() {
        if (mediaId == null || trainerId == null || traineeId == null) {
            Toast.makeText(this, "Error: Missing media details", Toast.LENGTH_SHORT).show()
            return
        }

        val mediaRef = db.collection("trainers").document(trainerId!!)
            .collection("trainees").document(traineeId!!)
            .collection("media").document(mediaId!!)

        mediaRef.delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()
                deleteMediaFromStorage()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteMediaFromStorage() {
        if (mediaUrl == null) return

        val storageRef = storage.getReferenceFromUrl(mediaUrl!!)
        storageRef.delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Media deleted from storage", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Storage delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
