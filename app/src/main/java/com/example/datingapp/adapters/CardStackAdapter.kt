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
import de.hdodenhof.circleimageview.CircleImageView

class CardStackAdapter(private val users: List<User>) :
    RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.findViewById(R.id.item_image)
        val name: TextView = view.findViewById(R.id.item_name)
        val age: TextView = view.findViewById(R.id.item_age)
        val major: TextView = view.findViewById(R.id.item_major)
        val schoolyear: TextView = view.findViewById(R.id.item_schoolyear)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_user_card, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.name.text = user.name
        holder.age.text = user.age
        holder.major.text = user.major?.displayName
        holder.schoolyear.text = user.schoolyear

        Glide.with(holder.itemView.context)
            .load(user.photoUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.photo)
    }

    override fun getItemCount(): Int {
        return users.size
    }
}
