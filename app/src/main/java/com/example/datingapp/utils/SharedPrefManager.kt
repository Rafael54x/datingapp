package com.example.datingapp.utils

import android.content.Context
import com.example.datingapp.models.User

class SharedPrefManager(private val context: Context) {

    private val pref = context.getSharedPreferences("DatingApp", Context.MODE_PRIVATE)
    private val editor = pref.edit()

    fun saveUser(user: User) {
        editor.putString("user_id", user.uid)
        editor.putString("user_name", user.name)
        editor.putString("user_email", user.email)
        editor.putString("user_password", user.password)
        editor.putStringSet("user_likes", user.likes.toSet())
        editor.apply()
        loggedInUser = user
    }

    fun getUser(): User? {
        val uid = pref.getString("user_id", null) ?: return null
        val name = pref.getString("user_name", null)
        val email = pref.getString("user_email", null)
        val password = pref.getString("user_password", null)
        val likes = pref.getStringSet("user_likes", emptySet())?.toMutableList() ?: mutableListOf()
        return User(uid, name, email = email, password = password, likes = likes)
    }

    fun login(email: String, pass: String): Boolean {
        val user = DummyData.users.find { it.email == email }
        return if (user != null && user.password == pass) {
            saveUser(user)
            true
        } else {
            false
        }
    }

    fun addLike(likedUserId: String) {
        val currentUser = getUser()
        currentUser?.likes?.add(likedUserId)
        if (currentUser != null) {
            saveUser(currentUser)
        }
    }

    fun isMatch(likedUserId: String): Boolean {
        val otherUser = DummyData.users.find { it.uid == likedUserId }
        return otherUser?.likes?.contains(getUser()?.uid) == true
    }

    fun register(user: User) {
        DummyData.users.add(user)
    }

    fun clear() {
        editor.clear()
        editor.apply()
        loggedInUser = null
    }

    companion object {
        var loggedInUser: User? = null
    }
}
