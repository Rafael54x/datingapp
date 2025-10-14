package com.example.datingapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.R
import com.example.datingapp.utils.SharedPrefManager

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefManager = SharedPrefManager(this)

        // if saved user exists go to main
        if (sharedPrefManager.getUser() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)
        val edtUser = findViewById<EditText>(R.id.username)
        val edtPass = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val u = edtUser.text.toString().trim()
            val p = edtPass.text.toString().trim()

            if (sharedPrefManager.login(u, p)) {
                val user = sharedPrefManager.getUser()
                Toast.makeText(this, "Welcome ${user?.name}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
