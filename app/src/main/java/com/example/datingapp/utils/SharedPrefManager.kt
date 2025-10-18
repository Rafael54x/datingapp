package com.example.datingapp.utils

import android.content.Context
import com.example.datingapp.models.User
import com.google.gson.Gson

class SharedPrefManager(private val context: Context) {

    private val pref = context.getSharedPreferences("DatingApp", Context.MODE_PRIVATE)
    private val editor = pref.edit()
    private val gson = Gson()

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        editor.putString("user_json", userJson)
        editor.apply()
        loggedInUser = user
    }

    fun getUser(): User? {
        val userJson = pref.getString("user_json", null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
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