package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.models.User

class MyLikesAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit,
    private val onUnlikeClick: (User) -> Unit
) : RecyclerView.Adapter<MyLikesAdapter.MyLikesViewHolder>() {

    inner class MyLikesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userPhoto: ImageView = view.findViewById(R.id.user_photo)
        val userName: TextView = view.findViewById(R.id.user_name)
        val unlikeButton: ImageButton = view.findViewById(R.id.unlike_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyLikesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_like_user, parent, false)
        return MyLikesViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyLikesViewHolder, position: Int) {
        val user = users[position]
        
        holder.userName.text = user.name
        
        Glide.with(holder.itemView.context)
            .load(user.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(holder.userPhoto)
        
        holder.itemView.setOnClickListener { onUserClick(user) }
        holder.unlikeButton.setOnClickListener { onUnlikeClick(user) }
    }

    override fun getItemCount() = users.size
}
