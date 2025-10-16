package com.example.datingapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.R
import com.example.datingapp.adapters.MatchAdapter
import com.example.datingapp.utils.DummyData

class MatchListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_match_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.matches_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val matches = DummyData.getMatchesForLoggedIn()

        // Set up the adapter with two click listeners
        recyclerView.adapter = MatchAdapter(
            matches = matches,
            onItemClicked = { user ->
                // When a match item is clicked, navigate to ChatFragment
                val chatFragment = ChatFragment.newInstance(user.username!!)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onPhotoClicked = { user ->
                // When a photo is clicked, navigate to ProfileViewFragment
                val profileViewFragment = ProfileViewFragment.newInstance(user.uid)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileViewFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        return view
    }
}
