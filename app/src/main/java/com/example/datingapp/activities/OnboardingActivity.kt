package com.example.datingapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.R
import com.google.firebase.auth.FirebaseAuth

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is already logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        // Set layout untuk halaman onboarding
        setContentView(R.layout.activity_onboarding)

        // Ambil referensi tombol continue dari layout
        val btnContinue = findViewById<Button>(R.id.btn_continue)

        // Set listener untuk tombol continue
        btnContinue.setOnClickListener {
            // Pindah ke LoginActivity saat tombol diklik
            startActivity(Intent(this, LoginActivity::class.java))
            // Tutup OnboardingActivity agar user tidak bisa kembali ke sini dengan tombol back
            finish()
        }
    }
}