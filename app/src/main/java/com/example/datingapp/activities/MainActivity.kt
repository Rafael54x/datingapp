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

    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPrefManager = SharedPrefManager(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment()).commit()
                R.id.nav_match_list -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MatchListFragment()).commit()
                R.id.nav_profile -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
            true
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }
}
