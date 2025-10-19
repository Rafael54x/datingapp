package com.example.datingapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.activities.LoginActivity
import com.example.datingapp.activities.ProfileEditActivity
import com.example.datingapp.utils.SharedPrefManager
import com.example.datingapp.models.Gender

class ProfileFragment : Fragment() {

    private lateinit var sharedPrefManager: SharedPrefManager

    // View references
    private lateinit var profileImage: ImageView
    private lateinit var profileUsername: TextView
    private lateinit var profileBio: TextView
    private lateinit var profileAge: TextView
    private lateinit var profileGender: TextView
    private lateinit var profileSchoolYear: TextView
    private lateinit var profileMajor: TextView
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profilePassword: TextView
    private lateinit var preferenceGender: TextView
    private lateinit var preferenceRange: TextView
    private lateinit var preferenceMajor: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefManager = SharedPrefManager(requireContext())

        // Initialize views
        initializeViews(view)

        // Load user data initially
        loadUserData()

        // Set up Edit Profile button
        view.findViewById<Button>(R.id.edit_profile_button).setOnClickListener {
            val intent = Intent(activity, ProfileEditActivity::class.java)
            startActivity(intent)
        }

        // Set up Logout button
        view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            logoutUser()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data every time the fragment is resumed,
        // to reflect any changes from the edit screen.
        loadUserData()
    }

    private fun initializeViews(view: View) {
        profileImage = view.findViewById(R.id.profile_image)
        profileUsername = view.findViewById(R.id.profile_username)
        profileBio = view.findViewById(R.id.profile_bio)
        profileAge = view.findViewById(R.id.profile_age)
        profileGender = view.findViewById(R.id.profile_gender)
        profileSchoolYear = view.findViewById(R.id.profile_schoolyear)
        profileMajor = view.findViewById(R.id.profile_major)
        profileName = view.findViewById(R.id.profile_name)
        profileEmail = view.findViewById(R.id.profile_email)
        profilePassword = view.findViewById(R.id.profile_password)
        preferenceGender = view.findViewById(R.id.preference_gender)
        preferenceRange = view.findViewById(R.id.preference_range)
        preferenceMajor = view.findViewById(R.id.preference_major)
    }

    private fun loadUserData() {
        val user = sharedPrefManager.getUser()
        user?.let { currentUser ->
            // Load profile image
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.ic_profile_placeholder) // Fallback image
                .error(R.drawable.ic_profile_placeholder)       // Error image
                .into(profileImage)

            // --- Set user details ---
            profileUsername.text = currentUser.username
            profileBio.text = currentUser.bio ?: "No bio provided."
            profileAge.text = currentUser.age
            profileGender.text = currentUser.gender?.displayName
            profileSchoolYear.text = currentUser.schoolyear
            profileMajor.text = currentUser.major?.displayName
            profileName.text = currentUser.name
            profileEmail.text = currentUser.email
            profilePassword.text = "********" // Mask password for security

            // --- Set preferences ---
            val prefs = currentUser.preference
            preferenceGender.text = if(currentUser.gender == Gender.M) Gender.F.displayName else Gender.M.displayName
            preferenceRange.text = prefs.yearPreferences?.displayName
            
            // Correctly access majorPreferences and display them
            if (prefs.majorPreferences?.isNotEmpty() == true) {
                preferenceMajor.text = prefs.majorPreferences?.joinToString(", ") { it.displayName }
            } else {
                preferenceMajor.text = "Not specified"
            }
        }
    }

    private fun logoutUser() {
        // Clear shared preferences
        sharedPrefManager.clear()

        // Navigate to LoginActivity
        val intent = Intent(activity, LoginActivity::class.java).apply {
            // Clear the activity stack to prevent user from going back to the app
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity?.finish()
    }
}
