package com.ahmed.i221132

// This model holds the data about the user who SENT the request
data class Request(
    val uid: String = "",
    val username: String = "",
    val name: String = "",
    val profileImageBase64: String? = null
)
