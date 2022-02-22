package com.app.app.db

import android.util.Log
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FcmRepository {

    private val TAG = "fcmRepository manu"
    private val db = Firebase.firestore

    fun saveToken(token: String) {
        val data = hashMapOf(
            "token" to token
        )
        Log.e(TAG, "saving fcm token $token")
        db.collection("apps")
            .document("communication")
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(TAG, "token saved")
            }.addOnFailureListener {e ->
                Log.e(TAG, "error saving token", e)
            }
    }
}