package com.example.datingapp.utils

import com.example.datingapp.models.Message

object DummyChatManager {

    private val chatHistories = mutableMapOf<String, MutableList<Message>>()

    fun getMessages(partnerId: String): MutableList<Message> {
        return chatHistories.getOrPut(partnerId) {
            createDummyConversation(partnerId)
        }
    }

    fun generateReply(partnerId: String): Message {
        val replies = listOf(
            "That's interesting! Tell me more.",
            "I agree!",
            "LOL! That's hilarious.",
            "What do you think?",
            "I'm not so sure about that."
        )
        return Message(replies.random(), partnerId, System.currentTimeMillis())
    }

    private fun createDummyConversation(partnerId: String): MutableList<Message> {
        val loggedInUserId = "user1"
        return mutableListOf(
            Message("Hey, how are you?", partnerId, System.currentTimeMillis()),
            Message("I'm good, thanks! How about you?", loggedInUserId, System.currentTimeMillis() + 1000),
            Message("Doing great! Wanna grab a coffee sometime?", partnerId, System.currentTimeMillis() + 2000),
            Message("Sure, I'd love that!", loggedInUserId, System.currentTimeMillis() + 3000)
        )
    }
}
