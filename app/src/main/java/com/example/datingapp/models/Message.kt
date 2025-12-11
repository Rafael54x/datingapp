package com.example.datingapp.models

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val replyTo: String? = null,
    val replyText: String? = null,
    val edited: Boolean = false,
    val messageId: String = "",
    val pinned: Boolean = false
)
