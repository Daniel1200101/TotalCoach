package com.example.totalcoach

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.totalcoach.models.MediaItem
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.Timestamp

class PostActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var postButton: Button
    private var selectedImageUri: Uri? = null
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    private var trainerId: String? = null
    private var traineeId: String? = null
    private var currentUser: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        imageView = findViewById(R.id.selectedImageView)
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        postButton = findViewById(R.id.postButton)

        val returnButton: ImageButton = findViewById(R.id.returnButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, TraineeMainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Get the URI and IDs from the Intent
        val imageUriString = intent.getStringExtra("imageUri")
        trainerId = intent.getStringExtra("trainerId")
        traineeId = intent.getStringExtra("traineeId")
        currentUser = intent.getStringExtra("currentUser")
        if (imageUriString != null) {
            selectedImageUri = Uri.parse(imageUriString)
            imageView.setImageURI(selectedImageUri)
        }

        postButton.setOnClickListener {
            postMedia()
        }
    }

    private fun postMedia() {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()

        if (selectedImageUri != null && title.isNotEmpty() && description.isNotEmpty()) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Uploading...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            // Upload the selected media
            uploadMediaToFirebase(selectedImageUri!!, title, description, progressDialog)
        } else {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadMediaToFirebase(uri: Uri, title: String, description: String, progressDialog: ProgressDialog) {
        val mediaRef = storageRef.child("media/${System.currentTimeMillis()}_${uri.lastPathSegment}")

        mediaRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                // Successfully uploaded, get the download URL
                mediaRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Save media details to Firestore
                    saveMediaDetailsToFirestore(downloadUri.toString(), title, description, progressDialog)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveMediaDetailsToFirestore(downloadUrl: String, title: String, description: String, progressDialog: ProgressDialog) {
        val db = FirebaseFirestore.getInstance()

        // Generate a unique media ID
        val mediaRef = db.collection("users")
            .document(trainerId!!)
            .collection("trainees")
            .document(traineeId!!)
            .collection("media")
            .document() // Generate ID

        val mediaId = mediaRef.id // Get the generated ID

        // Check if the media URL is a video or image based on the extension
        val mediaType = when {
            downloadUrl.endsWith(".mp4") || downloadUrl.endsWith(".mov") || downloadUrl.endsWith(".avi") -> {
                "video" // It's a video
            }
            else -> {
                "image" // It's an image
            }
        }

        val mediaItem = MediaItem(
            type = mediaType, // Set type as video or image
            url = downloadUrl, // Save the media URL properly
            caption = title,
            description = description,
            timestamp = Timestamp.now(),
            uploadedBy = currentUser ?: "",
            trainerId = trainerId ?: "",
            traineeId = traineeId ?: "",
            mediaId = mediaId // Save the media ID
        )

        // Set the media document using the generated ID
        mediaRef.set(mediaItem)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Media posted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to post media: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

