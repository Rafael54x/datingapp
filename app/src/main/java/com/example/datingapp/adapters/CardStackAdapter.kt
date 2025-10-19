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

class CardStackAdapter(private val users: List<User>) :
    RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    // ViewHolder untuk menyimpan referensi views dalam setiap card
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.findViewById(R.id.item_image)
        val name: TextView = view.findViewById(R.id.item_name)
        val age: TextView = view.findViewById(R.id.item_age)
        val major: TextView = view.findViewById(R.id.item_major)
        val schoolyear: TextView = view.findViewById(R.id.item_schoolyear)
    }

    // Dipanggil saat RecyclerView membutuhkan ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout untuk single card item
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_user_card, parent, false))
    }

    // Dipanggil untuk menampilkan data pada posisi tertentu
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Ambil data user berdasarkan posisi
        val user = users[position]

        // Set data ke views
        holder.name.text = user.name
        holder.age.text = user.age
        holder.major.text = user.major?.displayName
        holder.schoolyear.text = user.schoolyear

        // Load foto menggunakan Glide library
        Glide.with(holder.itemView.context)
            .load(user.photoUrl) // URL foto
            .placeholder(android.R.drawable.ic_menu_gallery) // Gambar placeholder saat loading
            .into(holder.photo) // Target ImageView
    }

    // Return jumlah total item dalam adapter
    override fun getItemCount(): Int {
        return users.size
    }
}