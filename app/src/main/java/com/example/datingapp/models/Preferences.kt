package com.example.datingapp.models

data class Preferences (
    val yearPreferences: YearPreferences? = YearPreferences.ANY,
    val gender: Gender? = null,
    val majorPreferences: List<Jurusan>? = null
)
