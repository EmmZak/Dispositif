package com.app.app.service

import android.util.Log
import com.app.app.db.FcmRepository
import com.app.app.dto.EventObject
import com.app.app.enums.FcmEventType
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

    override fun onMessageReceived(fcmObject: RemoteMessage) {
        super.onMessageReceived(fcmObject)
        Log.e(TAG, "message.data ${fcmObject.data}")

        val data = hashMapOf(
            "message" to fcmObject.data["message"]
        )
        val action = FcmEventType.values().first {it.name == fcmObject.data["action"]}
        Log.e(TAG, "action $action")

        val o = EventObject(action, fcmObject.data)
        EventBus.getDefault().post(o)
    }
}