package com.example.datingapp.models

data class Preferences (
    val yearPreferences: String? = "All",
    val gender: Gender? = null,
    val majorPreferences: List<String>? = null
)
