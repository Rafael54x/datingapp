package com.example.datingapp.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.adapters.ChatAdapter
import com.example.datingapp.models.Message
import com.example.datingapp.utils.DummyChatManager

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messages: MutableList<Message>
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: Button

    private lateinit var partnerId: String
    private lateinit var partnerName: String
    private lateinit var partnerPhotoUrl: String

    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView

    // Let's assume the logged-in user has this ID
    private val loggedInUserId = "user1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            partnerId = it.getString(ARG_PARTNER_ID) ?: ""
            partnerName = it.getString(ARG_PARTNER_NAME) ?: ""
            partnerPhotoUrl = it.getString(ARG_PARTNER_PHOTO_URL) ?: ""
        }
        messages = DummyChatManager.getMessages(partnerId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.recycler_chat)
        inputEditText = view.findViewById(R.id.input)
        sendButton = view.findViewById(R.id.send)
        profileImage = view.findViewById(R.id.profile_image)
        profileName = view.findViewById(R.id.profile_name)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the header
        profileName.text = partnerName
        Glide.with(this)
            .load(partnerPhotoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(profileImage)

        chatAdapter = ChatAdapter(requireContext(), messages, loggedInUserId)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = chatAdapter
        recyclerView.scrollToPosition(messages.size - 1)

        sendButton.setOnClickListener {
            val messageText = inputEditText.text.toString()
            if (messageText.isNotEmpty()) {
                val newMessage = Message(messageText, loggedInUserId, System.currentTimeMillis())
                messages.add(newMessage)
                chatAdapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
                inputEditText.text.clear()

                // Simulate a reply after a short delay
                Handler(Looper.getMainLooper()).postDelayed({
                    val replyMessage = DummyChatManager.generateReply(partnerId)
                    messages.add(replyMessage)
                    chatAdapter.notifyItemInserted(messages.size - 1)
                    recyclerView.scrollToPosition(messages.size - 1)
                }, 1000)
            }
        }
    }

    companion object {
        private const val ARG_PARTNER_ID = "partnerId"
        private const val ARG_PARTNER_NAME = "partnerName"
        private const val ARG_PARTNER_PHOTO_URL = "partnerPhotoUrl"

        @JvmStatic
        fun newInstance(partnerId: String, partnerName: String, partnerPhotoUrl: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARTNER_ID, partnerId)
                    putString(ARG_PARTNER_NAME, partnerName)
                    putString(ARG_PARTNER_PHOTO_URL, partnerPhotoUrl)
                }
            }
    }
}
