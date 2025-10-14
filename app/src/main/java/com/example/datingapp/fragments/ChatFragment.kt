package com.example.datingapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.R
import com.example.datingapp.adapters.ChatAdapter
import com.example.datingapp.models.Message
import com.example.datingapp.utils.DummyData
import com.example.datingapp.utils.SharedPrefManager

class ChatFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var input: EditText
    private lateinit var send: Button
    private var messages = mutableListOf<Message>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.recycler_chat)
        input = view.findViewById(R.id.input)
        send = view.findViewById(R.id.send)

        val peer = arguments?.getString(ARG_PEER)
        if (peer == null) {
            // Handle the case where peer is not provided, maybe show an error or a default chat
            return
        }

        val loggedInUser = SharedPrefManager(requireContext()).getUser()
        if (loggedInUser == null) {
            // Handle the case where there is no logged-in user
            return
        }

        messages = DummyData.getChatWith(peer).toMutableList()

        adapter = ChatAdapter(messages, loggedInUser.username ?: "")
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        send.setOnClickListener {
            val txt = input.text.toString().trim()
            if (txt.isNotEmpty()) {
                val msg = Message(loggedInUser.username ?: "", txt, System.currentTimeMillis())
                DummyData.sendMessage(peer, msg)
                messages.add(msg)
                adapter.notifyItemInserted(messages.size - 1)
                recycler.scrollToPosition(messages.size - 1)
                input.setText("")
            }
        }
    }

    companion object {
        private const val ARG_PEER = "peer"

        @JvmStatic
        fun newInstance(peer: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PEER, peer)
                }
            }
    }
}
