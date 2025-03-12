package com.example.totalcoach

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import android.util.Log
import android.view.MotionEvent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.totalcoach.utilities.ActivityNavigation
import com.example.totalcoach.utilities.AddressManager
import com.example.totalcoach.utilities.ImageLoader
import com.example.totalcoach.utilities.SharedPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.*

class PersonalDetailsActivity : AppCompatActivity() {

    private lateinit var birthdayEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var fullNameEditText: EditText
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var btnContinue: MaterialButton
    private lateinit var selectImageButton: MaterialButton
    val storageRef = Firebase.storage.reference
    private var file_uri: Uri? = null
    private val sharedPreferencesManager = SharedPreferencesManager.getInstance()
    private lateinit var addressManager: AddressManager
    private lateinit var navigationHelper: ActivityNavigation

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            file_uri = uri
            // Load the selected image into profileImageView
            ImageLoader
                .getInstance()
                .loadImage(file_uri!!, profileImageView)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_details)
        navigationHelper = ActivityNavigation(this)

        // Save form submission status to SharedPreferences
        sharedPreferencesManager.saveString("form_submission_status", "no")

        // Initialize views and date picker
        initViews()
        initDatePicker()

        // Initialize the AddressManager instance
        addressManager = AddressManager(this)

        // Call the separate function to set up the TextWatcher for the location EditText
    }



    private fun initViews() {
        // Initialize views
        birthdayEditText = findViewById(R.id.birthday)
        addressEditText = findViewById(R.id.address)
        profileImageView = findViewById(R.id.profileImageView)
        btnContinue = findViewById(R.id.btnContinue)
        selectImageButton = findViewById(R.id.selectImageButton)
        fullNameEditText = findViewById(R.id.fullNameInput)

        // Handle button click for selecting profile image
        selectImageButton.setOnClickListener {
            openImagePicker()
        }
        // Handle button click for finishing the form
        btnContinue.setOnClickListener {
            handleFormSubmission()
        }
    }

    // Open the image picker to select an image
    private fun openImagePicker() {
        // Launch the photo picker and let the user choose images
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // Handle form submission
    private fun handleFormSubmission() {
        // Get the entered values
        val fullName = fullNameEditText.text.toString().trim()
        val birthday = birthdayEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()

        // Simple validation for empty fields
        if (fullName.isEmpty() || birthday.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            sharedPreferencesManager.saveString("form_submission_status", "no")
            return
        }

        // Validate birthday format
        if (!isValidBirthday(birthday)) {
            Toast.makeText(this, "Please enter a valid birthday (YYYY-MM-DD)", Toast.LENGTH_SHORT).show()
            sharedPreferencesManager.saveString("form_submission_status", "no")
            return
        }

        // Validate address using AddressManager
        addressManager.validateAddress(address, this) { isValid ->
            if (!isValid) {
                Toast.makeText(this, "Please enter a valid address", Toast.LENGTH_SHORT).show()
                sharedPreferencesManager.saveString("form_submission_status", "no")
                return@validateAddress
            }
        }

        // Proceed with Firestore operations
        savePersonalDataToFirestore(fullName, birthday, address)

        // Only upload the image if one has been selected, otherwise use the default image
        if (file_uri != null) {
            uploadSelectedImage(file_uri)
        } else {
            // Optionally, use a default image (you can assign a default URI if needed)
            val defaultImageUri = Uri.parse("android.resource://your.package.name/drawable/unavailable_photo") // Example URI for a default image
            uploadSelectedImage(defaultImageUri)

            Toast.makeText(this, "No profile picture selected. Using default image.", Toast.LENGTH_SHORT).show()
        }


        // Save valid data and proceed
        sharedPreferencesManager.saveString("form_submission_status", "yes")

        Toast.makeText(this, "Form submitted!", Toast.LENGTH_SHORT).show()
        navigationHelper.navigateToTrainerMainActivity()
        finish()
    }

    // Validate the birthday format and date
    private fun isValidBirthday(birthday: String): Boolean {
        val regex = """\d{4}-\d{2}-\d{2}""".toRegex()
        if (!birthday.matches(regex)) {
            return false
        }

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false
            val date = dateFormat.parse(birthday)
            val currentDate = Date()
            date?.before(currentDate) == true
        } catch (e: Exception) {
            false
        }
    }

    // Upload the selected image to Firebase Storage and save the URL to Firestore
    private fun uploadSelectedImage(uri: Uri?) {
        if (uri == null) {
            Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
            return
        }

        // Reference to Firebase Storage
        val ref = storageRef.child("profile_pictures/${uri.lastPathSegment}")

        // Upload the file to Firebase Storage
        val uploadTask = ref.putFile(uri)

        uploadTask.addOnSuccessListener {
            // Retrieve the download URL after the upload
            ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                saveImageUrlToFirestore(downloadUrl.toString())
            }
            // Handle successful upload
            Toast.makeText(this, "Successfully uploaded file", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            // Handle failed upload
            Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Save the image URL to Firestore under the user's document
    private fun saveImageUrlToFirestore(downloadUrl: String) {
        // Get the current user ID (you might already be using Firebase Authentication)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Reference to the user's document in Firestore
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        // Save the profile image URL to Firestore
        userRef.update("profileImageUrl", downloadUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePersonalDataToFirestore(fullName: String, birthday: String, location: String) {
        // Get the current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Create a map with the user's personal details
        val userMap = hashMapOf(
            "birthday" to birthday,
            "location" to location,
            "fullName" to fullName,
            "role" to "Trainer", // Assign the Trainer role
        )
        // Reference to Firestore users collection
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        // Save personal data to Firestore
        userRef.set(userMap, SetOptions.merge()) // Merge with existing data, if any
            .addOnSuccessListener {
                Toast.makeText(this, "Personal data saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save personal data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year: Int, month: Int, dayOfMonth: Int ->
                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", dayOfMonth)
                birthdayEditText.setText("$year-$formattedMonth-$formattedDay")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        birthdayEditText.setOnClickListener { // Use setOnClickListener
            datePickerDialog.show()
        }

        birthdayEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = 2
                birthdayEditText.compoundDrawables[drawableRight]?.let { drawable ->
                    if (event.rawX >= (birthdayEditText.right - drawable.bounds.width() - birthdayEditText.paddingEnd)) {
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

}
