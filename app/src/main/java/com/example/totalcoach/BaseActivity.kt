package com.example.totalcoach

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val TIMEOUT_DURATION = 5 * 60 * 1000 // 5 minutes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences to store the last active time
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Check if the user is already signed in and reset the session timer if logged in
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Reset the last active time on successful login or on app restart
            val editor = sharedPreferences.edit()
            editor.putLong("lastActiveTime", System.currentTimeMillis())  // Set current time as the last active time
            editor.apply()
        }
    }

    override fun onResume() {
        super.onResume()
        checkSessionTimeout()
    }

    override fun onPause() {
        super.onPause()
        saveLastActiveTime()
    }

    private fun saveLastActiveTime() {
        val editor = sharedPreferences.edit()
        editor.putLong("lastActiveTime", System.currentTimeMillis())
        editor.apply()
    }

    private fun checkSessionTimeout() {
        val lastActiveTime = sharedPreferences.getLong("lastActiveTime", 0)
        val currentTime = System.currentTimeMillis()

        if (lastActiveTime != 0L && (currentTime - lastActiveTime) > TIMEOUT_DURATION) {
            FirebaseAuth.getInstance().signOut() // Sign out user
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()

            // Redirect to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
