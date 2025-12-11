package com.example.datingapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.R
import com.example.datingapp.fragments.TermsConditionsFragment
import com.example.datingapp.models.Gender
import com.example.datingapp.models.Jurusan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        val firestore = Firebase.firestore

        val edtUsername = findViewById<EditText>(R.id.username)
        val edtPass = findViewById<EditText>(R.id.password)
        val edtName = findViewById<EditText>(R.id.name)
        val edtEmail = findViewById<EditText>(R.id.email)
        val edtAge = findViewById<EditText>(R.id.age)
        val edtGender = findViewById<AutoCompleteTextView>(R.id.gender)
        val edtSchoolYear = findViewById<AutoCompleteTextView>(R.id.schoolyear)
        val edtMajor = findViewById<AutoCompleteTextView>(R.id.major)
        val termsCheckbox = findViewById<CheckBox>(R.id.terms_checkbox)
        val termsLink = findViewById<TextView>(R.id.terms_link)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val backButton = findViewById<ImageView>(R.id.back_button)

        setupDropdowns(edtGender, edtSchoolYear, edtMajor)
        
        backButton.setOnClickListener {
            finish()
        }
        
        termsLink.setOnClickListener {
            val intent = Intent(this, TermsActivity::class.java)
            startActivity(intent)
        }

        btnSignUp.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPass.text.toString().trim()
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val age = edtAge.text.toString().trim()
            val genderStr = edtGender.text.toString().trim()
            val schoolyear = edtSchoolYear.text.toString().trim()
            val majorStr = edtMajor.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty() ||
                age.isEmpty() || genderStr.isEmpty() || schoolyear.isEmpty() || majorStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!termsCheckbox.isChecked) {
                Toast.makeText(this, "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (age.toIntOrNull() == null || age.toInt() < 17) {
                Toast.makeText(this, "Minimum age is 17", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.endsWith("@student.umn.ac.id")) {
                Toast.makeText(this, "Only @student.umn.ac.id emails are allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = Gender.values().find { it.displayName == genderStr }?.name
            val major = Jurusan.values().find { it.displayName == majorStr }?.name

            Log.d("RegisterActivity", "Starting registration for: $email")

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    Log.d("RegisterActivity", "Auth success, creating Firestore document")
                    val user = authResult.user ?: return@addOnSuccessListener
                    val userId = user.uid
                    Log.d("RegisterActivity", "User ID: $userId")
                    val userDoc = hashMapOf(
                        "uid" to userId,
                        "username" to username,
                        "name" to name,
                        "email" to email,
                        "password" to password,
                        "bio" to "",
                        "age" to age,
                        "schoolyear" to schoolyear,
                        "gender" to gender,
                        "major" to major,
                        "photoUrl" to "",
                        "school" to "UMN",
                        "likes" to listOf<String>(),
                        "preference" to mapOf(
                            "gender" to if (gender == "M") "F" else "M",
                            "yearPreferences" to "All",
                            "majorPreferences" to listOf<String>()
                        )
                    )
                    firestore.collection("users").document(userId)
                        .set(userDoc)
                        .addOnSuccessListener {
                            Log.d("RegisterActivity", "Firestore document created successfully")
                            user.sendEmailVerification()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Account created! Please verify your email before logging in.", Toast.LENGTH_LONG).show()
                                    auth.signOut()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Account created but failed to send verification email", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                    finish()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("RegisterActivity", "Firestore error: ${e.message}", e)
                            Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("RegisterActivity", "Auth error: ${e.message}", e)
                    Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun setupDropdowns(genderDropdown: AutoCompleteTextView, schoolYearDropdown: AutoCompleteTextView, majorDropdown: AutoCompleteTextView) {
        val genders = Gender.values().map { it.displayName }
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        genderDropdown.setAdapter(genderAdapter)

        val schoolYears = listOf("2021", "2022", "2023", "2024", "2025")
        val schoolYearAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, schoolYears)
        schoolYearDropdown.setAdapter(schoolYearAdapter)

        val majors = Jurusan.values().map { it.displayName }
        val majorAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, majors)
        majorDropdown.setAdapter(majorAdapter)
    }
}
