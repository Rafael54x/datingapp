package com.example.datingapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.datingapp.R
import com.example.datingapp.fragments.HomeFragment
import com.example.datingapp.fragments.MatchListFragment
import com.example.datingapp.fragments.ProfileFragment
import com.example.datingapp.utils.SharedPrefManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private val TAG = "MainActivity"
    private val NOTIFICATION_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefManager = SharedPrefManager(this)
        
        requestNotificationPermission()
        registerFCMToken()

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
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }
    
    private fun registerFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
            
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d(TAG, "FCM token saved")
                    }
            }
        }
    }
}
