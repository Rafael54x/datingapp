package com.example.datingapp.utils

import com.example.datingapp.models.Message
import com.example.datingapp.models.User

object DummyData {

    val users = mutableListOf(
        User("1", "Alice", "alice", "28", "alice@example.com", "Female", "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "University of Arts", "123456"),
        User("2", "Bob", "bob", "32", "bob@example.com", "Male", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Tech Institute", "123456"),
        User("3", "Charlie", "charlie", "25", "charlie@example.com", "Male", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "State University", "123456"),
        User("4", "Diana", "diana", "30", "diana@example.com", "Female", "https://images.unsplash.com/photo-1580489944761-15a19d654956?q=80&w=1961&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Medical School", "123456")
    )

    var loggedInUser: User? = users[0] // Alice is logged in by default for demo

    val matches = mutableListOf<Pair<String, String>>().apply {
        add("alice" to "bob")
    }

    private val chats = mutableMapOf<String, MutableList<Message>>().apply {
        val chatKey = getChatKey("alice", "bob")
        put(chatKey, mutableListOf(
            Message("Hi Bob!", "alice", System.currentTimeMillis()),
            Message("Hey Alice, how are you?", "bob", System.currentTimeMillis() + 1000)
        ))
    }

    fun getMatchesForLoggedIn(): List<User> {
        val loggedInUsername = loggedInUser?.username ?: return emptyList()
        return users.filter { user ->
            !matches.any { (u1, u2) ->
                (u1 == loggedInUsername && u2 == user.username) || (u2 == loggedInUsername && u1 == user.username)
            }
        }
    }

    fun like(likedUser: User) {
        val loggedInUsername = loggedInUser?.username ?: return
        val likedUsername = likedUser.username ?: return

        // Add a one-way like. If the other user also liked, it becomes a match.
        if (!matches.contains(loggedInUsername to likedUsername)) {
            matches.add(loggedInUsername to likedUsername)
        }
    }

    fun getChatWith(username: String): List<Message> {
        val loggedInUsername = loggedInUser?.username ?: return emptyList()
        val chatKey = getChatKey(loggedInUsername, username)
        return chats.getOrPut(chatKey) { mutableListOf() }
    }

    fun sendMessage(toUsername: String, message: Message) {
        val loggedInUsername = loggedInUser?.username ?: return
        val chatKey = getChatKey(loggedInUsername, toUsername)
        chats.getOrPut(chatKey) { mutableListOf() }.add(message)
    }

    private fun getChatKey(user1: String, user2: String): String {
        return if (user1 < user2) "$user1-$user2" else "$user2-$user1"
    }
}
