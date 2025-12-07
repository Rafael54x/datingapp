package com.example.datingapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.datingapp.R
import com.example.datingapp.adapters.MatchAdapter
import com.example.datingapp.databinding.FragmentMatchListBinding
import com.example.datingapp.models.Match
import com.example.datingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MatchListFragment : Fragment() {

    private var _binding: FragmentMatchListBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var matchAdapter: MatchAdapter? = null
    private val matchesWithUsers = mutableListOf<Pair<Match, User>>()
    private val allMatchesWithUsers = mutableListOf<Pair<Match, User>>()
    private val TAG = "MatchListFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            binding.noMatchesText.visibility = View.VISIBLE
            return
        }

        setupRecyclerView()
        setupSearch()
        loadMatches()
    }
    
    private fun setupSearch() {
        binding.searchMatches.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMatches(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
    
    private fun filterMatches(query: String) {
        if (query.isEmpty()) {
            matchesWithUsers.clear()
            matchesWithUsers.addAll(allMatchesWithUsers)
        } else {
            val filtered = allMatchesWithUsers.filter { (_, user) ->
                user.name?.contains(query, ignoreCase = true) == true
            }
            matchesWithUsers.clear()
            matchesWithUsers.addAll(filtered)
        }
        matchAdapter?.notifyDataSetChanged()
        
        if (matchesWithUsers.isEmpty() && query.isNotEmpty()) {
            binding.noMatchesText.visibility = View.VISIBLE
            binding.matchesRecyclerView.visibility = View.GONE
        } else if (matchesWithUsers.isNotEmpty()) {
            binding.noMatchesText.visibility = View.GONE
            binding.matchesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        matchAdapter = MatchAdapter(matchesWithUsers,
            onItemClicked = { match, partner ->
                checkIfBlockedBidirectional(partner.uid) { isBlocked, blockedByPartner ->
                    when {
                        isBlocked -> Toast.makeText(requireContext(), "You have blocked this user", Toast.LENGTH_SHORT).show()
                        blockedByPartner -> Toast.makeText(requireContext(), "You cannot chat with this user", Toast.LENGTH_SHORT).show()
                        else -> {
                            val chatFragment = ChatFragment.newInstance(match.matchId)
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, chatFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
            },
            onPhotoClicked = { match, partner ->
                val profileViewFragment = ProfileViewFragment.newInstance(partner.uid)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileViewFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onBlockClicked = { match, partner ->
                showBlockDialog(partner)
            }
        )
        binding.matchesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = matchAdapter
        }
    }

    private fun loadMatches() {
        val myId = auth.currentUser?.uid ?: return

        firestore.collection("matches")
            .whereArrayContains("users", myId)
            .addSnapshotListener { snapshots, error ->
                if (!isAdded || _binding == null) return@addSnapshotListener
                
                if (error != null) {
                    Log.e(TAG, "Listen failed.", error)
                    Toast.makeText(requireContext(), "Failed to load matches", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    binding.noMatchesText.visibility = View.VISIBLE
                    binding.matchesRecyclerView.visibility = View.GONE
                    matchesWithUsers.clear()
                    allMatchesWithUsers.clear()
                    matchAdapter?.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                matchesWithUsers.clear()
                allMatchesWithUsers.clear()

                for (doc in snapshots.documents) {
                    val users = doc.get("users") as? List<String> ?: continue
                    val partnerId = users.firstOrNull { it != myId } ?: continue

                    val timestamp = doc.getTimestamp("timestamp")
                    val timestampLong = timestamp?.seconds ?: 0L

                    val match = Match(
                        matchId = doc.id,
                        user1 = users[0],
                        user2 = users[1],
                        users = users,
                        lastMessage = doc.getString("lastMessage") ?: "",
                        timestamp = timestampLong
                    )

                    firestore.collection("users").document(partnerId).get()
                        .addOnSuccessListener { userDoc ->
                            if (!isAdded || _binding == null) return@addOnSuccessListener
                            
                            val partner = userDoc.toObject(User::class.java)
                            if (partner != null) {
                                allMatchesWithUsers.add(Pair(match, partner))
                                filterMatches(binding.searchMatches.text.toString())
                            }
                        }
                }
            }
    }

    private fun showBlockDialog(user: User) {
        val myId = auth.currentUser?.uid ?: return
        
        checkIfBlocked(user.uid) { isBlocked ->
            val message = if (isBlocked) "Unblock ${user.name}?" else "Block ${user.name}?"
            val action = if (isBlocked) "Unblock" else "Block"
            
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("$action User")
                .setMessage(message)
                .setPositiveButton(action) { _, _ ->
                    if (isBlocked) {
                        unblockUser(myId, user.uid)
                    } else {
                        blockUser(myId, user.uid)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun checkIfBlocked(userId: String, callback: (Boolean) -> Unit) {
        val myId = auth.currentUser?.uid ?: return
        
        firestore.collection("blocks").document(myId).get()
            .addOnSuccessListener { doc ->
                val blockedUsers = doc.get("blockedUsers") as? List<String> ?: emptyList()
                callback(blockedUsers.contains(userId))
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    private fun checkIfBlockedBidirectional(userId: String, callback: (Boolean, Boolean) -> Unit) {
        val myId = auth.currentUser?.uid ?: return
        
        firestore.collection("blocks").document(myId).get()
            .addOnSuccessListener { myDoc ->
                val myBlockedUsers = myDoc.get("blockedUsers") as? List<String> ?: emptyList()
                val iBlockedThem = myBlockedUsers.contains(userId)
                
                firestore.collection("blocks").document(userId).get()
                    .addOnSuccessListener { theirDoc ->
                        val theirBlockedUsers = theirDoc.get("blockedUsers") as? List<String> ?: emptyList()
                        val theyBlockedMe = theirBlockedUsers.contains(myId)
                        callback(iBlockedThem, theyBlockedMe)
                    }
                    .addOnFailureListener {
                        callback(iBlockedThem, false)
                    }
            }
            .addOnFailureListener {
                callback(false, false)
            }
    }

    private fun blockUser(myId: String, userId: String) {
        firestore.collection("blocks").document(myId)
            .set(mapOf("blockedUsers" to com.google.firebase.firestore.FieldValue.arrayUnion(userId)), 
                com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "User blocked", Toast.LENGTH_SHORT).show()
            }
    }

    private fun unblockUser(myId: String, userId: String) {
        firestore.collection("blocks").document(myId)
            .update("blockedUsers", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "User unblocked", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = MatchListFragment()
    }
}
