package com.app.app.service

import android.app.Service
import android.speech.tts.TextToSpeech
import android.util.Log
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.greenrobot.eventbus.EventBus
import java.util.*

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
        //tts!!.speak(message as CharSequence?, TextToSpeech.QUEUE_FLUSH, null, "")
        EventBus.getDefault().post(message.data["message"])
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