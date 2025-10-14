package com.ahmed.i221132

// Represents a single message object stored in Firebase
data class ChatMessage(
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val imageUrl: String? = null // For when you add image sharing later
)