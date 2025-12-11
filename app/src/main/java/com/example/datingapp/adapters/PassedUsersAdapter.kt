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

class PassedUsersAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit,
    private val onUnpassClick: (User) -> Unit
) : RecyclerView.Adapter<PassedUsersAdapter.PassedUsersViewHolder>() {

    inner class PassedUsersViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userPhoto: ImageView = view.findViewById(R.id.user_photo)
        val userName: TextView = view.findViewById(R.id.user_name)
        val unpassButton: ImageButton = view.findViewById(R.id.unpass_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PassedUsersViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_passed_user, parent, false)
        return PassedUsersViewHolder(view)
    }

    override fun onBindViewHolder(holder: PassedUsersViewHolder, position: Int) {
        val user = users[position]
        
        holder.userName.text = user.name
        
        Glide.with(holder.itemView.context)
            .load(user.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(holder.userPhoto)
        
        holder.itemView.setOnClickListener { onUserClick(user) }
        holder.unpassButton.setOnClickListener { onUnpassClick(user) }
    }

    override fun getItemCount() = users.size
}