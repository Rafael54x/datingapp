package com.example.datingapp.models

data class Match(
    var matchId: String = "",
    val user1: String = "",
    val user2: String = "",
    val users: List<String> = listOf(),
    val lastMessage: String = "",
    val timestamp: Long = 0L
)
