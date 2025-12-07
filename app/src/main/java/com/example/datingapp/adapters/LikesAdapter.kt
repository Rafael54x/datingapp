package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.models.User

class LikesAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit,
    private val onLikeBackClick: (User) -> Unit
) : RecyclerView.Adapter<LikesAdapter.LikesViewHolder>() {

    inner class LikesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userPhoto: ImageView = view.findViewById(R.id.user_photo)
        val userName: TextView = view.findViewById(R.id.user_name)
        val userAge: TextView = view.findViewById(R.id.user_age)
        val verifiedBadge: ImageView = view.findViewById(R.id.verified_badge)
        val likeBackButton: ImageView = view.findViewById(R.id.like_back_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_like_user, parent, false)
        return LikesViewHolder(view)
    }

    override fun onBindViewHolder(holder: LikesViewHolder, position: Int) {
        val user = users[position]
        
        holder.userName.text = user.name
        
        if (user.photoVerified) {
            holder.verifiedBadge.visibility = View.VISIBLE
        } else {
            holder.verifiedBadge.visibility = View.GONE
        }
        
        Glide.with(holder.itemView.context)
            .load(user.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(holder.userPhoto)
        
        holder.itemView.setOnClickListener { onUserClick(user) }
        holder.likeBackButton.setOnClickListener { onLikeBackClick(user) }
    }

    override fun getItemCount() = users.size
}
