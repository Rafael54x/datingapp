package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.models.User
import de.hdodenhof.circleimageview.CircleImageView

class MatchAdapter(private val matches: List<User>, private val onMatchClicked: (User) -> Unit) :
    RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        holder.bind(match)
    }

    override fun getItemCount(): Int = matches.size

    inner class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val matchName: TextView = itemView.findViewById(R.id.match_name)
        private val matchPhoto: CircleImageView = itemView.findViewById(R.id.match_photo)

        fun bind(user: User) {
            matchName.text = user.name
            Glide.with(itemView.context)
                .load(user.photoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(matchPhoto)
            itemView.setOnClickListener { onMatchClicked(user) }
        }
    }
}
