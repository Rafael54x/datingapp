package com.example.datingapp.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.R
import com.example.datingapp.databinding.ActivityEditProfileBinding
import com.example.datingapp.models.Gender
import com.example.datingapp.models.Jurusan
import com.example.datingapp.models.User
import com.example.datingapp.models.YearPreferences
import com.example.datingapp.utils.SharedPrefManager
import com.google.android.material.chip.Chip

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefManager = SharedPrefManager(this)

        setupDropdowns()
        loadUserData()

        binding.save.setOnClickListener {
            saveUserData()
        }

        binding.addMajorPreferenceButton.setOnClickListener {
            addMajorPreference()
        }
    }

    private fun setupDropdowns() {
        val genders = Gender.values().map { it.displayName }
        binding.editGender.setAdapter(createArrayAdapter(genders))

        val majors = Jurusan.values().map { it.displayName }
        binding.editMajor.setAdapter(createArrayAdapter(majors))
        binding.addMajorPreference.setAdapter(createArrayAdapter(majors))

        val yearPrefs = YearPreferences.values().map { it.displayName }
        binding.editRange.setAdapter(createArrayAdapter(yearPrefs))
        
        // Gender preference is auto-set and disabled
        binding.editGenderPreference.isEnabled = false
    }

    private fun loadUserData() {
        val user = sharedPrefManager.getUser()
        user?.let {
            binding.editUsername.setText(it.username)
            binding.editFullname.setText(it.name)
            binding.editAge.setText(it.age)
            binding.editSchoolyear.setText(it.schoolyear)
            binding.editEmail.setText(it.email)
            binding.editPassword.setText(it.password)

            // Set dropdown selections
            binding.editGender.setText(it.gender?.displayName, false)
            binding.editMajor.setText(it.major?.displayName, false)
            binding.editGenderPreference.setText(if (it.gender == Gender.M) Gender.F.displayName else Gender.M.displayName, false)
            binding.editRange.setText(it.preference.yearPreferences?.displayName, false)
            
            // Load major preferences as chips
            binding.majorPreferencesChipGroup.removeAllViews()
            it.preference.majorPreferences?.forEach { major ->
                addMajorChip(major.displayName)
            }
        }
    }
    
    private fun addMajorPreference() {
        val majorName = binding.addMajorPreference.text.toString()
        if (majorName.isNotBlank() && Jurusan.values().any { it.displayName == majorName }) {
            // Prevent adding duplicates
            val isAlreadyAdded = (0 until binding.majorPreferencesChipGroup.childCount).any {
                (binding.majorPreferencesChipGroup.getChildAt(it) as Chip).text.toString() == majorName
            }
            if (!isAlreadyAdded) {
                addMajorChip(majorName)
                binding.addMajorPreference.text.clear()
            } else {
                Toast.makeText(this, "Major already added.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select a valid major.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        val currentUser = sharedPrefManager.getUser()
        if (currentUser != null) {
            val selectedMajors = mutableListOf<Jurusan>()
            for (i in 0 until binding.majorPreferencesChipGroup.childCount) {
                val chip = binding.majorPreferencesChipGroup.getChildAt(i) as Chip
                Jurusan.values().find { it.displayName == chip.text.toString() }?.let {
                    selectedMajors.add(it)
                }
            }
            
            val updatedPreferences = currentUser.preference.copy(
                gender = if (binding.editGenderPreference.text.toString() == "Female") Gender.F else Gender.M,
                yearPreferences = YearPreferences.values().find { it.displayName == binding.editRange.text.toString() },
                majorPreferences = selectedMajors
            )

            val updatedUser = currentUser.copy(
                username = binding.editUsername.text.toString(),
                name = binding.editFullname.text.toString(),
                age = binding.editAge.text.toString(),
                schoolyear = binding.editSchoolyear.text.toString(),
                email = binding.editEmail.text.toString(),
                password = binding.editPassword.text.toString(),
                gender = Gender.values().find { it.displayName == binding.editGender.text.toString() },
                major = Jurusan.values().find { it.displayName == binding.editMajor.text.toString() },
                preference = updatedPreferences
            )

            sharedPrefManager.saveUser(updatedUser)
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addMajorChip(majorName: String) {
        val chip = Chip(this).apply {
            text = majorName
            isCloseIconVisible = true
            setOnCloseIconClickListener { binding.majorPreferencesChipGroup.removeView(this) }
        }
        binding.majorPreferencesChipGroup.addView(chip)
    }

    private fun createArrayAdapter(items: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
    }
}
