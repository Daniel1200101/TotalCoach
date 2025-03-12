package com.example.totalcoach.models

import com.google.firebase.Timestamp

data class MediaItem(
    val mediaId: String = "",  // Unique ID for the media
    val trainerId: String = "", // Trainer's ID
    val traineeId: String = "", // Trainee's ID
    val type: String = "",
    val url: String = "",
    val caption: String = "",
    val description: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val uploadedBy: String = ""
)
