package com.ahmed.i221132

data class Post(
    val postId: String = "",
    val userId: String = "",
    val imageBase64: String = "",
    val caption: String = "",
    val location: String = "",
    val timestamp: Long = 0L,
    val likes: Map<String, Boolean> = emptyMap()
)