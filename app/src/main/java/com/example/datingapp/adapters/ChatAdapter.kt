package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.R
import com.example.datingapp.models.Message

class ChatAdapter(private val messages: List<Message>, private val meUsername: String) :
    RecyclerView.Adapter<ChatAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textLeft = view.findViewById<TextView>(R.id.txt_left)
        val textRight = view.findViewById<TextView>(R.id.txt_right)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = messages[position]
        if (m.sender == meUsername) {
            holder.textRight.visibility = View.VISIBLE
            holder.textLeft.visibility = View.GONE
            holder.textRight.text = m.text
        } else {
            holder.textLeft.visibility = View.VISIBLE
            holder.textRight.visibility = View.GONE
            holder.textLeft.text = m.text
        }
    }

    override fun getItemCount(): Int = messages.size
}
