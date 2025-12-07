package com.example.datingapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.activities.LoginActivity
import com.example.datingapp.activities.ProfileEditActivity
import com.example.datingapp.models.Gender
import com.example.datingapp.models.Jurusan
import com.example.datingapp.models.YearPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

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

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
            return
        }

        initializeViews(view)
        loadUserData()

        // Setup tombol Edit Profile
        view.findViewById<Button>(R.id.edit_profile_button).setOnClickListener {
            // Pindah ke ProfileEditActivity
            val intent = Intent(activity, ProfileEditActivity::class.java)
            startActivity(intent)
        }

        // Setup tombol See Who Liked You
        view.findViewById<Button>(R.id.btn_see_likes).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LikesFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
        
        // Setup tombol My Likes
        view.findViewById<Button>(R.id.btn_my_likes).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyLikesFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
        
        // Setup tombol Delete Account
        view.findViewById<Button>(R.id.btn_delete_account).setOnClickListener {
            showDeleteAccountDialog()
        }

        // Setup tombol Change Password
        view.findViewById<Button>(R.id.btn_change_password).setOnClickListener {
            showChangePasswordDialog()
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
        preferenceGender = view.findViewById(R.id.preference_gender)
        preferenceRange = view.findViewById(R.id.preference_range)
        preferenceMajor = view.findViewById(R.id.preference_major)
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val data = doc.data ?: return@addOnSuccessListener

                Glide.with(this)
                    .load(data["photoUrl"] as? String)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImage)

                profileUsername.text = data["username"] as? String
                profileBio.text = (data["bio"] as? String) ?: "No bio provided."
                profileAge.text = data["age"] as? String
                profileSchoolYear.text = data["schoolyear"] as? String
                profileName.text = data["name"] as? String
                profileEmail.text = data["email"] as? String

                val genderStr = data["gender"] as? String
                profileGender.text = Gender.values().find { it.name == genderStr }?.displayName

                val majorStr = data["major"] as? String
                profileMajor.text = Jurusan.values().find { it.name == majorStr }?.displayName

                val prefs = data["preference"] as? Map<*, *>
                prefs?.let {
                    val prefGender = it["gender"] as? String
                    preferenceGender.text = Gender.values().find { g -> g.name == prefGender }?.displayName

                    val yearPref = it["yearPreferences"] as? String
                    preferenceRange.text = YearPreferences.values().find { y -> y.name == yearPref }?.displayName

                    @Suppress("UNCHECKED_CAST")
                    val majorPrefs = it["majorPreferences"] as? List<String>
                    if (!majorPrefs.isNullOrEmpty()) {
                        preferenceMajor.text = majorPrefs.joinToString(", ") { m ->
                            Jurusan.values().find { j -> j.name == m }?.displayName ?: m
                        }
                    } else {
                        preferenceMajor.text = "Not specified"
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteAccountDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteAccount() {
        val userId = auth.currentUser?.uid ?: return
        val user = auth.currentUser ?: return
        
        // Show progress
        val progressDialog = android.app.AlertDialog.Builder(requireContext())
            .setMessage("Deleting account...")
            .setCancelable(false)
            .create()
        progressDialog.show()
        
        // 1. Remove userId from other users' liked/passed arrays
        firestore.collection("swipes").get()
            .addOnSuccessListener { swipes ->
                for (doc in swipes) {
                    @Suppress("UNCHECKED_CAST")
                    val liked = doc.get("liked") as? List<String> ?: emptyList()
                    @Suppress("UNCHECKED_CAST")
                    val passed = doc.get("passed") as? List<String> ?: emptyList()
                    
                    if (liked.contains(userId) || passed.contains(userId)) {
                        val updates = mutableMapOf<String, Any>()
                        if (liked.contains(userId)) {
                            updates["liked"] = com.google.firebase.firestore.FieldValue.arrayRemove(userId)
                        }
                        if (passed.contains(userId)) {
                            updates["passed"] = com.google.firebase.firestore.FieldValue.arrayRemove(userId)
                        }
                        doc.reference.update(updates)
                    }
                }
                
                // 2. Remove from other users' blocks
                firestore.collection("blocks").get()
                    .addOnSuccessListener { blocks ->
                        for (doc in blocks) {
                            @Suppress("UNCHECKED_CAST")
                            val blockedUsers = doc.get("blockedUsers") as? List<String> ?: emptyList()
                            if (blockedUsers.contains(userId)) {
                                doc.reference.update("blockedUsers", 
                                    com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                            }
                        }
                        
                        // 3. Delete all matches involving this user
                        firestore.collection("matches")
                            .whereArrayContains("users", userId)
                            .get()
                            .addOnSuccessListener { matches ->
                                for (match in matches) {
                                    // Delete chat subcollection
                                    match.reference.collection("chats").get()
                                        .addOnSuccessListener { chats ->
                                            for (chat in chats) {
                                                chat.reference.delete()
                                            }
                                        }
                                    // Delete match document
                                    match.reference.delete()
                                }
                                
                                // 4. Delete user's own data
                                firestore.collection("swipes").document(userId).delete()
                                firestore.collection("blocks").document(userId).delete()
                                firestore.collection("users").document(userId).delete()
                                
                                // 5. Delete Firebase Auth account
                                user.delete()
                                    .addOnSuccessListener {
                                        progressDialog.dismiss()
                                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(activity, LoginActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        }
                                        startActivity(intent)
                                        activity?.finish()
                                    }
                                    .addOnFailureListener { e ->
                                        progressDialog.dismiss()
                                        Toast.makeText(requireContext(), "Failed to delete account: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to delete data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val oldPasswordInput = dialogView.findViewById<android.widget.EditText>(R.id.old_password_input)
        val newPasswordInput = dialogView.findViewById<android.widget.EditText>(R.id.new_password_input)
        val confirmPasswordInput = dialogView.findViewById<android.widget.EditText>(R.id.confirm_password_input)
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val oldPassword = oldPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()
                
                if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (newPassword.length < 6) {
                    Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                changePassword(oldPassword, newPassword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun changePassword(oldPassword: String, newPassword: String) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return
        
        // Re-authenticate user
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPassword)
        
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Update password
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to change password: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Old password is incorrect", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun logoutUser() {
        auth.signOut()

        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity?.finish()
    }
}