package com.example.datingapp.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object NotificationHelper {
    
    private val TAG = "NotificationHelper"
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun sendLikeNotification(likedUserId: String, likerName: String) {
        try {
            val userDoc = firestore.collection("users").document(likedUserId).get().await()
            val fcmToken = userDoc.getString("fcmToken")
            
            if (fcmToken != null) {
                // In production, call your backend API to send FCM notification
                Log.d(TAG, "Would send like notification to $likedUserId: $likerName liked you!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending like notification", e)
        }
    }
    
    suspend fun sendMatchNotification(userId: String, matchedUserName: String) {
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val fcmToken = userDoc.getString("fcmToken")
            
            if (fcmToken != null) {
                // In production, call your backend API to send FCM notification
                Log.d(TAG, "Would send match notification to $userId: You matched with $matchedUserName!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending match notification", e)
        }
    }
}
