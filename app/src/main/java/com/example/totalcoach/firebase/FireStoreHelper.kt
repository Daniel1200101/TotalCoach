package com.example.totalcoach.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.net.Uri

class FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    // Save or update user's form submission status
    fun saveFormSubmissionStatus(userId: String, status: String, callback: (Boolean) -> Unit) {
        val userRef = db.collection("users").document(userId)
        userRef.update("form_submission_status", status)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Save user's personal details (fullName, birthday, and address)
    fun savePersonalData(userId: String, fullName: String, birthday: String, address: String, callback: (Boolean) -> Unit) {
        val userMap = hashMapOf(
            "fullName" to fullName,
            "birthday" to birthday,
            "address" to address
        )

        val userRef = db.collection("users").document(userId)
        userRef.set(userMap)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Upload profile image and save the image URL to Firestore
    fun uploadProfileImage(userId: String, uri: Uri, callback: (Boolean, String?) -> Unit) {
        val ref = storageRef.child("profile_pictures/${uri.lastPathSegment}")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveImageUrlToFirestore(userId, downloadUrl.toString()) { success ->
                        callback(success, downloadUrl.toString())
                    }
                }
            }
            .addOnFailureListener {
                callback(false, null)
            }
    }

    // Save image URL to Firestore under the user's document
    private fun saveImageUrlToFirestore(userId: String, imageUrl: String, callback: (Boolean) -> Unit) {
        val userRef = db.collection("users").document(userId)
        userRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}
