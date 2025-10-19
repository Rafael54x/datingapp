package com.example.datingapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.R
import com.example.datingapp.fragments.HomeFragment
import com.example.datingapp.fragments.MatchListFragment
import com.example.datingapp.fragments.ProfileFragment
import com.example.datingapp.utils.SharedPrefManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // SharedPrefManager untuk mengelola data user yang login
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi SharedPrefManager
        sharedPrefManager = SharedPrefManager(this)

        // Ambil referensi bottom navigation view dari layout
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

        // Set listener untuk menangani klik pada item bottom navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Jika home diklik, ganti fragment dengan HomeFragment
                R.id.nav_home -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment()).commit()

                // Jika match list diklik, ganti fragment dengan MatchListFragment
                R.id.nav_match_list -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MatchListFragment()).commit()

                // Jika profile diklik, ganti fragment dengan ProfileFragment
                R.id.nav_profile -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
            true // Return true untuk menandakan item telah diproses
        }

        // Jika activity baru dibuat (bukan dari rotation/config change),
        // tampilkan HomeFragment sebagai fragment default
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }
}