package com.example.datingapp.utils

import com.example.datingapp.models.Message
import com.example.datingapp.models.User
import com.example.datingapp.models.Jurusan
import com.example.datingapp.models.Gender
object DummyData {

    val users = mutableListOf(
        User("1", "Alice", "alice", "28", "alice@student.umn.ac.id", Gender.F, "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "University of Arts", "123456",mutableListOf(), "Hello I'm Alice","2023", Jurusan.TI),
        User("2", "Bob", "bob", "32", "bob@student.umn.ac.id", Gender.M, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Tech Institute", "123456",mutableListOf(), "Hello I like fishing","2024", Jurusan.TI),
        User("3", "Charlie", "charlie", "25", "charlie@student.umn.ac.id", Gender.M, "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "State University", "123456", mutableListOf(), "I like Anime Nice to meet you","2025", Jurusan.TI),
        User("4", "Diana", "diana", "30", "diana@student.umn.ac.id", Gender.F, "https://images.unsplash.com/photo-1580489944761-15a19d654956?q=80&w=1961&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Medical School", "123456",mutableListOf(), "Looking for a friedn","2022", Jurusan.TI),
        User("5", "Eva", "eva", "29", "eva@student.umn.ac.id", Gender.F, "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=1888&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Design School", "123456", mutableListOf(), "Creative soul.", "2021", Jurusan.TI),
        User("6", "Frank", "frank", "31", "frank@student.umn.ac.id", Gender.M, "https://images.unsplash.com/photo-1557862921-37829c790f19?q=80&w=2071&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Business School", "123456", mutableListOf(), "Entrepreneur in the making.", "2020", Jurusan.TI),
        User("7", "Grace", "grace", "26", "grace@student.umn.ac.id", Gender.F, "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Law School", "123456", mutableListOf(), "Seeking justice and a good coffee.", "2022", Jurusan.TI),
        User("8", "Harry", "harry", "33", "harry@student.umn.ac.id", Gender.M, "https://images.unsplash.com/photo-1564564321837-a57b7070ac4f?q=80&w=1974&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Engineering College", "123456", mutableListOf(), "Building things.", "2019", Jurusan.TI),
        User("9", "Ivy", "ivy", "24", "ivy@student.umn.ac.id", Gender.F, "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Science Academy", "123456", mutableListOf(), "Lover of science and nature.", "2023", Jurusan.TI),
        User("10", "Jack", "jack", "28", "jack@student.umn.ac.id", Gender.M, "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Music Conservatory", "123456", mutableListOf(), "Musician. Let's jam.", "2021", Jurusan.TI),
        User("11", "Kate", "kate", "27", "kate@student.umn.ac.id", Gender.F, "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=1964&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Culinary Institute", "123456", mutableListOf(), "Foodie for life.", "2022", Jurusan.TI),
        User("12", "Leo", "leo", "30", "leo@student.umn.ac.id", Gender.M, "https://images.unsplash.com/photo-1581382575275-972c7c3554bc?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Film School", "123456", mutableListOf(), "Director in the making.", "2020", Jurusan.TI)
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
