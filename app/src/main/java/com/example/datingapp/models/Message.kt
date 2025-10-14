package com.example.datingapp.models

data class Message(
    val text: String? = "",
    val sender: String? = "",
    val timestamp: Long? = 0
)
