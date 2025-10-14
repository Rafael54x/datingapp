package com.example.datingapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.R
import com.example.datingapp.models.User
import com.example.datingapp.utils.SharedPrefManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        sharedPrefManager = SharedPrefManager(this)

        val edtUsername = findViewById<EditText>(R.id.username)
        val edtPass = findViewById<EditText>(R.id.password)
        val edtName = findViewById<EditText>(R.id.name)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPass.text.toString().trim()
            val name = edtName.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a new User object. Using username for both uid and username for simplicity.
            val newUser = User(
                uid = username,
                name = name,
                username = username,
                email = "$username@example.com", // Create a dummy email
                photoUrl = "https://via.placeholder.com/600x800.png?text=$name"
            )

            sharedPrefManager.register(newUser)
            sharedPrefManager.saveUser(newUser)

            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity() // Finish this activity and all parent activities
        }
    }
}
