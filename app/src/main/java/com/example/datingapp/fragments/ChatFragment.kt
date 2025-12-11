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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.R
import com.example.datingapp.adapters.ChatAdapter
import com.example.datingapp.models.Message
import com.example.datingapp.utils.FCMSender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var pinnedRecyclerView: RecyclerView
    private lateinit var pinnedAdapter: ChatAdapter
    private val pinnedMessages = mutableListOf<Message>()
    private lateinit var pinnedContainer: View

    private lateinit var inputEditText: EditText
    private lateinit var sendButton: com.google.android.material.floatingactionbutton.FloatingActionButton
    private var replyingTo: Message? = null
    private var editingMessage: Message? = null
    private lateinit var replyPreview: View
    private lateinit var replyMessagePreview: android.widget.TextView
    private lateinit var cancelReply: com.google.android.material.button.MaterialButton

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
        replyPreview = view.findViewById(R.id.reply_preview)
        replyMessagePreview = view.findViewById(R.id.reply_message_preview)
        cancelReply = view.findViewById(R.id.cancel_reply)
        pinnedRecyclerView = view.findViewById(R.id.pinned_messages_recycler)
        pinnedContainer = view.findViewById(R.id.pinned_messages_container)
        
        cancelReply.setOnClickListener { clearReplyEdit() }
        
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

        // Check if both users still like each other
        firestore.collection("swipes").document(myId).get()
            .addOnSuccessListener { myDoc ->
                @Suppress("UNCHECKED_CAST")
                val myLiked = myDoc.get("liked") as? List<String> ?: emptyList()
                
                if (!myLiked.contains(partnerId)) {
                    Toast.makeText(requireContext(), "Match no longer valid", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                    return@addOnSuccessListener
                }
                
                firestore.collection("swipes").document(partnerId).get()
                    .addOnSuccessListener { partnerDoc ->
                        @Suppress("UNCHECKED_CAST")
                        val partnerLiked = partnerDoc.get("liked") as? List<String> ?: emptyList()
                        
                        if (!partnerLiked.contains(myId)) {
                            Toast.makeText(requireContext(), "Match no longer valid", Toast.LENGTH_LONG).show()
                            parentFragmentManager.popBackStack()
                            return@addOnSuccessListener
                        }
                        
                        // Check if I blocked the partner
                        firestore.collection("blocks").document(myId).get()
                            .addOnSuccessListener { myBlockDoc ->
                                @Suppress("UNCHECKED_CAST")
                                val myBlockedUsers = myBlockDoc.get("blockedUsers") as? List<String> ?: emptyList()
                                
                                if (myBlockedUsers.contains(partnerId)) {
                                    Toast.makeText(requireContext(), "You have blocked this user", Toast.LENGTH_LONG).show()
                                    parentFragmentManager.popBackStack()
                                    return@addOnSuccessListener
                                }

                                // Check if partner blocked me
                                firestore.collection("blocks").document(partnerId).get()
                                    .addOnSuccessListener { partnerBlockDoc ->
                                        @Suppress("UNCHECKED_CAST")
                                        val partnerBlockedUsers = partnerBlockDoc.get("blockedUsers") as? List<String> ?: emptyList()
                                        
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
            }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(requireContext(), messages, myId,
            onDeleteMessage = { message, position -> deleteMessage(message, position) },
            onReplyMessage = { message -> startReply(message) },
            onEditMessage = { message -> startEdit(message) },
            onPinMessage = { message -> togglePinMessage(message) }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = chatAdapter
        
        pinnedAdapter = ChatAdapter(requireContext(), pinnedMessages, myId,
            onPinMessage = { message -> togglePinMessage(message) }
        )
        pinnedRecyclerView.layoutManager = LinearLayoutManager(context)
        pinnedRecyclerView.adapter = pinnedAdapter
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
                when {
                    editingMessage != null -> editMessage(text)
                    replyingTo != null -> sendReply(text)
                    else -> sendMessage(text)
                }
                inputEditText.text.clear()
                clearReplyEdit()
            }
        }
    }

    private fun sendMessage(text: String) {
        val chatsRef = firestore.collection("matches").document(matchId).collection("chats")
        val msg = mapOf(
            "senderId" to myId,
            "text" to text,
            "timestamp" to FieldValue.serverTimestamp(),
            "messageId" to chatsRef.document().id
        )

        chatsRef.add(msg).addOnSuccessListener {
            Log.d(TAG, "Message sent successfully")
            firestore.collection("matches").document(matchId).update(mapOf(
                "lastMessage" to text,
                "timestamp" to FieldValue.serverTimestamp()
            ))
            
            val partnerId = matchId.split("_").firstOrNull { it != myId }
            if (partnerId != null) {
                sendMessageNotification(partnerId, text)
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error sending message", e)
            Toast.makeText(context, "Failed to send message.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sendReply(text: String) {
        val replyMsg = replyingTo ?: return
        val chatsRef = firestore.collection("matches").document(matchId).collection("chats")
        val msg = mapOf(
            "senderId" to myId,
            "text" to text,
            "timestamp" to FieldValue.serverTimestamp(),
            "replyTo" to replyMsg.messageId,
            "replyText" to replyMsg.text,
            "messageId" to chatsRef.document().id
        )

        chatsRef.add(msg).addOnSuccessListener {
            firestore.collection("matches").document(matchId).update(mapOf(
                "lastMessage" to text,
                "timestamp" to FieldValue.serverTimestamp()
            ))
        }
    }
    
    private fun editMessage(newText: String) {
        val editMsg = editingMessage ?: return
        val chatsRef = firestore.collection("matches").document(matchId).collection("chats")
        
        chatsRef.whereEqualTo("senderId", editMsg.senderId)
            .whereEqualTo("text", editMsg.text)
            .whereEqualTo("timestamp", editMsg.timestamp)
            .get()
            .addOnSuccessListener { snapshots ->
                for (doc in snapshots.documents) {
                    doc.reference.update(mapOf(
                        "text" to newText,
                        "edited" to true
                    ))
                }
            }
    }
    
    private fun startReply(message: Message) {
        replyingTo = message
        replyPreview.visibility = View.VISIBLE
        replyMessagePreview.text = message.text
        inputEditText.requestFocus()
    }
    
    private fun startEdit(message: Message) {
        editingMessage = message
        inputEditText.setText(message.text)
        inputEditText.hint = "Editing message..."
    }
    
    private fun togglePinMessage(message: Message) {
        val chatsRef = firestore.collection("matches").document(matchId).collection("chats")
        
        chatsRef.whereEqualTo("senderId", message.senderId)
            .whereEqualTo("text", message.text)
            .whereEqualTo("timestamp", message.timestamp)
            .get()
            .addOnSuccessListener { snapshots ->
                for (doc in snapshots.documents) {
                    doc.reference.update("pinned", !message.pinned)
                }
            }
    }
    
    private fun clearReplyEdit() {
        replyingTo = null
        editingMessage = null
        replyPreview.visibility = View.GONE
        inputEditText.hint = "Type a message..."
        if (editingMessage != null) {
            inputEditText.text.clear()
        }
    }
    
    private fun sendMessageNotification(partnerId: String, messageText: String) {
        firestore.collection("users").document(myId).get()
            .addOnSuccessListener { myDoc ->
                val myName = myDoc.getString("name") ?: "Someone"
                Log.d(TAG, "Notification ready: $myName sent message to $partnerId")
                // TODO: Implement backend API to send FCM notification
                // For now, notifications are disabled (requires backend server)
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
                    pinnedMessages.clear()
                    
                    for (message in newMessages) {
                        if (message.pinned) {
                            pinnedMessages.add(message)
                        } else {
                            messages.add(message)
                        }
                    }
                    
                    chatAdapter.notifyDataSetChanged()
                    pinnedAdapter.notifyDataSetChanged()
                    
                    pinnedContainer.visibility = if (pinnedMessages.isNotEmpty()) View.VISIBLE else View.GONE
                    
                    if (messages.isNotEmpty()) {
                        recyclerView.scrollToPosition(messages.size - 1)
                    }
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
