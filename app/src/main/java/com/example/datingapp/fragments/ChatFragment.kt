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

    // UI Components
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messages: MutableList<Message>
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: Button

    // Data partner chat
    private lateinit var partnerId: String
    private lateinit var partnerName: String
    private lateinit var partnerPhotoUrl: String

    // Header components
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView

    // ID user yang sedang login (hardcoded untuk demo)
    private val loggedInUserId = "user1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ambil data partner dari arguments
        arguments?.let {
            partnerId = it.getString(ARG_PARTNER_ID) ?: ""
            partnerName = it.getString(ARG_PARTNER_NAME) ?: ""
            partnerPhotoUrl = it.getString(ARG_PARTNER_PHOTO_URL) ?: ""
        }

        // Load riwayat chat dengan partner dari DummyChatManager
        messages = DummyChatManager.getMessages(partnerId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Inisialisasi views
        recyclerView = view.findViewById(R.id.recycler_chat)
        inputEditText = view.findViewById(R.id.input)
        sendButton = view.findViewById(R.id.send)
        profileImage = view.findViewById(R.id.profile_image)
        profileName = view.findViewById(R.id.profile_name)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup header chat - tampilkan nama dan foto partner
        profileName.text = partnerName
        Glide.with(this)
            .load(partnerPhotoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(profileImage)

        // Setup RecyclerView untuk menampilkan pesan
        chatAdapter = ChatAdapter(requireContext(), messages, loggedInUserId)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = chatAdapter

        // Scroll ke pesan terakhir
        recyclerView.scrollToPosition(messages.size - 1)

        // Setup tombol send
        sendButton.setOnClickListener {
            // Ambil text dari input
            val messageText = inputEditText.text.toString()

            // Validasi: pastikan pesan tidak kosong
            if (messageText.isNotEmpty()) {
                // Buat object Message baru
                val newMessage = Message(
                    messageText,
                    loggedInUserId,
                    System.currentTimeMillis()
                )

                // Tambahkan pesan ke list
                messages.add(newMessage)

                // Notify adapter bahwa ada item baru
                chatAdapter.notifyItemInserted(messages.size - 1)

                // Scroll ke pesan terbaru
                recyclerView.scrollToPosition(messages.size - 1)

                // Kosongkan input field
                inputEditText.text.clear()

                // Simulasi balasan otomatis dari partner setelah 1 detik
                Handler(Looper.getMainLooper()).postDelayed({
                    // Generate balasan random
                    val replyMessage = DummyChatManager.generateReply(partnerId)

                    // Tambahkan balasan ke list
                    messages.add(replyMessage)

                    // Notify adapter
                    chatAdapter.notifyItemInserted(messages.size - 1)

                    // Scroll ke balasan terbaru
                    recyclerView.scrollToPosition(messages.size - 1)
                }, 1000) // Delay 1 detik
            }
        }
    }

    companion object {
        // Keys untuk arguments
        private const val ARG_PARTNER_ID = "partnerId"
        private const val ARG_PARTNER_NAME = "partnerName"
        private const val ARG_PARTNER_PHOTO_URL = "partnerPhotoUrl"

        // Factory method untuk membuat instance ChatFragment dengan arguments
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