package com.example.datingapp.models

data class Preferences (
    val yearPreferences: Range? = null, // >, <, == (lebih tua, lebih muda, sama dengan)
    val gender: Gender? = null,
    val major: MutableList<Jurusan> = mutableListOf()
)