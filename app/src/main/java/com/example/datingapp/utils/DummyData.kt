package com.example.datingapp.utils

import com.example.datingapp.models.Message
import com.example.datingapp.models.User
import com.example.datingapp.models.Jurusan
import com.example.datingapp.models.Gender
import com.google.firebase.Timestamp
import java.util.Date

// Object singleton untuk menyimpan data dummy aplikasi
object DummyData {

    val users = mutableListOf<User>()

    // User yang sedang login (default: Alice untuk demo)
    var loggedInUser: User? = users[0]

    // List pasangan yang sudah match
    // Berisi pair username yang saling match
    val matches = mutableListOf<Pair<String, String>>().apply {
        add("alice" to "bob") // Alice dan Bob sudah match
    }

    // Map untuk menyimpan riwayat chat antar user
    // Key: "username1-username2" (diurutkan alfabetis)
    // Value: List of Message
    private val chats = mutableMapOf<String, MutableList<Message>>().apply {
        // Inisialisasi chat dummy antara Alice dan Bob
        val chatKey = getChatKey("alice", "bob")
        put(chatKey, mutableListOf(
            Message(senderId = "alice", text = "Hi Bob!", timestamp = Timestamp(Date())),
            Message(senderId = "bob", text = "Hey Alice, how are you?", timestamp = Timestamp(Date(System.currentTimeMillis() + 1000)))
        ))
    }

    // Ambil list user yang belum match dengan user yang login
    fun getMatchesForLoggedIn(): List<User> {
        val loggedInUsername = loggedInUser?.username ?: return emptyList()

        // Filter: tampilkan user yang belum ada di list matches
        return users.filter { user ->
            !matches.any { (u1, u2) ->
                (u1 == loggedInUsername && u2 == user.username) ||
                        (u2 == loggedInUsername && u1 == user.username)
            }
        }
    }

    // Tambahkan like ke user lain
    fun like(likedUser: User) {
        val loggedInUsername = loggedInUser?.username ?: return
        val likedUsername = likedUser.username ?: return

        // Tambahkan ke list matches jika belum ada
        // Ini adalah like satu arah. Jika user lain juga like, baru jadi match
        if (!matches.contains(loggedInUsername to likedUsername)) {
            matches.add(loggedInUsername to likedUsername)
        }
    }

    // Ambil riwayat chat dengan username tertentu
    fun getChatWith(username: String): List<Message> {
        val loggedInUsername = loggedInUser?.username ?: return emptyList()

        // Generate key untuk chat
        val chatKey = getChatKey(loggedInUsername, username)

        // Return chat history, atau list kosong jika belum ada
        return chats.getOrPut(chatKey) { mutableListOf() }
    }

    // Kirim pesan ke user lain
    fun sendMessage(toUsername: String, message: Message) {
        val loggedInUsername = loggedInUser?.username ?: return

        // Generate key untuk chat
        val chatKey = getChatKey(loggedInUsername, toUsername)

        // Tambahkan pesan ke history
        chats.getOrPut(chatKey) { mutableListOf() }.add(message)
    }

    // Generate key unik untuk chat antara dua user
    // Key selalu dalam format yang konsisten (alfabetis) agar chat bisa ditemukan
    // dari kedua sisi
    private fun getChatKey(user1: String, user2: String): String {
        // Sort alfabetis untuk konsistensi
        // Contoh: "alice-bob" sama dengan "bob-alice"
        return if (user1 < user2) "$user1-$user2" else "$user2-$user1"
    }
}