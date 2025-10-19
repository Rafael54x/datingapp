package com.example.datingapp.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.adapters.CardStackAdapter
import com.example.datingapp.models.User
import com.example.datingapp.utils.DummyData
import com.example.datingapp.utils.SharedPrefManager
import com.yuyakaido.android.cardstackview.*

class HomeFragment : Fragment(), CardStackListener {

    // Variabel untuk mengelola card stack (swipe cards)
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var cardStackView: CardStackView
    private lateinit var layoutManager: CardStackLayoutManager
    private lateinit var adapter: CardStackAdapter
    private var users: List<User> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inisialisasi SharedPrefManager
        sharedPrefManager = SharedPrefManager(requireContext())

        // Ambil user yang sedang login
        val loggedInUser = sharedPrefManager.getUser()

        // Filter users: tampilkan semua kecuali user yang sedang login
        users = DummyData.users.filter { it.uid != loggedInUser?.uid }

        // Ambil referensi CardStackView dari layout
        cardStackView = view.findViewById(R.id.card_stack_view)

        // Buat adapter dengan list users
        adapter = CardStackAdapter(users)

        // Setup layout manager untuk card stack
        layoutManager = CardStackLayoutManager(requireContext(), this).apply {
            // Set metode swipe: otomatis dan manual
            setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
            // Set interpolator untuk animasi overlay
            setOverlayInterpolator(LinearInterpolator())
        }

        // Set layout manager dan adapter ke CardStackView
        cardStackView.layoutManager = layoutManager
        cardStackView.adapter = adapter

        // Setup tombol like - swipe otomatis ke kanan
        view.findViewById<ImageButton>(R.id.like_button).setOnClickListener {
            cardStackView.swipe()
        }

        // Setup tombol dislike - swipe otomatis (arah default)
        view.findViewById<ImageButton>(R.id.dislike_button).setOnClickListener {
            cardStackView.swipe()
        }

        // Setup tombol inspect - lihat profile detail user
        view.findViewById<ImageButton>(R.id.inspect_button).setOnClickListener {
            // Ambil posisi card teratas
            val position = layoutManager.topPosition

            // Pastikan posisi valid
            if (position < users.size) {
                val user = users[position]

                // Buat ProfileViewFragment dengan user ID
                val profileView = ProfileViewFragment.newInstance(user.uid)

                // Navigate ke ProfileViewFragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileView)
                    .addToBackStack(null) // Tambahkan ke back stack agar bisa back
                    .commit()
            }
        }

        return view
    }

    // Dipanggil saat card di-swipe
    override fun onCardSwiped(direction: Direction?) {
        // Jika swipe ke kanan (like)
        if (direction == Direction.Right) {
            // Ambil posisi card yang baru saja di-swipe (topPosition - 1)
            val position = layoutManager.topPosition - 1

            // Pastikan posisi valid
            if (position < users.size) {
                val likedUser = users[position]

                // Simpan like ke SharedPreferences
                sharedPrefManager.addLike(likedUser.uid)

                // Tampilkan dialog match
                showMatchDialog(likedUser)
            }
        }
    }

    // Tampilkan dialog ketika terjadi match
    private fun showMatchDialog(matchedUser: User) {
        // Buat dialog custom
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_match)

        // Set background transparan
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set ukuran dialog
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Ambil ImageView untuk foto user yang match
        val userImageView = dialog.findViewById<ImageView>(R.id.match_user_image)

        // Load foto user menggunakan Glide
        matchedUser.photoUrl?.let {
            Glide.with(requireContext()).load(it).into(userImageView)
        }

        // Setup tombol "Start Chatting"
        dialog.findViewById<Button>(R.id.start_chatting_button).setOnClickListener {
            // Tutup dialog
            dialog.dismiss()

            // Buat ChatFragment dengan data user yang match
            val chatFragment = ChatFragment.newInstance(
                matchedUser.uid,
                matchedUser.name ?: "",
                matchedUser.photoUrl ?: ""
            )

            // Navigate ke ChatFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit()
        }

        // Tampilkan dialog
        dialog.show()
    }

    // Callback methods dari CardStackListener (tidak digunakan tapi harus diimplementasi)
    override fun onCardDragging(direction: Direction?, ratio: Float) {
        // Dipanggil saat card sedang di-drag
    }

    override fun onCardRewound() {
        // Dipanggil saat card di-rewind (undo swipe)
    }

    override fun onCardCanceled() {
        // Dipanggil saat swipe dibatalkan
    }

    override fun onCardAppeared(view: View?, position: Int) {
        // Dipanggil saat card muncul
    }

    override fun onCardDisappeared(view: View?, position: Int) {
        // Dipanggil saat card menghilang
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}