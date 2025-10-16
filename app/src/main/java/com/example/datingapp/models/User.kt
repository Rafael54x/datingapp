package com.example.datingapp.models

import java.util.Date
data class User(
    val uid: String = "",
    val name: String? = "",
    val username: String? = "",
    val age: String? = "",
    val email: String? = "",
    val gender: String? = "",
    val photoUrl: String? = "",
    val school: String? = "",
    val password: String? = "",
    val likes: MutableList<String> = mutableListOf(),
    val bio: String? = "",
    val schoolyear: String? = "",
    val major: Jurusan? = null
)
