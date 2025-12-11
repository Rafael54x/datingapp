package com.example.datingapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.datingapp.R
import com.example.datingapp.adapters.LikesAdapter
import com.example.datingapp.databinding.FragmentLikesBinding
import com.example.datingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LikesFragment : Fragment() {

    private var _binding: FragmentLikesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var likesAdapter: LikesAdapter
    private val usersWhoLikedMe = mutableListOf<User>()
    private val TAG = "LikesFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLikesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        setupRecyclerView()
        loadUsersWhoLikedMe()
        

    }

    private fun setupRecyclerView() {
        likesAdapter = LikesAdapter(usersWhoLikedMe, 
            onUserClick = { user ->
                val profileView = ProfileViewFragment.newInstance(user.uid)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileView)
                    .addToBackStack(null)
                    .commit()
            },
            onLikeBackClick = { user ->
                likeBackUser(user)
            }
        )
        
        binding.likesRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = likesAdapter
        }
    }

    private fun loadUsersWhoLikedMe() {
        val myId = auth.currentUser?.uid ?: return
        if (_binding == null) return
        
        binding.progressBar.visibility = View.VISIBLE
        
        firestore.collection("swipes").get()
            .addOnSuccessListener { documents ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                
                usersWhoLikedMe.clear()
                val userIdsWhoLikedMe = mutableListOf<String>()
                
                for (doc in documents) {
                    val likedUsers = doc.get("liked") as? List<String> ?: emptyList()
                    if (likedUsers.contains(myId) && doc.id != myId) {
                        userIdsWhoLikedMe.add(doc.id)
                    }
                }
                
                if (userIdsWhoLikedMe.isEmpty()) {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }
                
                // Load users in batches to avoid ANR
                loadUsersBatch(userIdsWhoLikedMe)
            }
            .addOnFailureListener { e ->
                if (!isAdded || _binding == null) return@addOnFailureListener
                Log.e(TAG, "Error loading likes", e)
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to load likes", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun loadUsersBatch(userIds: List<String>) {
        if (!isAdded || _binding == null || userIds.isEmpty()) {
            binding.progressBar.visibility = View.GONE
            return
        }
        
        val batchSize = 10
        val batches = userIds.chunked(batchSize)
        var loadedBatches = 0
        
        for (batch in batches) {
            firestore.collection("users")
                .whereIn("uid", batch)
                .get()
                .addOnSuccessListener { users ->
                    if (!isAdded || _binding == null) return@addOnSuccessListener
                    
                    for (doc in users) {
                        val user = doc.toObject(User::class.java)
                        if (user != null) {
                            usersWhoLikedMe.add(user)
                        }
                    }
                    
                    loadedBatches++
                    if (loadedBatches == batches.size) {
                        binding.progressBar.visibility = View.GONE
                        if (usersWhoLikedMe.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                        } else {
                            binding.emptyState.visibility = View.GONE
                            likesAdapter.notifyDataSetChanged()
                        }
                    }
                }
        }
    }

    private fun likeBackUser(user: User) {
        val myId = auth.currentUser?.uid ?: return
        
        // Add to my liked list
        firestore.collection("swipes").document(myId)
            .set(mapOf("liked" to com.google.firebase.firestore.FieldValue.arrayUnion(user.uid)), 
                com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                // Create match since both liked each other
                val matchId = listOf(myId, user.uid).sorted().joinToString("_")
                createMatch(matchId, myId, user.uid)
                
                Toast.makeText(requireContext(), "Matched with ${user.name}!", Toast.LENGTH_SHORT).show()
                
                // Remove from list
                usersWhoLikedMe.remove(user)
                likesAdapter.notifyDataSetChanged()
                
                if (usersWhoLikedMe.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to like back", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun createMatch(matchId: String, userA: String, userB: String) {
        val matchData = mapOf(
            "users" to listOf(userA, userB),
            "lastMessage" to "You matched! Say hi!",
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        
        firestore.collection("matches").document(matchId)
            .set(matchData, com.google.firebase.firestore.SetOptions.merge())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = LikesFragment()
    }
}
