package com.ahmed.i221132

data class Conversation(
    // User info
    val uid: String = "",
    val username: String = "",
    val profileImageBase64: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L
)