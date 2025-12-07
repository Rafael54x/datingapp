package com.example.datingapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.R
import com.example.datingapp.models.Message

class ChatAdapter(
    private val context: Context,
    private val messages: MutableList<Message>,
    private val loggedInUserId: String,
    private val onDeleteMessage: ((Message, Int) -> Unit)? = null
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

        // Long press hanya untuk pesan yang dikirim sendiri
        if (message.senderId == loggedInUserId) {
            holder.itemView.setOnLongClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setMessage("Delete this message for everyone?")
                    .setPositiveButton("Delete") { _, _ ->
                        val currentPosition = holder.bindingAdapterPosition
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            onDeleteMessage?.invoke(message, currentPosition)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }
    }

    // Return jumlah total pesan
    override fun getItemCount(): Int {
        return messages.size
    }

    // ViewHolder untuk menyimpan referensi view pesan
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)

        // Bind data message ke TextView
        fun bind(message: Message) {
            messageText.text = message.text
        }
    }
}
