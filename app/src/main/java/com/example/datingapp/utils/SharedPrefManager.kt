package com.example.datingapp.utils

import android.content.Context
import com.example.datingapp.models.User
import com.google.gson.Gson

// Class untuk mengelola SharedPreferences (penyimpanan lokal)
class SharedPrefManager(private val context: Context) {

    // SharedPreferences instance untuk menyimpan data
    private val pref = context.getSharedPreferences("DatingApp", Context.MODE_PRIVATE)

    // Editor untuk menulis data ke SharedPreferences
    private val editor = pref.edit()

    // Gson untuk convert object ke JSON dan sebaliknya
    private val gson = Gson()

    // Simpan data user ke SharedPreferences
    fun saveUser(user: User) {
        // Convert User object menjadi JSON string
        val userJson = gson.toJson(user)

        // Simpan JSON string ke SharedPreferences dengan key "user_json"
        editor.putString("user_json", userJson)

        // Apply perubahan (async)
        editor.apply()

        // Update variabel static loggedInUser
        loggedInUser = user
    }

    // Ambil data user dari SharedPreferences
    fun getUser(): User? {
        // Ambil JSON string dari SharedPreferences
        val userJson = pref.getString("user_json", null)

        // Jika ada data, convert JSON string kembali ke User object
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null // Return null jika tidak ada data
        }
    }

    // Proses login: cek email dan password
    fun login(email: String, pass: String): Boolean {
        // Cari user dengan email yang sesuai di DummyData
        val user = DummyData.users.find { it.email == email }

        // Jika user ditemukan dan password cocok
        return if (user != null && user.password == pass) {
            // Simpan user yang login
            saveUser(user)
            true // Login berhasil
        } else {
            false // Login gagal
        }
    }

    // Tambahkan like ke user lain
    fun addLike(likedUserId: String) {
        // Ambil data user yang sedang login
        val currentUser = getUser()

        // Tambahkan likedUserId ke list likes
        currentUser?.likes?.add(likedUserId)

        // Simpan kembali user yang sudah diupdate
        if (currentUser != null) {
            saveUser(currentUser)
        }
    }

    // Cek apakah terjadi mutual match (keduanya saling like)
    fun isMatch(likedUserId: String): Boolean {
        // Cari user yang di-like
        val otherUser = DummyData.users.find { it.uid == likedUserId }

        // Return true jika user lain juga like kita
        return otherUser?.likes?.contains(getUser()?.uid) == true
    }

    // Daftarkan user baru ke DummyData
    fun register(user: User) {
        // Tambahkan user ke list users di DummyData
        DummyData.users.add(user)
    }

    // Hapus semua data dari SharedPreferences (untuk logout)
    fun clear() {
        // Hapus semua data
        editor.clear()

        // Apply perubahan
        editor.apply()

        // Reset variabel static
        loggedInUser = null
    }

    // Cek apakah user sudah login
    fun isLoggedIn(): Boolean {
        return getUser() != null
    }

    // Ambil user yang sedang login
    fun getCurrentUser(): User? {
        return getUser()
    }

    // Ambil daftar matches untuk user yang sedang login
    fun getMatches(): List<Pair<com.example.datingapp.models.Match, User>> {
        val currentUser = getUser() ?: return emptyList()
        val matches = mutableListOf<Pair<com.example.datingapp.models.Match, User>>()

        // Cari user yang saling like (mutual match)
        DummyData.users.forEach { otherUser ->
            if (otherUser.uid != currentUser.uid) {
                val welikeThem = currentUser.likes.contains(otherUser.uid)
                val theyLikeUs = otherUser.likes.contains(currentUser.uid)

                if (welikeThem && theyLikeUs) {
                    val match = com.example.datingapp.models.Match(
                        matchId = "${currentUser.uid}_${otherUser.uid}",
                        user1 = currentUser.uid,
                        user2 = otherUser.uid,
                        users = listOf(currentUser.uid, otherUser.uid)
                    )
                    matches.add(Pair(match, otherUser))
                }
            }
        }

        return matches
    }

    companion object {
        // Variabel static untuk menyimpan user yang sedang login
        // Bisa diakses dari mana saja tanpa perlu instance SharedPrefManager
        var loggedInUser: User? = null
    }
}