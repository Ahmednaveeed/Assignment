package com.ahmed.i221132

data class ChatMessage(
    var messageId: String = "", // 🔑 NEW: To store the unique key for editing/deleting
    val senderId: String = "",
    val receiverId: String = "",
    val text: String? = null, // Can be null if it's an image message
    val imageBase64: String? = null, // 🔑 NEW: For the encoded image
    val timestamp: Long = 0L
)