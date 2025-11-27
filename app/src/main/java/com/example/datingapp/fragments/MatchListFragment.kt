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
import com.example.datingapp.utils.SharedPrefManager

class MatchListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment
        val view = inflater.inflate(R.layout.fragment_match_list, container, false)

        // Inisialisasi SharedPrefManager
        val sharedPrefManager = SharedPrefManager(requireContext())

        // Ambil data user yang sedang login
        val loggedInUser = sharedPrefManager.getUser()

        // Setup RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.matches_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Filter users: tampilkan semua user kecuali diri sendiri
        val users = DummyData.users.filter { it.uid != loggedInUser?.uid }

        // Setup adapter dengan dua callback:
        // 1. onItemClicked - saat item (seluruh row) diklik -> buka chat
        // 2. onPhotoClicked - saat foto diklik -> lihat profile
        recyclerView.adapter = MatchAdapter(
            matches = users,

            // Callback ketika item diklik - navigasi ke ChatFragment
            onItemClicked = { user ->
                val chatFragment = ChatFragment.newInstance(
                    user.uid,
                    user.name ?: "",
                    user.photoUrl ?: ""
                )

                // Ganti fragment dan tambahkan ke back stack
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null)
                    .commit()
            },

            // Callback ketika foto diklik - navigasi ke ProfileViewFragment
            onPhotoClicked = { user ->
                val profileViewFragment = ProfileViewFragment.newInstance(user.uid)

                // Ganti fragment dan tambahkan ke back stack
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileViewFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        return view
    }
}