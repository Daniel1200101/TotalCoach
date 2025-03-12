package com.example.totalcoach

import FirebaseApiService
import SendEmailRequest
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.totalcoach.R
import com.example.totalcoach.models.PersonalInfo
import com.example.totalcoach.models.Trainee
import com.example.totalcoach.utilities.ActivityNavigation
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class AddTraineeActivity : AppCompatActivity() {

    // Views for input fields
    private lateinit var nameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var genderInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var saveButton: MaterialButton
    private lateinit var returnButton: ImageButton // Declare return button

    // Firebase references
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var navigation: ActivityNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trainee)
        navigation = ActivityNavigation(this)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        initializeViews()

        // Set button listeners
        setButtonListeners()
    }

    private fun initializeViews() {
        nameInput = findViewById(R.id.nameInput)
        ageInput = findViewById(R.id.ageInput)
        genderInput = findViewById(R.id.genderInput)
        heightInput = findViewById(R.id.heightInput)
        emailInput = findViewById(R.id.emailInput)
        locationInput = findViewById(R.id.locationInput)
        phoneInput = findViewById(R.id.phoneInput)
        saveButton = findViewById(R.id.save_button)
        returnButton = findViewById(R.id.return_button)
    }

    private fun setButtonListeners() {
        saveButton.setOnClickListener {
            handleSaveButtonClick()
            navigation.navigateToTrainerMainActivity()
            finish()
        }

        returnButton.setOnClickListener {
            navigation.navigateToTrainerMainActivity() // Navigates to TrainerMainActivity
            finish()
        }
    }

    private fun handleSaveButtonClick() {
        val name = nameInput.text.toString()
        val age = ageInput.text.toString().toIntOrNull()
        val gender = genderInput.text.toString()
        val height = heightInput.text.toString().toDoubleOrNull()
        val email = emailInput.text.toString()
        val location = locationInput.text.toString()
        val phone = phoneInput.text.toString()

        if (isInputValid(name, age, gender, height, email, location, phone)) {
            val trainerId = auth.currentUser?.uid
            if (trainerId == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            val uniqueCode = UUID.randomUUID().toString()
            val traineeId = UUID.randomUUID().toString() // Generate a unique ID for the trainee

            // Prepare the trainee's personal information
            val trainee = Trainee(
                id = traineeId, // Set the unique ID
                personalInfo = PersonalInfo(
                    name = name,
                    age = age,
                    gender = gender,
                    height = height,
                    email = email,
                    location = location,
                    phone = phone
                )
            )

            saveTraineeToFirestore(trainerId, trainee, uniqueCode, email)
        }
    }


    private fun isInputValid(
        name: String, age: Int?, gender: String, height: Double?,
        email: String, location: String, phone: String
    ): Boolean {
        return name.isNotEmpty() && age != null && gender.isNotEmpty() && height != null && email.isNotEmpty() && location.isNotEmpty() && phone.isNotEmpty()
    }

    private fun saveTraineeToFirestore(trainerId: String, trainee: Trainee, uniqueCode: String, email: String) {
        // Reference to the trainee document
        val traineeRef = firestore.collection("users")
            .document(trainerId) // Trainer ID document
            .collection("trainees") // Trainee collection
            .document(trainee.id!!) // Save with the custom generated trainee ID

        // Prepare login information
        val loginInfo = mapOf(
            "email" to email,
            "passCode" to uniqueCode
        )

        // Prepare personal information
        val personalInfo = mapOf(
            "name" to trainee.personalInfo?.name,
            "age" to trainee.personalInfo?.age,
            "gender" to trainee.personalInfo?.gender,
            "height" to trainee.personalInfo?.height,
            "email" to trainee.personalInfo?.email,
            "location" to trainee.personalInfo?.location,
            "phone" to trainee.personalInfo?.phone
        )

        // Add the trainerId to link the trainee with the trainer
        val traineeData = mapOf(
            "trainerId" to trainerId,  // Link the trainer ID here
            "personalInfo" to personalInfo,
            "loginInfo" to loginInfo
        )

        // Save both personalInfo and loginInfo in the same document
        traineeRef.set(traineeData)
            .addOnSuccessListener {
                // After saving to Firestore, create Firebase Authentication account for the trainee
                createAuthAccount(email, uniqueCode)
            }
            .addOnFailureListener { e ->
                Log.e("TraineeRegistration", "Failed to save trainee data to Firestore", e)
                Toast.makeText(this, "Failed to save trainee data", Toast.LENGTH_SHORT).show()
            }
    }


    // Function to create Firebase Authentication user for the trainee
    private fun createAuthAccount(email: String, uniqueCode: String) {
        auth.createUserWithEmailAndPassword(email, uniqueCode)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Account created successfully
                    Toast.makeText(this, "Trainee Registered & Email Sent", Toast.LENGTH_LONG).show()

                    // Optionally, you can call your method to send the email
                    sendEmailToTrainee(email, uniqueCode)

                    // Finish or navigate as needed
                    finish() // Navigates back or performs another action
                } else {
                    Toast.makeText(this, "Failed to create trainee account: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }


    // Function to send email to the trainee
    private fun sendEmailToTrainee(traineeEmail: String, uniqueCode: String) {
        // Firebase function URL
        val firebaseFunctionUrl =
            "https://us-central1-<your-project-id>.cloudfunctions.net/sendEmail"

        // Create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl(firebaseFunctionUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(FirebaseApiService::class.java)

        // Create the request body
        val requestBody = SendEmailRequest(traineeEmail, uniqueCode)

        // Make the request in a background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.sendEmail(requestBody)
                if (response.isSuccessful) {
                    // Handle success (email sent)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AddTraineeActivity,
                            "Login code sent to the trainee",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // Handle failure (something went wrong)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AddTraineeActivity,
                            "Failed to send the email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddTraineeActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
