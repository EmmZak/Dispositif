package com.app.app.service

import android.util.Log
import com.app.app.db.FcmRepository
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

    val fcmRepository = FcmRepository()

    override fun onNewToken(token: String) {
        Log.e(TAG, "Refreshed token: $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        fcmRepository.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.e(TAG, "${message.data}")

        val data = hashMapOf(
            "message" to message.data["message"]
        )
        val action = EventType.values().first {it.name == message.data["action"]}
        Log.e(TAG, "action $action")
        val o = EventObject(action, message.data)
        EventBus.getDefault().post(o)
    }
}