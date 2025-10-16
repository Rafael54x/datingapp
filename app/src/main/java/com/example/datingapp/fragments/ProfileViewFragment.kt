package com.example.datingapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.models.User
import com.example.datingapp.utils.DummyData

private const val ARG_USER_ID = "userId"

class ProfileViewFragment : Fragment() {
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the user from DummyData based on the passed ID
        val user = DummyData.users.find { it.uid == userId }

        user?.let {
            populateProfileData(view, it)
        }
    }

    private fun populateProfileData(view: View, user: User) {
        // Find views
        val profileImage = view.findViewById<ImageView>(R.id.profile_image)
        val profileUsername = view.findViewById<TextView>(R.id.profile_username)
        val profileBio = view.findViewById<TextView>(R.id.profile_bio)
        val profileAge = view.findViewById<TextView>(R.id.profile_age)
        val profileGender = view.findViewById<TextView>(R.id.profile_gender)
        val profileSchoolYear = view.findViewById<TextView>(R.id.profile_schoolyear)
        val profileMajor = view.findViewById<TextView>(R.id.profile_major)

        // Load profile image using Glide
        Glide.with(this)
            .load(user.photoUrl)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(profileImage)

        // Set text data
        profileUsername.text = user.username
        profileBio.text = user.bio
        profileAge.text = user.age.toString()
        profileGender.text = user.gender?.displayName
        profileSchoolYear.text = user.schoolyear.toString()
        profileMajor.text = user.major?.displayName
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: String) =
            ProfileViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }
}
