// User.kt
package com.ahmed.i221132

// Updated to match your Firebase keys exactly
data class User(
    val uid: String = "",
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val website: String = "", // Added to resolve the previous error
    val profileImageBase64: String? = null
)


