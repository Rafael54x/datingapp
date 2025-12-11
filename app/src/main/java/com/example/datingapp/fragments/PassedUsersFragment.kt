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
import com.example.datingapp.adapters.PassedUsersAdapter
import com.example.datingapp.databinding.FragmentPassedUsersBinding
import com.example.datingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PassedUsersFragment : Fragment() {

    private var _binding: FragmentPassedUsersBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var passedUsersAdapter: PassedUsersAdapter
    private val passedUsers = mutableListOf<User>()
    private val TAG = "PassedUsersFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPassedUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        setupRecyclerView()
        loadPassedUsers()
        

    }

    private fun setupRecyclerView() {
        passedUsersAdapter = PassedUsersAdapter(passedUsers,
            onUserClick = { user ->
                val profileView = ProfileViewFragment.newInstance(user.uid)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileView)
                    .addToBackStack(null)
                    .commit()
            },
            onUnpassClick = { user ->
                removeFromPassed(user)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = passedUsersAdapter
        }
    }

    private fun loadPassedUsers() {
        val myId = auth.currentUser?.uid ?: return
        if (_binding == null) return
        
        binding.progressBar.visibility = View.VISIBLE
        
        firestore.collection("swipes").document(myId).get()
            .addOnSuccessListener { doc ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                
                val passedIds = doc.get("passed") as? List<String> ?: emptyList()
                
                if (passedIds.isEmpty()) {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }
                
                loadUsersBatch(passedIds)
            }
            .addOnFailureListener { e ->
                if (!isAdded || _binding == null) return@addOnFailureListener
                Log.e(TAG, "Error loading passed users", e)
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to load", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun loadUsersBatch(userIds: List<String>) {
        if (!isAdded || _binding == null || userIds.isEmpty()) {
            binding.progressBar.visibility = View.GONE
            return
        }
        
        passedUsers.clear()
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
                            passedUsers.add(user)
                        }
                    }
                    
                    loadedBatches++
                    if (loadedBatches == batches.size) {
                        binding.progressBar.visibility = View.GONE
                        if (passedUsers.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                        } else {
                            binding.emptyState.visibility = View.GONE
                            passedUsersAdapter.notifyDataSetChanged()
                        }
                    }
                }
        }
    }

    private fun removeFromPassed(user: User) {
        val myId = auth.currentUser?.uid ?: return

        firestore.collection("swipes").document(myId)
            .update("passed", FieldValue.arrayRemove(user.uid))
            .addOnSuccessListener {
                passedUsers.remove(user)
                passedUsersAdapter.notifyDataSetChanged()
                
                if (passedUsers.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                }
                
                Toast.makeText(requireContext(), "Removed from passed list", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to remove user", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    companion object {
        @JvmStatic
        fun newInstance() = PassedUsersFragment()
    }
}