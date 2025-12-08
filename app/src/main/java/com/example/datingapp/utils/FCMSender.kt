package com.example.datingapp.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object FCMSender {
    
    private const val TAG = "FCMSender"
    private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
    
    // IMPORTANT: Replace with your Firebase Server Key from Firebase Console
    // Go to: Firebase Console > Project Settings > Cloud Messaging > Server Key
    private const val SERVER_KEY = "YOUR_FIREBASE_SERVER_KEY_HERE"
    
    suspend fun sendNotification(
        token: String,
        title: String,
        body: String
    ) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("to", token)
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                    put("sound", "default")
                })
                put("priority", "high")
            }
            
            val requestBody = json.toString()
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(FCM_URL)
                .addHeader("Authorization", "key=$SERVER_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()
            
            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                Log.d(TAG, "Notification sent successfully")
            } else {
                Log.e(TAG, "Failed to send notification: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
        }
    }
}
