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

class MatchAdapter(
    private val matches: List<User>, // List user yang match
    private val onItemClicked: (User) -> Unit, // Callback saat item diklik (buka chat)
    private val onPhotoClicked: (User) -> Unit // Callback saat foto diklik (lihat profile)
) :
    RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    // Buat ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    // Bind data user ke ViewHolder
    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        holder.bind(match)
    }

    // Return jumlah total matches
    override fun getItemCount(): Int = matches.size

    // ViewHolder untuk item match
    inner class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val matchName: TextView = itemView.findViewById(R.id.match_name)
        private val matchPhoto: CircleImageView = itemView.findViewById(R.id.match_photo)

        // Bind data user dan set click listeners
        fun bind(user: User) {
            // Set nama user
            matchName.text = user.name

            // Load foto menggunakan Glide
            Glide.with(itemView.context)
                .load(user.photoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(matchPhoto)

            // Set listener untuk seluruh item - navigasi ke chat
            itemView.setOnClickListener { onItemClicked(user) }

            // Set listener untuk foto - navigasi ke profile view
            matchPhoto.setOnClickListener { onPhotoClicked(user) }
        }
    }
}