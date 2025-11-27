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

    // SharedPrefManager untuk menyimpan data user
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inisialisasi SharedPrefManager
        sharedPrefManager = SharedPrefManager(this)

        // Ambil referensi input fields dari layout
        val edtUsername = findViewById<EditText>(R.id.username)
        val edtPass = findViewById<EditText>(R.id.password)
        val edtName = findViewById<EditText>(R.id.name)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        // Set listener untuk tombol sign up
        btnSignUp.setOnClickListener {
            // Ambil text dari input dan hapus whitespace di awal/akhir
            val username = edtUsername.text.toString().trim()
            val password = edtPass.text.toString().trim()
            val name = edtName.text.toString().trim()

            // Validasi: pastikan semua field terisi
            if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Buat object User baru dengan data yang diinput
            // Menggunakan username untuk uid dan membuat dummy email
            val newUser = User(
                uid = username,
                name = name,
                username = username,
                email = "$username@example.com", // Email dummy
                password = password,
                likes = mutableListOf(),
                photoUrl = "https://example.com/default_profile_pic.jpg"
            )

            // Daftarkan user ke DummyData
            sharedPrefManager.register(newUser)
            // Simpan user ke SharedPreferences (login otomatis)
            sharedPrefManager.saveUser(newUser)

            // Tampilkan pesan sukses
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()

            // Pindah ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            // Tutup semua activity sebelumnya agar user tidak bisa back ke register
            finishAffinity()
        }
    }
}