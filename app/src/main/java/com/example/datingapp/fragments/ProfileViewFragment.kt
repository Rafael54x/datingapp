package com.example.datingapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.models.User
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.firebase.firestore.FirebaseFirestore

// Konstanta untuk argument key
private const val ARG_USER_ID = "userId"

class ProfileViewFragment : Fragment() {
    private var userId: String? = null
    private lateinit var profileImage: ImageView
    private lateinit var firestore: FirebaseFirestore
    private var isPhotoVerified: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ambil user ID dari arguments
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment
        return inflater.inflate(R.layout.fragment_profile_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        profileImage = view.findViewById(R.id.profile_image)

        view.findViewById<android.widget.ImageButton>(R.id.back_button)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadUserData(view)
    }

    private fun loadUserData(view: View) {
        val uid = userId ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val user = doc.toObject(User::class.java)
                user?.let {
                    isPhotoVerified = doc.getBoolean("photoVerified") ?: false
                    populateProfileData(view, it)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    // Isi semua view dengan data user
    private fun populateProfileData(view: View, user: User) {
        // Ambil referensi semua views
        val profileImage = view.findViewById<ImageView>(R.id.profile_image)
        val verifiedBadge = view.findViewById<ImageView>(R.id.verified_badge)
        val profileUsername = view.findViewById<TextView>(R.id.profile_username)
        val profileBio = view.findViewById<TextView>(R.id.profile_bio)
        val profileAge = view.findViewById<TextView>(R.id.profile_age)
        val profileGender = view.findViewById<TextView>(R.id.profile_gender)
        val profileSchoolYear = view.findViewById<TextView>(R.id.profile_schoolyear)
        val profileMajor = view.findViewById<TextView>(R.id.profile_major)

        // Load foto profile menggunakan Glide
        Glide.with(this)
            .load(user.photoUrl)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .into(profileImage)

        // Show verified badge if photo is verified
        if (isPhotoVerified) {
            verifiedBadge.visibility = View.VISIBLE
        } else {
            verifiedBadge.visibility = View.GONE
        }

        // Set semua data user ke TextView
        profileUsername.text = user.username
        profileBio.text = user.bio
        profileAge.text = user.age.toString()
        profileGender.text = user.gender?.displayName
        profileSchoolYear.text = user.schoolyear.toString()
        profileMajor.text = user.major?.displayName
    }

    // Tampilkan popup untuk scanning/verifikasi foto profile
    private fun showProfilePopup() {
        // Buat dialog
        val dialog = Dialog(requireContext())

        // Inflate layout popup
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_profile_image, null)

        dialog.setContentView(view)

        // Set background semi-transparent hitam
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(android.graphics.Color.parseColor("#80000000"))
        )

        // Set ukuran dialog
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // Set posisi di tengah layar
        dialog.window?.setGravity(Gravity.CENTER)

        // Dialog bisa ditutup dengan tap di luar
        dialog.setCanceledOnTouchOutside(true)

        // Load dan jalankan animasi zoom in
        val zoomIn = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in)
        view.startAnimation(zoomIn)

        // Ambil referensi views dalam popup
        val popupImage = view.findViewById<ImageView>(R.id.popup_profile_image_view)
        val gifOverlay = view.findViewById<ImageView>(R.id.gif_overlay)
        val checkIcon = view.findViewById<ImageView>(R.id.checkIcon)
        val verifiedText = view.findViewById<TextView>(R.id.verified_text)

        // Copy gambar dari profile image ke popup
        popupImage.setImageDrawable(profileImage.drawable)

        // Load GIF animasi scanning
        Glide.with(this)
            .asGif()
            .load(R.drawable.scanning) // File GIF scanning
            .into(gifOverlay)

        // Setelah 3 detik, sembunyikan GIF dan tampilkan checkmark
        Handler(Looper.getMainLooper()).postDelayed({
            // Sembunyikan GIF scanning
            gifOverlay.visibility = View.GONE

            // Tampilkan check icon
            checkIcon.visibility = View.VISIBLE

            // Load animasi bounce dan fade out
            val bounce = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
            val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

            // Set listener untuk animasi fade out
            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    // Setelah fade out selesai, sembunyikan check icon
                    checkIcon.visibility = View.GONE

                    // Tampilkan text "Verified Picture"
                    verifiedText.visibility = View.VISIBLE

                    // Setelah 2 detik, tutup dialog
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                    }, 2000)
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })

            // Jalankan animasi bounce pada check icon
            checkIcon.startAnimation(bounce)

            // Setelah 2 detik bounce, jalankan fade out
            Handler(Looper.getMainLooper()).postDelayed({
                checkIcon.startAnimation(fadeOut)
            }, 2000)

        }, 3000) // Delay 3 detik untuk scanning

        // Update verified text berdasarkan status
        Handler(Looper.getMainLooper()).postDelayed({
            if (isPhotoVerified) {
                verifiedText.text = "✓ Verified Original Photo"
                verifiedText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            } else {
                verifiedText.text = "⚠ Photo Not Verified"
                verifiedText.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
            }
        }, 5000)

        // Tampilkan dialog
        dialog.show()
    }

    companion object {
        // Factory method untuk membuat instance dengan user ID
        @JvmStatic
        fun newInstance(userId: String) =
            ProfileViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }
}