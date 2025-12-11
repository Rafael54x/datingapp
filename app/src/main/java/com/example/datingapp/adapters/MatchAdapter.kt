package com.example.datingapp.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.databinding.ItemMatchBinding
import com.example.datingapp.models.Match
import com.example.datingapp.models.User

class MatchAdapter(
    private val matches: List<Pair<Match, User>>,
    private val onItemClicked: (Match, User) -> Unit,
    private val onPhotoClicked: (Match, User) -> Unit,
    private val onBlockClicked: (Match, User) -> Unit,
    private val onFavoriteClicked: (Match, User) -> Unit,
    private val isFavorite: (Match) -> Boolean,
    private val isBlocked: (User) -> Boolean
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    inner class MatchViewHolder(val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val (match, partner) = matches[position]

        holder.binding.apply {
            matchName.text = partner.name

            Glide.with(holder.itemView.context)
                .load(partner.photoUrl)
                .error(R.drawable.ic_profile)
                .into(matchPhoto)
        }

        // Click listener for the whole item
        holder.itemView.setOnClickListener {
            onItemClicked(match, partner)
        }

        // Click listener for the photo only
        holder.binding.matchPhoto.setOnClickListener {
            onPhotoClicked(match, partner)
        }

        // Long press for options menu
        holder.itemView.setOnLongClickListener {
            showOptionsDialog(holder, match, partner)
            true
        }
    }

    private fun showOptionsDialog(holder: MatchViewHolder, match: Match, partner: User) {
        val favoriteOption = if (isFavorite(match)) "Remove from Favorites" else "Add to Favorites"
        val blockOption = if (isBlocked(partner)) "Unblock User" else "Block User"
        val options = arrayOf(favoriteOption, blockOption)
        
        AlertDialog.Builder(holder.itemView.context)
            .setTitle(partner.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onFavoriteClicked(match, partner)
                    1 -> onBlockClicked(match, partner)
                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return matches.size
    }
}
