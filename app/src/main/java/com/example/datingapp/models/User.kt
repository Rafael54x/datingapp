package com.example.datingapp.models

data class User(
    val uid: String = "",
    val name: String? = "",
    val username: String? = "",
    val age: String? = "",
    val email: String? = "",
    val gender: Gender? = Gender.M,
    val photoUrl: String? = "",
    val school: String? = "",
    val password: String? = "",
    val likes: MutableList<String> = mutableListOf(),
    val bio: String? = "",
    val schoolyear: String? = "",
    val major: Jurusan? = null,
    val preference: Preferences = Preferences()
)
