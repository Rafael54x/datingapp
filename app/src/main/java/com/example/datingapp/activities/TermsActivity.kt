package com.example.datingapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.R
import com.example.datingapp.fragments.TermsConditionsFragment

class TermsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)
        
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TermsConditionsFragment.newInstance())
                .commit()
        }
        
        findViewById<android.view.View>(R.id.back_button).setOnClickListener {
            finish()
        }
    }
}