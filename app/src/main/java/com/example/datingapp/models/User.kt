package com.example.datingapp.models

data class User(
    val uid: String = "",
    val name: String? = "",
    val username: String? = "",
    val age: String? = "",
    val email: String? = "",
    val gender: Gender? = Gender.M,
    val photoUrl: String? = "",
    val photoUrls: List<String> = listOf(), // Multiple photos
    val photoVerified: Boolean = false,
    val school: String? = "",
    val password: String? = "",
    val likes: MutableList<String> = mutableListOf(),
    val bio: String? = "",
    val interests: List<String> = listOf(), // Hobbies/interests tags
    val schoolyear: String? = "",
    val major: String? = null,
    val preference: Preferences = Preferences(),
    val lastSeen: Long = 0L, // Timestamp for last seen
    val isOnline: Boolean = false, // Online status
    val isTyping: Boolean = false // Typing indicator
)
