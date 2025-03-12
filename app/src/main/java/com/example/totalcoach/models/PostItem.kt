package com.example.totalcoach.models

import com.google.firebase.Timestamp

data class PostItem(
    val postId: String = "",           // Unique identifier for the post
    val userId: String = "",           // User who created the post
    val caption: String = "",          // Caption of the post
    val timestamp: Timestamp = Timestamp.now(), // Timestamp of the post
    val mediaItems: List<MediaItem> = listOf()  // List of media items (images, videos)
)

