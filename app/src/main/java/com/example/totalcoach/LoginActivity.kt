package com.example.totalcoach

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.totalcoach.utilities.ActivityNavigation
import com.example.totalcoach.utilities.SharedPreferencesManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val REQ_ONE_TAP = 2  // Unique request code for One Tap
    val sharedPreferencesManager = SharedPreferencesManager.getInstance()
    private lateinit var navigationHelper: ActivityNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        setupOneTapSignIn()

        navigationHelper = ActivityNavigation(this)

        val emailField = findViewById<EditText>(R.id.emailInput)
        val passwordField = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<MaterialButton>(R.id.loginButton)
        val signUpButton = findViewById<MaterialButton>(R.id.signUpButton)
        val googleSignInButton = findViewById<MaterialButton>(R.id.appProvider)

        // Handle deep link if it exists
        handleDeepLink()

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        signUpButton.setOnClickListener {
            navigationHelper.navigateToSignUp()
            finish() // Close the LoginActivity after navigating to SignUpActivity
        }

        googleSignInButton.setOnClickListener {
            startOneTapSignIn()
        }
    }

    private fun setupOneTapSignIn() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false) // Set to true for returning users
                    .build()
            ).build()
    }

    private fun handleDeepLink() {
        val appLinkIntent: Intent? = intent
        val appLinkData: Uri? = appLinkIntent?.data

        // Check if the app was opened via a deep link
        if (appLinkData != null) {
            val code = appLinkData.getQueryParameter("code")
            if (code != null) {
                // Automatically fill in the password field with the code
                val passwordField = findViewById<EditText>(R.id.passwordInput)
                passwordField.setText(code)
            }
        }
    }

    private fun startOneTapSignIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                startIntentSenderForResult(
                    result.pendingIntent.intentSender, REQ_ONE_TAP,
                    null, 0, 0, 0, null
                )
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "One Tap Sign-In failed: ${e.localizedMessage}")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    Log.d(TAG, "No ID token found.")
                }
            } catch (e: ApiException) {
                Log.e(TAG, "One Tap Sign-In error: ${e.localizedMessage}")
            }
        }
    }

    private fun saveUserNameToPreferences(userName: String) {
        sharedPreferencesManager.saveString("USER_NAME", userName)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val displayName = user?.displayName ?: "User" // Default if name is null
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    // Save the user's name to SharedPreferences
                    saveUserNameToPreferences(displayName)

                    // Determine next activity
                    if (isNewUser) {
                        navigationHelper.navigateToPersonalDetails()
                    } else {
                        checkUserRole()
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginUser(email: String, password: String) {
        // Attempt to sign in the user with the provided email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Check if the user has completed the form submission
                    checkUserRole()
                } else {
                    // Handle authentication failure
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserRole() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role") // Assuming role is saved in Firestore
                        when (role) {
                            "Trainer" -> {
                                navigationHelper.navigateToTrainerMainActivity()
                                finish() // Close the LoginActivity after navigating to the TrainerMainActivity
                            }
                            else -> {
                                val intent = Intent(this, TraineeMainActivity::class.java)
                                intent.putExtra("traineeId", userId)  // Add the Trainee ID as an extra
                                startActivity(intent)
                                finish() // Close the LoginActivity
                            }
                        }
                    } else {
                        navigationHelper.navigateToTraineeMainActivity()
                        finish() // Close the LoginActivity if no role is found
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch user role.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
