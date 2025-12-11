package com.example.datingapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.R
import com.example.datingapp.models.Message
import kotlin.math.abs

class ChatAdapter(
    private val context: Context,
    private val messages: MutableList<Message>,
    private val loggedInUserId: String,
    private val onDeleteMessage: ((Message, Int) -> Unit)? = null,
    private val onReplyMessage: ((Message) -> Unit)? = null,
    private val onEditMessage: ((Message) -> Unit)? = null,
    private val onPinMessage: ((Message) -> Unit)? = null
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    // Konstanta untuk membedakan tipe view (pesan dikirim vs diterima)
    private companion object {
        const val VIEW_TYPE_SENT = 1 // Pesan yang dikirim oleh user
        const val VIEW_TYPE_RECEIVED = 2 // Pesan yang diterima dari orang lain
    }

    // Tentukan tipe view berdasarkan siapa pengirim pesan
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        // Jika pengirim adalah user yang login, gunakan layout sent
        // Jika bukan, gunakan layout received
        return if (message.senderId == loggedInUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    // Buat ViewHolder baru dengan layout yang sesuai
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // Pilih layout berdasarkan tipe view
        val layoutId = if (viewType == VIEW_TYPE_SENT) {
            R.layout.item_chat_sent // Layout untuk pesan terkirim (posisi kanan)
        } else {
            R.layout.item_chat_received // Layout untuk pesan diterima (posisi kiri)
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    // Bind data pesan ke ViewHolder
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)

        // Setup long press and hover effects
        holder.setupMessageActions(message)
    }

    // Return jumlah total pesan
    override fun getItemCount(): Int {
        return messages.size
    }

    // ViewHolder untuk menyimpan referensi view pesan
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val replyContainer: View? = itemView.findViewById(R.id.reply_container)
        private val replyText: TextView? = itemView.findViewById(R.id.reply_text)
        private val editedIndicator: TextView? = itemView.findViewById(R.id.edited_indicator)
        private val pinIndicator: ImageView? = itemView.findViewById(R.id.pin_indicator)
        private val replyIcon: ImageView? = itemView.findViewById(R.id.reply_icon)
        private val messageContainer: View = itemView.findViewById(R.id.message_container)
        
        private var startX = 0f
        private var isSwipeStarted = false
        private var isLongPressing = false

        fun bind(message: Message) {
            messageText.text = message.text
            
            // Show reply if exists
            if (!message.replyText.isNullOrEmpty()) {
                replyContainer?.visibility = View.VISIBLE
                replyText?.text = message.replyText
            } else {
                replyContainer?.visibility = View.GONE
            }
            
            // Show edited indicator
            editedIndicator?.visibility = if (message.edited) View.VISIBLE else View.GONE
            
            // Show pin indicator
            pinIndicator?.visibility = if (message.pinned) View.VISIBLE else View.GONE
            
            // Setup swipe to reply
            setupSwipeToReply(message)
        }
        
        private fun setupSwipeToReply(message: Message) {
            itemView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        isSwipeStarted = false
                        isLongPressing = false
                        messageContainer.animate().scaleX(0.98f).scaleY(0.98f).setDuration(100).start()
                        false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.x - startX
                        
                        if (deltaX > 20 && !isSwipeStarted && !isLongPressing) {
                            isSwipeStarted = true
                        }
                        
                        if (isSwipeStarted && !isLongPressing) {
                            val maxSwipe = 120f
                            val clampedDelta = deltaX.coerceIn(0f, maxSwipe)
                            
                            messageContainer.translationX = clampedDelta
                            
                            val progress = clampedDelta / maxSwipe
                            if (progress > 0.2f) {
                                replyIcon?.visibility = View.VISIBLE
                                replyIcon?.alpha = (progress * 0.8f).coerceAtMost(0.8f)
                            } else {
                                replyIcon?.visibility = View.GONE
                            }
                        }
                        isSwipeStarted
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        messageContainer.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                        
                        if (isSwipeStarted && !isLongPressing) {
                            val deltaX = messageContainer.translationX
                            val threshold = 60f
                            
                            if (deltaX > threshold) {
                                onReplyMessage?.invoke(message)
                            }
                        }
                        
                        messageContainer.animate().translationX(0f).setDuration(200).start()
                        replyIcon?.animate()?.alpha(0f)?.setDuration(200)?.withEndAction {
                            replyIcon?.visibility = View.GONE
                        }?.start()
                        
                        isSwipeStarted = false
                        isLongPressing = false
                        true
                    }
                    else -> false
                }
            }
        }
        
        fun setupMessageActions(message: Message) {
            messageContainer.setOnLongClickListener {
                isLongPressing = true
                messageContainer.animate().scaleX(0.95f).scaleY(0.95f).setDuration(150).start()
                
                val pinOption = if (message.pinned) "Unpin" else "Pin"
                val options = if (message.senderId == loggedInUserId) {
                    arrayOf("Reply", "Edit", pinOption, "Delete")
                } else {
                    arrayOf("Reply", pinOption)
                }
                
                AlertDialog.Builder(context)
                    .setTitle("Message Options")
                    .setItems(options) { _, which ->
                        when (options[which]) {
                            "Reply" -> onReplyMessage?.invoke(message)
                            "Edit" -> onEditMessage?.invoke(message)
                            "Pin", "Unpin" -> onPinMessage?.invoke(message)
                            "Delete" -> {
                                AlertDialog.Builder(context)
                                    .setTitle("Delete Message")
                                    .setMessage("Delete this message for everyone?")
                                    .setPositiveButton("Delete") { _, _ ->
                                        val currentPosition = bindingAdapterPosition
                                        if (currentPosition != RecyclerView.NO_POSITION) {
                                            onDeleteMessage?.invoke(message, currentPosition)
                                        }
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                            }
                        }
                    }
                    .setOnDismissListener {
                        messageContainer.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    }
                    .show()
                true
            }
        }
    }
}
