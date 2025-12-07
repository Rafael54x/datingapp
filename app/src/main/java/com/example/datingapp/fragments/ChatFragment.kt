package com.example.datingapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.R
import com.example.datingapp.adapters.ChatAdapter
import com.example.datingapp.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    private lateinit var inputEditText: EditText
    private lateinit var sendButton: com.google.android.material.floatingactionbutton.FloatingActionButton

    private lateinit var matchId: String
    private lateinit var myId: String

    // Firebase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var chatListener: ListenerRegistration? = null

    private val TAG = "ChatFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            matchId = it.getString(ARG_MATCH_ID) ?: ""
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        myId = auth.currentUser?.uid ?: ""

        if (matchId.isEmpty() || myId.isEmpty()) {
            Log.e(TAG, "Match ID or User ID is missing.")
            parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.recycler_chat)
        inputEditText = view.findViewById(R.id.input)
        sendButton = view.findViewById(R.id.send)
        
        // Setup header
        setupHeader(view)
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadPartnerInfo()
        checkIfBlocked()
    }
    
    private fun setupHeader(view: View) {
        view.findViewById<android.widget.ImageButton>(R.id.back_button)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        view.findViewById<android.view.View>(R.id.partner_photo)?.setOnClickListener {
            val partnerId = matchId.split("_").firstOrNull { it != myId }
            if (partnerId != null) {
                val profileView = ProfileViewFragment.newInstance(partnerId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileView)
                    .addToBackStack(null)
                    .commit()
            }
        }
        
        view.findViewById<android.widget.TextView>(R.id.partner_name)?.setOnClickListener {
            val partnerId = matchId.split("_").firstOrNull { it != myId }
            if (partnerId != null) {
                val profileView = ProfileViewFragment.newInstance(partnerId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileView)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
    
    private fun loadPartnerInfo() {
        val partnerId = matchId.split("_").firstOrNull { it != myId } ?: return
        
        firestore.collection("users").document(partnerId).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                
                val name = doc.getString("name") ?: "User"
                val photoUrl = doc.getString("photoUrl")
                
                view?.findViewById<android.widget.TextView>(R.id.partner_name)?.text = name
                
                photoUrl?.let {
                    view?.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.partner_photo)?.let { imageView ->
                        com.bumptech.glide.Glide.with(this)
                            .load(it)
                            .placeholder(R.drawable.ic_profile)
                            .into(imageView)
                    }
                }
            }
    }

    private fun checkIfBlocked() {
        val partnerId = matchId.split("_").firstOrNull { it != myId }
        if (partnerId == null) {
            parentFragmentManager.popBackStack()
            return
        }

        // Check if I blocked the partner
        firestore.collection("blocks").document(myId).get()
            .addOnSuccessListener { myDoc ->
                @Suppress("UNCHECKED_CAST")
                val myBlockedUsers = myDoc.get("blockedUsers") as? List<String> ?: emptyList()
                
                if (myBlockedUsers.contains(partnerId)) {
                    Toast.makeText(requireContext(), "You have blocked this user", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                    return@addOnSuccessListener
                }

                // Check if partner blocked me
                firestore.collection("blocks").document(partnerId).get()
                    .addOnSuccessListener { partnerDoc ->
                        @Suppress("UNCHECKED_CAST")
                        val partnerBlockedUsers = partnerDoc.get("blockedUsers") as? List<String> ?: emptyList()
                        
                        if (partnerBlockedUsers.contains(myId)) {
                            Toast.makeText(requireContext(), "You cannot chat with this user", Toast.LENGTH_LONG).show()
                            parentFragmentManager.popBackStack()
                        } else {
                            setupSendButton()
                            startChatListener()
                        }
                    }
            }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(requireContext(), messages, myId) { message, position ->
            deleteMessage(message, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = chatAdapter
    }

    private fun deleteMessage(message: Message, position: Int) {
        val chatsRef = firestore.collection("matches").document(matchId).collection("chats")
        
        chatsRef.whereEqualTo("senderId", message.senderId)
            .whereEqualTo("text", message.text)
            .whereEqualTo("timestamp", message.timestamp)
            .get()
            .addOnSuccessListener { snapshots ->
                for (doc in snapshots.documents) {
                    doc.reference.delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val text = inputEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                inputEditText.text.clear()
            }
        }
    }

    private fun sendMessage(text: String) {
        val chatsRef = firestore.collection("matches").document(matchId).collection("chats")
        val msg = mapOf(
            "senderId" to myId,
            "text" to text,
            "timestamp" to FieldValue.serverTimestamp()
        )

        chatsRef.add(msg).addOnSuccessListener {
            Log.d(TAG, "Message sent successfully")
            // Also update the last message in the match document
            firestore.collection("matches").document(matchId).update(mapOf(
                "lastMessage" to text,
                "timestamp" to FieldValue.serverTimestamp()
            ))
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error sending message", e)
            Toast.makeText(context, "Failed to send message.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startChatListener() {
        val chatsRef = firestore.collection("matches").document(matchId).collection("chats")
        chatListener = chatsRef.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val newMessages = snapshots.toObjects(Message::class.java)
                    messages.clear()
                    messages.addAll(newMessages)
                    chatAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Stop listening to prevent memory leaks
        chatListener?.remove()
    }

    companion object {
        private const val ARG_MATCH_ID = "matchId"

        @JvmStatic
        fun newInstance(matchId: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MATCH_ID, matchId)
                }
            }
    }
}
