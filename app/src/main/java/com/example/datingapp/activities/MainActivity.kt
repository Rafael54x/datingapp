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
        
        // Apply saved dark mode preference
        val sharedPref = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("dark_mode", false)
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        setContentView(R.layout.activity_main)

        sharedPrefManager = SharedPrefManager(this)
        
        requestNotificationPermission()
        registerFCMToken()

        // Ambil referensi bottom navigation view dari layout
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

        // Set listener untuk menangani klik pada item bottom navigation
        bottomNav.setOnItemSelectedListener { item ->
            // Clear back stack when navigating to main tabs
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            
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
            showBackButton(false)
            true // Return true untuk menandakan item telah diproses
        }

        // Setup nav header buttons
        setupNavHeader()
        
        // Jika activity baru dibuat (bukan dari rotation/config change),
        // tampilkan HomeFragment sebagai fragment default
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
                
            // Check if first time user and show tutorial
            checkAndShowTutorial()
        }
    }
    
    private fun setupNavHeader() {
        val navBackButton = findViewById<android.view.View>(R.id.nav_back_button)
        
        navBackButton.setOnClickListener {
            supportFragmentManager.popBackStack()
        }
        
        findViewById<android.view.View>(R.id.guide_button).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, com.example.datingapp.fragments.GuideFragment.newInstance())
                .addToBackStack(null)
                .commit()
            showBackButton(true)
        }
        
        findViewById<android.view.View>(R.id.settings_button).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, com.example.datingapp.fragments.SettingsFragment.newInstance())
                .addToBackStack(null)
                .commit()
            showBackButton(true)
        }
        
        // Listen for back stack changes
        supportFragmentManager.addOnBackStackChangedListener {
            showBackButton(supportFragmentManager.backStackEntryCount > 0)
        }
    }
    
    fun showBackButton(show: Boolean) {
        findViewById<android.view.View>(R.id.nav_back_button).visibility = 
            if (show) android.view.View.VISIBLE else android.view.View.GONE
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
    
    private fun checkAndShowTutorial() {
        val prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean("first_time_user", true)
        
        if (isFirstTime) {
            // Mark as not first time
            prefs.edit().putBoolean("first_time_user", false).apply()
            
            // Show tutorial after a short delay
            findViewById<android.view.View>(R.id.fragment_container).postDelayed({
                showTutorial()
            }, 500)
        }
    }
    
    private fun showTutorial() {
        val tutorialSteps = listOf(
            TutorialStep("Welcome to UMN Dating App!", "Swipe right to like, left to pass on potential matches", R.id.fragment_container),
            TutorialStep("Find Your Matches", "Check your matches and start conversations here", R.id.nav_match_list),
            TutorialStep("Your Profile", "Edit your profile, view likes, and manage settings", R.id.nav_profile),
            TutorialStep("Need Help?", "Tap the guide button anytime for help", R.id.guide_button)
        )
        
        showTutorialStep(tutorialSteps, 0)
    }
    
    private fun showTutorialStep(steps: List<TutorialStep>, currentStep: Int) {
        if (currentStep >= steps.size) return
        
        val step = steps[currentStep]
        val targetView = findViewById<android.view.View>(step.targetViewId)
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(step.title)
            .setMessage(step.description)
            .setPositiveButton(if (currentStep == steps.size - 1) "Got it!" else "Next") { _, _ ->
                showTutorialStep(steps, currentStep + 1)
            }
            .setNegativeButton("Skip") { _, _ -> }
            .create()
            
        dialog.show()
    }
    
    data class TutorialStep(
        val title: String,
        val description: String,
        val targetViewId: Int
    )
}
