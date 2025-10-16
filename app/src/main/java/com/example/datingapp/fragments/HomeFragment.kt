package com.example.datingapp.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.adapters.CardStackAdapter
import com.example.datingapp.models.User
import com.example.datingapp.utils.DummyData
import com.example.datingapp.utils.SharedPrefManager
import com.yuyakaido.android.cardstackview.*

class HomeFragment : Fragment(), CardStackListener {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var cardStackView: CardStackView
    private lateinit var layoutManager: CardStackLayoutManager
    private lateinit var adapter: CardStackAdapter
    private val users = DummyData.users

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        sharedPrefManager = SharedPrefManager(requireContext())
        cardStackView = view.findViewById(R.id.card_stack_view)
        
        adapter = CardStackAdapter(users)

        layoutManager = CardStackLayoutManager(requireContext(), this).apply {
            setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
            setOverlayInterpolator(LinearInterpolator())
        }

        cardStackView.layoutManager = layoutManager
        cardStackView.adapter = adapter

        view.findViewById<ImageButton>(R.id.like_button).setOnClickListener {
            cardStackView.swipe()
        }

        view.findViewById<ImageButton>(R.id.dislike_button).setOnClickListener {
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

        return view
    }

    override fun onCardSwiped(direction: Direction?) {
        if (direction == Direction.Right) {
            val position = layoutManager.topPosition - 1
            if (position < users.size) {
                val likedUser = users[position]
                sharedPrefManager.addLike(likedUser.uid)
                if (sharedPrefManager.isMatch(likedUser.uid)) {
                    showMatchDialog(likedUser)
                }
            }
        }
    }

    private fun showMatchDialog(matchedUser: User) {
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
            val chatFragment = ChatFragment.newInstance(matchedUser.uid)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit()
        }

        dialog.show()
    }

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
