package com.ahmed.i221132

data class DMMessage(
    val username: String,
    val lastMessage: String,
    val timestamp: String,
    val profileImageRes: Int  // Drawable resource ID
)