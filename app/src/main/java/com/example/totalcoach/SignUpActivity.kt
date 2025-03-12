package com.example.totalcoach

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.totalcoach.enums.Role
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : AppCompatActivity() {
    private var emailInput: EditText? = null
    private var passwordInput: EditText? = null
    private lateinit var signUpButton: MaterialButton
    private lateinit var signInButton: Button
    private var progressBar: ProgressBar? = null
    private var mAuth: FirebaseAuth? = null // Firebase Authentication instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance() // Initialize Firebase Auth

        emailInput = findViewById<EditText>(R.id.emailInput)
        passwordInput = findViewById<EditText>(R.id.passwordInput)
        signUpButton = findViewById<MaterialButton>(R.id.sign_up_button)
       // progressBar = findViewById<ProgressBar>(R.id.progressBar)
        signInButton = findViewById<Button>(R.id.sign_in_button)

        signInButton.setOnClickListener(View.OnClickListener { redirectToLogin() })
        signUpButton.setOnClickListener(View.OnClickListener { registerUser() })

    }

    private fun registerUser() {
        val email = emailInput!!.text.toString().trim()
        val password = passwordInput!!.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            emailInput!!.error = "Email is required!"
            return
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput!!.error = "Password is required!"
            return
        }
        if (password.length < 6) {
            passwordInput!!.error = "Password must be at least 6 characters!"
            return
        }


        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth!!.currentUser
                    if (user != null) {
                        saveUserData(user.uid, email)  // Save user info
                        user.sendEmailVerification().addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Verification email sent. Please verify before continuing.",
                                    Toast.LENGTH_LONG
                                ).show()
                                waitForEmailVerification(user)
                            } else {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Failed to send verification email. Try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Registration Failed: " + task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }


    private fun waitForEmailVerification(user: FirebaseUser) {
        val handler = android.os.Handler()
        val checkVerification = object : Runnable {
            override fun run() {
                user.reload() // Refresh user data
                if (user.isEmailVerified) {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Email verified! Registration complete.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Redirect to login after successful verification
                    mAuth!!.signOut()
                    redirectToLogin()
                } else {
                    // Check again in 3 seconds
                    handler.postDelayed(this, 3000)
                }
            }
        }
        handler.postDelayed(checkVerification, 3000) // Start checking
    }
    private fun saveUserData(userId: String, email: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userMap = hashMapOf(
            "email" to email,
            "created_at" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered as Trainer!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}