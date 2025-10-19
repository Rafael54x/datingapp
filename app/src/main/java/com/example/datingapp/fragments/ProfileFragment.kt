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

    // SharedPrefManager untuk mengambil data user
    private lateinit var sharedPrefManager: SharedPrefManager

    // Referensi ke semua view yang akan diisi dengan data
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
        // Inflate layout untuk fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi SharedPrefManager
        sharedPrefManager = SharedPrefManager(requireContext())

        // Inisialisasi semua views
        initializeViews(view)

        // Load data user pertama kali
        loadUserData()

        // Setup tombol Edit Profile
        view.findViewById<Button>(R.id.edit_profile_button).setOnClickListener {
            // Pindah ke ProfileEditActivity
            val intent = Intent(activity, ProfileEditActivity::class.java)
            startActivity(intent)
        }

        // Setup tombol Logout
        view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            logoutUser()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data setiap kali fragment di-resume
        // Ini memastikan perubahan dari edit profile langsung terlihat
        loadUserData()
    }

    // Inisialisasi semua view references
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

    // Load dan tampilkan data user dari SharedPreferences
    private fun loadUserData() {
        // Ambil data user yang sedang login
        val user = sharedPrefManager.getUser()

        user?.let { currentUser ->
            // Load foto profile menggunakan Glide
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.ic_profile_placeholder) // Gambar placeholder
                .error(R.drawable.ic_profile_placeholder)       // Gambar saat error
                .into(profileImage)

            // --- Set data profile publik ---
            profileUsername.text = currentUser.username
            profileBio.text = currentUser.bio ?: "No bio provided."
            profileAge.text = currentUser.age
            profileGender.text = currentUser.gender?.displayName
            profileSchoolYear.text = currentUser.schoolyear
            profileMajor.text = currentUser.major?.displayName

            // --- Set data profile private ---
            profileName.text = currentUser.name
            profileEmail.text = currentUser.email
            profilePassword.text = "********" // Mask password untuk keamanan

            // --- Set preferences ---
            val prefs = currentUser.preference

            // Gender preference: kebalikan dari gender user
            // Jika user Male, preferensinya Female, begitu sebaliknya
            preferenceGender.text = if(currentUser.gender == Gender.M)
                Gender.F.displayName
            else
                Gender.M.displayName

            // Year preference (angkatan)
            preferenceRange.text = prefs.yearPreferences?.displayName

            // Major preferences (bisa lebih dari satu)
            if (prefs.majorPreferences?.isNotEmpty() == true) {
                // Join semua major preferences dengan koma
                preferenceMajor.text = prefs.majorPreferences?.joinToString(", ") {
                    it.displayName
                }
            } else {
                preferenceMajor.text = "Not specified"
            }
        }
    }

    // Proses logout user
    private fun logoutUser() {
        // Hapus semua data dari SharedPreferences
        sharedPrefManager.clear()

        // Buat intent ke LoginActivity
        val intent = Intent(activity, LoginActivity::class.java).apply {
            // Clear activity stack agar user tidak bisa back ke app setelah logout
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Pindah ke LoginActivity
        startActivity(intent)

        // Tutup activity saat ini
        activity?.finish()
    }
}