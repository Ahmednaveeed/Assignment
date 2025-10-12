package com.ahmed.i221132

data class Post(
    // Properties must match the keys in the Firebase database exactly
    val postId: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val location: String = "",
    val timestamp: Long = 0L,
    val likes: Int = 0
)