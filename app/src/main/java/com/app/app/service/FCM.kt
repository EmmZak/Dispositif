package com.app.app.service

import android.util.Log
import com.app.app.dto.EventObject
import com.app.app.dto.EventType
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.greenrobot.eventbus.EventBus

data class FCMMessage(val message: Map<String, String>)

class FCM: FirebaseMessagingService() {

    private val TAG = "FireBaseMessagingService manu"
    var NOTIFICATION_CHANNEL_ID = "com.example.secunotif2"
    val NOTIFICATION_ID = 100
    val db = Firebase.firestore

    override fun onNewToken(token: String) {
        Log.e(TAG, "Refreshed token: $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.e(TAG, "${message.data}")

        val data = hashMapOf(
            "message" to message.data["message"]
        )
        val o = EventObject(EventType.TextToSpeech, data as Map<String, Object>)
        EventBus.getDefault().post(o)
    }

    fun saveToken(token: String) {
        val data = hashMapOf(
            "token" to token
        )
        db.collection("apps")
            .document("communication")
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.e("TOKEN", "token saved")
            }.addOnFailureListener {e ->
                Log.e("TOKEN", "error saving token", e)
            }
    }

}