package com.example.datingapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.datingapp.R
import com.example.datingapp.utils.SharedPrefManager

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val sharedPrefManager = SharedPrefManager(requireContext())
        val user = sharedPrefManager.getUser()

        val nameTextView = view.findViewById<TextView>(R.id.profile_name)
        nameTextView.text = user?.name

        return view
    }
}
