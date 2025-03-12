package com.example.totalcoach.firebase

import android.provider.Settings.Global.getString
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.totalcoach.R
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthHelper(private val auth: FirebaseAuth, private val oneTapClient: SignInClient) {

    private lateinit var signInRequest: BeginSignInRequest

    init {
        setupOneTapSignIn()
    }

    private fun setupOneTapSignIn() {

    }

    fun startOneTapSignIn() {

    }

    fun firebaseAuthWithGoogle(idToken: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val displayName = user?.displayName ?: "User"
                    onSuccess(displayName)
                } else {
                    onFailure()
                }
            }
    }

    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }
    }
}
