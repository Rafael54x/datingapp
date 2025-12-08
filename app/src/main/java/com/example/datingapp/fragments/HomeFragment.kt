package com.example.datingapp.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.adapters.CardStackAdapter
import com.example.datingapp.models.Gender
import com.example.datingapp.models.Jurusan
import com.example.datingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.yuyakaido.android.cardstackview.*

class HomeFragment : Fragment(), CardStackListener {

    private lateinit var cardStackView: CardStackView
    private lateinit var layoutManager: CardStackLayoutManager
    private lateinit var adapter: CardStackAdapter
    private var users: MutableList<User> = mutableListOf()
    private var allUsers: MutableList<User> = mutableListOf()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var myId: String

    // Undo feature
    private var lastSwipedUser: User? = null
    private var lastSwipeDirection: Direction? = null
    
    // Filter preferences
    private var filterGender: Gender? = null
    private var filterMajor: String? = null
    private var filterYear: String? = null

    private val TAG = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            // Handle user not logged in
            return view
        }
        myId = auth.currentUser!!.uid

        // Setup CardStackView
        cardStackView = view.findViewById(R.id.card_stack_view)
        adapter = CardStackAdapter(users)
        layoutManager = CardStackLayoutManager(requireContext(), this).apply {
            setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
            setOverlayInterpolator(LinearInterpolator())
        }
        cardStackView.layoutManager = layoutManager
        cardStackView.adapter = adapter

        setupButtons(view)
        setupSwipeRefresh(view)
        loadUsersFromFirestore()

        return view
    }

    private fun setupButtons(view: View) {
        view.findViewById<ImageButton>(R.id.like_button).setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(200)
                .build()
            layoutManager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }
        view.findViewById<ImageButton>(R.id.dislike_button).setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(200)
                .build()
            layoutManager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }
        view.findViewById<ImageButton>(R.id.inspect_button).setOnClickListener {
            val position = layoutManager.topPosition
            if (position < users.size) {
                val user = users[position]
                val profileView = ProfileViewFragment.newInstance(user.uid)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileView)
                    .addToBackStack(null)
                    .commit()
            }
        }
        
        // Undo button
        view.findViewById<ImageButton>(R.id.undo_button)?.setOnClickListener {
            undoLastSwipe()
        }
        
        // Filter button
        view.findViewById<ImageButton>(R.id.filter_button)?.setOnClickListener {
            showFilterDialog()
        }
    }
    
    private fun setupSwipeRefresh(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            loadUsersFromFirestore()
        }
    }

    private fun loadUsersFromFirestore() {
        swipeRefreshLayout.isRefreshing = true
        
        // Get current user's gender first
        firestore.collection("users").document(myId).get()
            .addOnSuccessListener { myDoc ->
                val myGender = myDoc.getString("gender")
                
                firestore.collection("swipes").document(myId).get()
                    .addOnSuccessListener { swipeDoc ->
                        val liked = (swipeDoc.get("liked") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        val passed = (swipeDoc.get("passed") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        val alreadySwiped = (liked + passed).toSet()

                        firestore.collection("users").get()
                            .addOnSuccessListener { result ->
                                allUsers = result.toObjects(User::class.java).toMutableList()
                                var potentialMatches = allUsers.filter { user ->
                                    // Filter: not me, not already swiped, opposite gender only
                                    val isNotMe = user.uid != myId
                                    val notSwiped = !alreadySwiped.contains(user.uid)
                                    val oppositeGender = when(myGender) {
                                        "M" -> user.gender?.name == "F"
                                        "F" -> user.gender?.name == "M"
                                        else -> true
                                    }
                                    isNotMe && notSwiped && oppositeGender
                                }
                                
                                // Apply additional filters
                                potentialMatches = applyFilters(potentialMatches)
                                
                                users.clear()
                                users.addAll(potentialMatches)
                                adapter.notifyDataSetChanged()
                                swipeRefreshLayout.isRefreshing = false
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Error getting documents: ", exception)
                                Toast.makeText(context, "Failed to load users.", Toast.LENGTH_SHORT).show()
                                swipeRefreshLayout.isRefreshing = false
                            }
                    }
            }
    }
    
    private fun applyFilters(userList: List<User>): List<User> {
        var filtered = userList
        
        filterGender?.let { gender ->
            filtered = filtered.filter { it.gender == gender }
        }
        
        filterMajor?.let { major ->
            filtered = filtered.filter { it.major == major }
        }
        
        filterYear?.let { year ->
            filtered = filtered.filter { it.schoolyear == year }
        }
        
        return filtered
    }

    override fun onCardSwiped(direction: Direction?) {
        val position = layoutManager.topPosition - 1
        if (position >= users.size) return

        val swipedUser = users[position]
        lastSwipedUser = swipedUser
        lastSwipeDirection = direction
        val otherId = swipedUser.uid

        if (direction == Direction.Right) {
            swipeRight(myId, otherId)
        } else {
            swipeLeft(myId, otherId)
        }
    }
    
    private fun undoLastSwipe() {
        if (lastSwipedUser == null) {
            Toast.makeText(context, "No swipe to undo", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userId = lastSwipedUser!!.uid
        val field = if (lastSwipeDirection == Direction.Right) "liked" else "passed"
        
        firestore.collection("swipes").document(myId)
            .update(field, FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                layoutManager.setTopPosition(layoutManager.topPosition - 1)
                Toast.makeText(context, "Swipe undone", Toast.LENGTH_SHORT).show()
                lastSwipedUser = null
                lastSwipeDirection = null
            }
    }
    
    private fun showFilterDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_filter)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        val majorSpinner = dialog.findViewById<Spinner>(R.id.filter_major_spinner)
        val yearSpinner = dialog.findViewById<Spinner>(R.id.filter_year_spinner)
        
        val majorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
            listOf("All") + Jurusan.values().map { it.name })
        majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        majorSpinner.adapter = majorAdapter
        
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
            listOf("All", "2021", "2022", "2023", "2024", "2025"))
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter
        
        dialog.findViewById<Button>(R.id.apply_filter_button).setOnClickListener {
            filterMajor = if (majorSpinner.selectedItem.toString() != "All") {
                majorSpinner.selectedItem.toString()
            } else null
            
            filterYear = if (yearSpinner.selectedItem.toString() != "All") {
                yearSpinner.selectedItem.toString()
            } else null
            
            loadUsersFromFirestore()
            dialog.dismiss()
            Toast.makeText(context, "Filter applied", Toast.LENGTH_SHORT).show()
        }
        
        dialog.findViewById<Button>(R.id.clear_filter_button).setOnClickListener {
            filterGender = null
            filterMajor = null
            filterYear = null
            loadUsersFromFirestore()
            dialog.dismiss()
            Toast.makeText(context, "Filter cleared", Toast.LENGTH_SHORT).show()
        }
        
        dialog.show()
    }

    private fun swipeRight(myId: String, otherId: String) {
        val swipesRef = firestore.collection("swipes")
        val myDocRef = swipesRef.document(myId)

        myDocRef.set(mapOf("liked" to FieldValue.arrayUnion(otherId)), SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Liked user: $otherId")
                // Check for a match
                swipesRef.document(otherId).get()
                    .addOnSuccessListener { snap ->
                        val otherLiked = snap.get("liked") as? List<*>
                        if (otherLiked?.contains(myId) == true) {
                            // Match found!
                            Log.d(TAG, "Match found with $otherId")
                            val matchId = listOf(myId, otherId).sorted().joinToString("_")
                            createMatch(matchId, myId, otherId)
                            val matchedUser = users.firstOrNull { it.uid == otherId }
                            if (matchedUser != null) {
                                showMatchDialog(matchedUser, matchId)
                            }
                        }
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error on swipe right: ${e.message}")
            }
    }

    private fun swipeLeft(myId: String, otherId: String) {
        val swipesRef = firestore.collection("swipes")
        swipesRef.document(myId).set(mapOf("passed" to FieldValue.arrayUnion(otherId)), SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "Passed user: $otherId") }
    }

    private fun createMatch(matchId: String, userA: String, userB: String) {
        val matchesRef = firestore.collection("matches")
        val matchData = mapOf(
            "users" to listOf(userA, userB),
            "lastMessage" to "You matched! Say hi!",
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Use SetOptions.merge() to create the document if it doesn't exist, or update it if it does.
        // This prevents overwriting data if the match document was created by the other user.
        matchesRef.document(matchId).set(matchData, SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "Match document ensured: $matchId") }
            .addOnFailureListener { e -> Log.e(TAG, "Error creating match: ${e.message}") }
    }


    private fun showMatchDialog(matchedUser: User, matchId: String) {
        if (!isAdded) return // Ensure fragment is attached

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_match)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val userImageView = dialog.findViewById<ImageView>(R.id.match_user_image)
        matchedUser.photoUrl?.let {
            Glide.with(requireContext()).load(it).into(userImageView)
        }

        dialog.findViewById<Button>(R.id.start_chatting_button).setOnClickListener {
            dialog.dismiss()
            // The key change is here: Pass the matchId
            val chatFragment = ChatFragment.newInstance(matchId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit()
        }
        dialog.show()
    }

    // Unused CardStackListener methods
    override fun onCardDragging(direction: Direction?, ratio: Float) {}
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: View?, position: Int) {}
    override fun onCardDisappeared(view: View?, position: Int) {}

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
