package com.ahmed.i221132

data class Conversation(
    // User info
    var uid: String = "",
    var username: String = "",
    var profileImageUrl: String = "",
    // Last message info
    var lastMessage: String = "",
    var timestamp: Long = 0L
)