package com.example.totalcoach

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.totalcoach.utilities.SharedPreferencesManager
import com.example.yourappname.TrainerPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class TrainerMainActivity : BaseActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var messageIcon: ImageView
    private lateinit var profileImageView: ImageView

    // For handling result of image picker intent
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        // Set up window insets for padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the ViewPager2 and TabLayout
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        messageIcon = findViewById(R.id.messageIcon)
        profileImageView = findViewById(R.id.profileImageView)

        // Set up the ViewPager2 adapter
        val adapter = TrainerPagerAdapter(this)
        viewPager.adapter = adapter

        // Sync the TabLayout with the ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.ic_tasks) // Icon for the Tasks tab
                1 -> tab.setIcon(R.drawable.ic_trainee) // Icon for the Trainee tab
                2 -> tab.setIcon(R.drawable.ic_calendar) // Icon for the Calendar tab
                3 -> tab.setIcon(R.drawable.ic_payment) // Icon for the Saved tab
            }
        }.attach()

        loadUserData()

        // Click Listener for Message Icon
        messageIcon.setOnClickListener { showMessageMenu(it) }

        // Set up image picker result launcher
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val imageUri = result.data?.data
                    if (imageUri != null) {
                        uploadProfileImage(imageUri)
                    }
                }
            }

        // Set click listener for profile image to open image picker
        profileImageView.setOnClickListener {
            openImagePicker()
        }
    }

    private fun showMessageMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.message_menu, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_chat -> {
                    Toast.makeText(this, "Open Chat", Toast.LENGTH_SHORT).show()
                    // TODO: Start Chat Activity
                    true
                }

                R.id.menu_requests -> {
                    Toast.makeText(this, "View Requests", Toast.LENGTH_SHORT).show()
                    // TODO: Open Requests Page
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun loadUserData() {
        val sharedPreferencesManager = SharedPreferencesManager.getInstance()

        // Retrieve saved data
        val fullName = sharedPreferencesManager.getString("user_name", null)
        val profileImageUrl = sharedPreferencesManager.getString("user_profile_url", null)

        if (!fullName.isNullOrEmpty() && !profileImageUrl.isNullOrEmpty()) {
            // Data exists in SharedPreferences, use it
            updateUI(fullName, profileImageUrl)
        } else {
            // No data in SharedPreferences, fetch from Firestore
            loadUserDataFromFirestore()
        }
    }

    private fun loadUserDataFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val fullName = document.getString("fullName") ?: "Guest"
                val profileImageUrl = document.getString("profileImageUrl") ?: ""

                // Update UI
                updateUI(fullName, profileImageUrl)

                // Save data for future use
                SharedPreferencesManager.getInstance().saveString("user_name", fullName)
                SharedPreferencesManager.getInstance()
                    .saveString("user_profile_url", profileImageUrl)
            }
        }
    }

    // Function to update UI
    private fun updateUI(fullName: String, profileImageUrl: String) {
        val profileNameTextView = findViewById<TextView>(R.id.username)

        // Set name
        profileNameTextView.text = fullName

        // Set profile image using Glide
        if (profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(profileImageView)
        }
    }

    // Open the image picker to choose an image
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    // Upload the selected profile image to Firebase Storage
    private fun uploadProfileImage(imageUri: Uri) {
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

    // Update the profile image URL in Firestore
    private fun updateProfileImageInFirestore(profileImageUrl: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserId)

        userRef.update("profileImageUrl", profileImageUrl)
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
}
