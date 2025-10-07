package com.ahmed.i221132

data class Post(
    val username: String,
    val location: String,
    val profileImageRes: Int,  // Drawable resource ID
    val postImageRes: Int,     // Drawable resource ID
    val likedByText: String,
    val caption: String
)