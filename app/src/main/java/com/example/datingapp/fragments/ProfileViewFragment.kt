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
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils

private const val ARG_USER_ID = "userId"

class ProfileViewFragment : Fragment() {
    private var userId: String? = null
    private lateinit var profileImage: ImageView

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

        profileImage = view.findViewById(R.id.profile_image)

        view.findViewById<View>(R.id.scan_button).setOnClickListener {
            showProfilePopup()
        }


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

    private fun showProfilePopup() {
        val dialog = Dialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_profile_image, null)

        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.parseColor("#80000000")))
        dialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.setCanceledOnTouchOutside(true)

        val zoomIn = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in)
        view.startAnimation(zoomIn)

        val popupImage = view.findViewById<ImageView>(R.id.popup_profile_image_view)
        val gifOverlay = view.findViewById<ImageView>(R.id.gif_overlay)
        val checkIcon = view.findViewById<ImageView>(R.id.checkIcon)
        val verifiedText = view.findViewById<TextView>(R.id.verified_text)

        // Copy current loaded image
        popupImage.setImageDrawable(profileImage.drawable)

        // Load GIF
        Glide.with(this)
            .asGif()
            .load(R.drawable.scanning) // your GIF file
            .into(gifOverlay)

        // After 3s, hide GIF & show check icon with animation
        Handler(Looper.getMainLooper()).postDelayed({
            gifOverlay.visibility = View.GONE

            // Show check icon and animate
            checkIcon.visibility = View.VISIBLE

            val bounce = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
            val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    checkIcon.visibility = View.GONE
                    verifiedText.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed({
                       dialog.dismiss()
                    }, 2000)
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }
            })

            checkIcon.startAnimation(bounce)

            // After bounce, fade out icon
            Handler(Looper.getMainLooper()).postDelayed({
                checkIcon.startAnimation(fadeOut)
            }, 2000) // Visible for 2 seconds

        }, 3000)

        dialog.show()
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
