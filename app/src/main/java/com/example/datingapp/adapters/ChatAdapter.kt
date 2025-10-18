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
    private val loggedInUserId: String
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.sender == loggedInUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_SENT) {
            R.layout.item_chat_sent
        } else {
            R.layout.item_chat_received
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Chat")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Yes") { _, _ ->
                    val currentPosition = holder.bindingAdapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        messages.removeAt(currentPosition)
                        notifyItemRemoved(currentPosition)
                        notifyItemRangeChanged(currentPosition, messages.size)
                    }
                }
                .setNegativeButton("No", null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)

        fun bind(message: Message) {
            messageText.text = message.text
        }
    }
}
