package com.example.datingapp.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.R
import com.example.datingapp.models.User
import com.example.datingapp.utils.SharedPrefManager

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        sharedPrefManager = SharedPrefManager(this)

        val name = findViewById<EditText>(R.id.name)
        val age = findViewById<EditText>(R.id.age)
        val school = findViewById<EditText>(R.id.school)
        val save = findViewById<Button>(R.id.save)

        val user = sharedPrefManager.getUser()
        user?.let {
            name.setText(it.name)
            age.setText(it.age)
            school.setText(it.school)
        }

        save.setOnClickListener {
            val updatedName = name.text.toString()
            val updatedAge = age.text.toString()
            val updatedSchool = school.text.toString()

            val currentUser = sharedPrefManager.getUser()
            if (currentUser != null) {
                val updatedUser = currentUser.copy(
                    name = updatedName,
                    age = updatedAge,
                    school = updatedSchool
                )
                sharedPrefManager.saveUser(updatedUser)
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
