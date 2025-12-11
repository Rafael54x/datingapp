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
    private val activeMatches = mutableListOf<Pair<Match, User>>()
    private val inactiveMatches = mutableListOf<Pair<Match, User>>()
    private val blockedMatches = mutableListOf<Pair<Match, User>>()
    private val favoriteMatches = mutableListOf<Pair<Match, User>>()
    private var currentTab = 0 // 0 = Active, 1 = Inactive, 2 = Blocked
    private var showingFavorites = false
    private val TAG = "MatchListFragment"
    
    private var matchesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var favoritesListener: com.google.firebase.firestore.ListenerRegistration? = null

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
        setupTabs()
        setupSearch()
        setupFavorites()
        setupSwipeRefresh()
        loadMatches()
    }
    
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                filterMatchesByTab()
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }
    
    private fun setupFavorites() {
        binding.favoriteIcon.setOnClickListener {
            showingFavorites = !showingFavorites
            updateFavoriteIcon()
            filterMatchesByTab()
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshMatches.setOnRefreshListener {
            loadMatches()
            binding.swipeRefreshMatches.isRefreshing = false
        }
    }
    
    private fun updateFavoriteIcon() {
        val iconRes = if (showingFavorites) {
            android.R.drawable.btn_star_big_on
        } else {
            android.R.drawable.btn_star_big_off
        }
        binding.favoriteIcon.setImageResource(iconRes)
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
    
    private fun filterMatchesByTab() {
        val sourceList = if (showingFavorites) {
            favoriteMatches
        } else {
            when (currentTab) {
                0 -> activeMatches
                1 -> inactiveMatches
                2 -> blockedMatches
                else -> activeMatches
            }
        }
        matchesWithUsers.clear()
        matchesWithUsers.addAll(sourceList)
        filterMatches(binding.searchMatches.text.toString())
    }
    
    private fun filterMatches(query: String) {
        val sourceList = if (showingFavorites) {
            favoriteMatches
        } else {
            when (currentTab) {
                0 -> activeMatches
                1 -> inactiveMatches
                2 -> blockedMatches
                else -> activeMatches
            }
        }
        
        if (query.isEmpty()) {
            matchesWithUsers.clear()
            matchesWithUsers.addAll(sourceList)
        } else {
            val filtered = sourceList.filter { (_, user) ->
                user.name?.contains(query, ignoreCase = true) == true
            }
            matchesWithUsers.clear()
            matchesWithUsers.addAll(filtered)
        }
        matchAdapter?.notifyDataSetChanged()
        
        if (matchesWithUsers.isEmpty()) {
            binding.noMatchesText.visibility = View.VISIBLE
            binding.matchesRecyclerView.visibility = View.GONE
        } else {
            binding.noMatchesText.visibility = View.GONE
            binding.matchesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        matchAdapter = MatchAdapter(matchesWithUsers,
            onItemClicked = { match, partner ->
                checkMutualLike(partner.uid) { mutualLike ->
                    if (!mutualLike) {
                        Toast.makeText(requireContext(), "Match no longer valid", Toast.LENGTH_SHORT).show()
                        return@checkMutualLike
                    }
                    
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
            },
            onFavoriteClicked = { match, partner ->
                toggleFavorite(match, partner)
            },
            isFavorite = { match ->
                favoriteMatches.any { it.first.matchId == match.matchId }
            },
            isBlocked = { partner ->
                blockedMatches.any { it.second.uid == partner.uid }
            }
        )
        binding.matchesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = matchAdapter
        }
    }

    private fun loadMatches() {
        val myId = auth.currentUser?.uid ?: return

        matchesListener?.remove()
        matchesListener = firestore.collection("matches")
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
                activeMatches.clear()
                inactiveMatches.clear()
                blockedMatches.clear()
                favoriteMatches.clear()

                var processedCount = 0
                val totalMatches = snapshots.documents.size

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
                                checkIfBlocked(partnerId) { isBlocked ->
                                    val matchPair = Pair(match, partner)
                                    allMatchesWithUsers.add(matchPair)
                                    
                                    if (isBlocked) {
                                        blockedMatches.add(matchPair)
                                    } else {
                                        checkMutualLike(partnerId) { mutualLike ->
                                            if (mutualLike) {
                                                activeMatches.add(matchPair)
                                            } else {
                                                inactiveMatches.add(matchPair)
                                            }
                                            
                                            processedCount++
                                            if (processedCount == totalMatches) {
                                                loadFavorites()
                                            }
                                        }
                                    }
                                    
                                    if (isBlocked) {
                                        processedCount++
                                        if (processedCount == totalMatches) {
                                            loadFavorites()
                                        }
                                    }
                                }
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

    private fun checkMutualLike(userId: String, callback: (Boolean) -> Unit) {
        val myId = auth.currentUser?.uid ?: return
        
        firestore.collection("swipes").document(myId).get()
            .addOnSuccessListener { myDoc ->
                val myLiked = myDoc.get("liked") as? List<String> ?: emptyList()
                
                if (!myLiked.contains(userId)) {
                    callback(false)
                    return@addOnSuccessListener
                }
                
                firestore.collection("swipes").document(userId).get()
                    .addOnSuccessListener { theirDoc ->
                        val theirLiked = theirDoc.get("liked") as? List<String> ?: emptyList()
                        callback(theirLiked.contains(myId))
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
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
                loadMatches()
            }
    }

    private fun unblockUser(myId: String, userId: String) {
        firestore.collection("blocks").document(myId)
            .update("blockedUsers", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "User unblocked", Toast.LENGTH_SHORT).show()
                loadMatches()
            }
    }
    
    private fun toggleFavorite(match: Match, partner: User) {
        val myId = auth.currentUser?.uid ?: return
        val isFavorite = favoriteMatches.any { it.first.matchId == match.matchId }
        
        if (isFavorite) {
            firestore.collection("favorites").document(myId)
                .update("favoriteUsers", com.google.firebase.firestore.FieldValue.arrayRemove(partner.uid))
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
                }
        } else {
            firestore.collection("favorites").document(myId)
                .set(mapOf("favoriteUsers" to com.google.firebase.firestore.FieldValue.arrayUnion(partner.uid)),
                    com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    private fun loadFavorites() {
        val myId = auth.currentUser?.uid ?: return
        
        favoritesListener?.remove()
        favoritesListener = firestore.collection("favorites").document(myId)
            .addSnapshotListener { doc, error ->
                if (!isAdded || _binding == null) return@addSnapshotListener
                
                if (error != null) {
                    Log.e(TAG, "Favorites listen failed.", error)
                    return@addSnapshotListener
                }
                
                val favoriteUserIds = doc?.get("favoriteUsers") as? List<String> ?: emptyList()
                favoriteMatches.clear()
                
                for (matchPair in allMatchesWithUsers) {
                    val partnerId = matchPair.first.users.firstOrNull { it != myId }
                    if (favoriteUserIds.contains(partnerId)) {
                        favoriteMatches.add(matchPair)
                    }
                }
                
                filterMatchesByTab()
                matchAdapter?.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        matchesListener?.remove()
        favoritesListener?.remove()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = MatchListFragment()
    }
}
