package com.example.datingapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.datingapp.R
import com.example.datingapp.fragments.HomeFragment
import com.example.datingapp.fragments.MatchListFragment
import com.example.datingapp.fragments.ProfileFragment
import com.example.datingapp.utils.SharedPrefManager
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPrefManager = SharedPrefManager(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.main)
        val nav = findViewById<NavigationView>(R.id.nav_view)
        nav.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
            nav.setCheckedItem(R.id.nav_home)
        }

        val callback = object : OnBackPressedCallback(false) { // Initially disabled
            override fun handleOnBackPressed() {
                drawer.closeDrawer(GravityCompat.START)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                callback.isEnabled = true
            }

            override fun onDrawerClosed(drawerView: View) {
                callback.isEnabled = false
            }
        })
    }

    @Suppress("DEPRECATION")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_chat -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MatchListFragment()).commit()
            R.id.nav_profile -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
            R.id.nav_logout -> {
                sharedPrefManager.clear()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
