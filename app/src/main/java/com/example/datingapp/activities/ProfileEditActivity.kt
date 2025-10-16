package com.example.datingapp.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.R
import com.example.datingapp.models.Jurusan
import com.example.datingapp.models.Preferences
import com.example.datingapp.models.User
import com.example.datingapp.models.Gender
import com.example.datingapp.utils.SharedPrefManager

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager

    // Declare views
    private lateinit var username: EditText
    private lateinit var schoolyear: EditText
    private lateinit var gender: Spinner
    private lateinit var major: Spinner
    private lateinit var fullname: EditText
    private lateinit var birthday: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var genderPreference: Spinner
    private lateinit var range: Spinner
    private lateinit var majorPreferences: ListView
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        sharedPrefManager = SharedPrefManager(this)

        // Initialize views
        username = findViewById(R.id.edit_username)
        schoolyear = findViewById(R.id.edit_schoolyear)
        gender = findViewById(R.id.edit_gender)
        major = findViewById(R.id.edit_major)
        fullname = findViewById(R.id.edit_fullname)
        birthday = findViewById(R.id.edit_birthday)
        email = findViewById(R.id.edit_email)
        password = findViewById(R.id.edit_password)
        genderPreference = findViewById(R.id.edit_gender_preference)
        range = findViewById(R.id.edit_range)
        majorPreferences = findViewById(R.id.major_preferences)
        saveButton = findViewById(R.id.save)

        // Setup Spinners
        setupSpinners()

        // Load existing user data
        loadUserData()

        // Set save button listener
        saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun setupSpinners() {
        // Example setup for Jurusan spinner. You would do this for other spinners too.
        major.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Jurusan.values().map { it.displayName }
        )
    }

    private fun loadUserData() {
        val user = sharedPrefManager.getUser()
        user?.let {
            username.setText(it.username)
            schoolyear.setText(it.schoolyear)
            fullname.setText(it.name)
            birthday.setText(it.age) // Assuming 'age' field stores the birthday string
            email.setText(it.email)
            password.setText(it.password)

            // Set spinner selections
            setSpinnerSelection(gender, it.gender?.displayName)
            it.major?.let { majorEnum ->
                setSpinnerSelection(major, majorEnum.displayName)
            }
            setSpinnerSelection(genderPreference, if(it.gender == Gender.M) Gender.F.displayName else Gender.M.displayName)
            // Assuming range preference is also a string.

            setSpinnerSelection(range, it.preference.yearPreferences?.displayName)
        }
    }

    private fun saveUserData() {
        val currentUser = sharedPrefManager.getUser()
        if (currentUser != null) {
            val updatedPreferences = currentUser.preference.copy(
                gender = if(genderPreference.selectedItem.toString()=="Male") Gender.M else Gender.F,
                yearPreferences = currentUser.preference.yearPreferences,
                // Logic for yearPreferences and major preferences would be added here
            )

            val updatedUser = currentUser.copy(
                username = username.text.toString(),
                schoolyear = schoolyear.text.toString(),
                name = fullname.text.toString(),
                age = birthday.text.toString(), // Storing birthday string in 'age' field
                email = email.text.toString(),
                password = password.text.toString(),
                gender = if(gender.selectedItem.toString()=="Male") Gender.M else Gender.F,
                photoUrl = currentUser.photoUrl.toString(),
                major = Jurusan.values().find { it.displayName == major.selectedItem.toString() },
                preference = updatedPreferences
            )

            sharedPrefManager.saveUser(updatedUser)
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            finish() // Go back to the previous activity
        } else {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String?) {
        value?.let {
            val adapter = spinner.adapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == value) {
                    spinner.setSelection(i)
                    break
                }
            }
        }
    }
}
