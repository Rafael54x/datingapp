package com.example.datingapp.utils

import com.example.datingapp.models.Message

// Object singleton untuk mengelola chat dummy
object DummyChatManager {

    // Map untuk menyimpan riwayat chat per partner
    // Key: partnerId, Value: List of messages
    private val chatHistories = mutableMapOf<String, MutableList<Message>>()

    // Ambil riwayat chat dengan partner tertentu
    fun getMessages(partnerId: String): MutableList<Message> {
        // Jika belum ada chat history, buat yang baru dengan percakapan dummy
        return chatHistories.getOrPut(partnerId) {
            createDummyConversation(partnerId)
        }
    }

    // Generate balasan otomatis (random)
    fun generateReply(partnerId: String): Message {
        // List balasan random yang bisa dipilih
        val replies = listOf(
            "That's interesting! Tell me more.",
            "I agree!",
            "LOL! That's hilarious.",
            "What do you think?",
            "I'm not so sure about that."
        )

        // Pilih balasan secara random dan buat Message object
        return Message(
            replies.random(), // Text random dari list
            partnerId,        // Sender adalah partner
            System.currentTimeMillis() // Timestamp sekarang
        )
    }

    // Buat percakapan dummy awal dengan partner
    private fun createDummyConversation(partnerId: String): MutableList<Message> {
        // ID user yang login (hardcoded untuk demo)
        val loggedInUserId = "user1"

        // Buat list pesan dummy
        return mutableListOf(
            // Partner memulai percakapan
            Message(
                "Hey, how are you?",
                partnerId,
                System.currentTimeMillis()
            ),
            // User membalas
            Message(
                "I'm good, thanks! How about you?",
                loggedInUserId,
                System.currentTimeMillis() + 1000
            ),
            // Partner membalas lagi
            Message(
                "Doing great! Wanna grab a coffee sometime?",
                partnerId,
                System.currentTimeMillis() + 2000
            ),
            // User setuju
            Message(
                "Sure, I'd love that!",
                loggedInUserId,
                System.currentTimeMillis() + 3000
            )
        )
    }
}