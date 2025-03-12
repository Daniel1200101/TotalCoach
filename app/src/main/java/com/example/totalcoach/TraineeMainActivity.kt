package com.example.totalcoach

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.totalcoach.utilities.ActivityNavigation
import com.example.totalcoach.utilities.SharedPreferencesManager
import com.example.yourappname.TraineePagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class TraineeMainActivity : AppCompatActivity() {

    val sharedPreferencesManager = SharedPreferencesManager.getInstance()
    private lateinit var navigationHelper: ActivityNavigation
    private var traineeId: String? = null
    private val IMAGE_PICKER_REQUEST_CODE = 1001
    private lateinit var profileImageView: ImageView

    // For handling result of image picker intent
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        navigationHelper = ActivityNavigation(this)
        traineeId = intent.getStringExtra("traineeId")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (traineeId == null || currentUserId == null) {
            Toast.makeText(this, "Error: Trainee ID or User ID not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Setup image picker result launcher
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val imageUri = result.data?.data
                    if (imageUri != null) {
                        uploadProfileImage(imageUri)
                    }
                }
            }

        checkUserRole(currentUserId, traineeId!!)
    }

    private fun checkUserRole(currentUserId: String, traineeId: String) {
        val trainerRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .collection("trainees")
            .document(traineeId)

        trainerRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                setTrainerView(traineeId)
            } else {
                setTraineeView(traineeId)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to verify user role", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTabs(traineeId: String, isTrainer: Boolean) {
        val layoutResId =
            if (isTrainer) R.layout.activity_main_trainee_trainer_view else R.layout.activity_main_trainee
        setContentView(layoutResId)

        Log.d("TraineeMainActivity", "View loaded: ${if (isTrainer) "Trainer" else "Trainee"}")

        val username: TextView = findViewById(R.id.username)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)


        if (isTrainer) {
            val backButton: ImageView = findViewById(R.id.return_button)
            val messageIcon: ImageView = findViewById(R.id.messageIcon)

            backButton.visibility = View.VISIBLE
            messageIcon.visibility = View.VISIBLE

            backButton.setOnClickListener {
                navigationHelper.navigateToTrainerMainActivity()
                finish()
            }
            messageIcon.setOnClickListener { showMessageMenu(it) }
        } else {
            profileImageView = findViewById(R.id.profileImageView)
            // Allow profile image click to open image picker
            profileImageView.setOnClickListener {
                openImagePicker()
            }
            findViewById<ImageView>(R.id.return_button)?.visibility = View.GONE
            findViewById<ImageView>(R.id.messageIcon)?.visibility = View.GONE
        }

        val adapter = TraineePagerAdapter(this, traineeId)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.ic_tasks)
                1 -> tab.setIcon(R.drawable.ic_pic_vid)
                2 -> tab.setIcon(R.drawable.ic_training)
                3 -> tab.setIcon(R.drawable.ic_calendar)
            }
        }.attach()

        loadUserData(traineeId, username)
    }

    private fun setTrainerView(traineeId: String) {
        setupTabs(traineeId, true)
    }

    private fun setTraineeView(traineeId: String) {
        setupTabs(traineeId, false)
    }

    private fun loadUserData(traineeId: String, username: TextView) {
        val fullName = sharedPreferencesManager.getString("user_name_$traineeId", null)
        val profileImageUrl =
            sharedPreferencesManager.getString("user_profile_url_$traineeId", null)

        if (!fullName.isNullOrEmpty() && !profileImageUrl.isNullOrEmpty()) {
            updateUI(fullName, profileImageUrl, username)
        } else {
            loadUserDataFromFirestore(traineeId, username)
        }
    }

    private fun loadUserDataFromFirestore(traineeId: String, username: TextView) {
        val trainerId = FirebaseAuth.getInstance().currentUser?.uid
        if (trainerId == null) {
            Toast.makeText(this, "Trainer not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val traineeRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(trainerId)
            .collection("trainees")
            .document(traineeId)

        traineeRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val personalInfo = document.get("personalInfo") as? Map<String, Any>
                val fullName = personalInfo?.get("name") as? String ?: "Guest"
                val profileImageUrl = personalInfo?.get("profileImageUrl") as? String ?: ""

                updateUI(fullName, profileImageUrl, username)

                sharedPreferencesManager.saveString("user_name_$traineeId", fullName)
                sharedPreferencesManager.saveString("user_profile_url_$traineeId", profileImageUrl)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error loading trainee data: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateUI(fullName: String, profileImageUrl: String, username: TextView) {
        username.text = fullName
        if (profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(profileImageView)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun uploadProfileImage(imageUri: android.net.Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
        val fileReference =
            storageReference.child("profile_pictures/${FirebaseAuth.getInstance().currentUser?.uid}.jpg")

        fileReference.putFile(imageUri).addOnSuccessListener {
            // Image uploaded successfully
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                updateProfileImageInFirestore(uri.toString())
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error uploading image: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateProfileImageInFirestore(profileImageUrl: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val trainerId = FirebaseAuth.getInstance().currentUser?.uid
        if (trainerId == null) {
            Toast.makeText(this, "Trainer not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val traineeRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(trainerId)
            .collection("trainees")
            .document(traineeId!!)

        traineeRef.update("personalInfo.profileImageUrl", profileImageUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile picture updated successfully!", Toast.LENGTH_SHORT)
                    .show()
                Glide.with(this)
                    .load(profileImageUrl)
                    .circleCrop()
                    .into(profileImageView)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to update profile image: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showMessageMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.message_menu, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_chat -> {
                    Toast.makeText(this, "Open Chat", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.menu_requests -> {
                    Toast.makeText(this, "View Requests", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
        popup.show()
    }
}
