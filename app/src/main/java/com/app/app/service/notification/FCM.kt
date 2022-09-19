package com.app.app.service

import android.util.Log
import com.app.app.db.FcmRepository
import com.app.app.dto.EventObject
import com.app.app.dto.FcmObject
import com.app.app.dto.FcmObjectData
import com.app.app.enums.EventType
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.lang.Exception
import java.util.*

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

        val event = EventType.values().first {it.name == fcmObject.data["event"]}
        Log.e(TAG, "event $event")


        val o = EventObject(event, fcmObject.data)
        EventBus.getDefault().post(o)
    }

    // custom
    companion object {
        const val TAG = "FireBaseMessagingService manu"
        private val httpClient = OkHttpClient()

        fun sendFCM(token: String) {
            val data = FcmObjectData()
            data.message = "Notification reçue"
            data.date = Date() //Utils.getFormattedDateTime()
            data.event = EventType.NOTIFICATION_SUCCESS

            sendFCM(token, data)
        }

        fun sendFCM(token: String, data: FcmObjectData) {
            //val json = JSONObject()
            val fcmObject = FcmObject()
            //json.put("to", token)
            //json.put("data", data)
            fcmObject.to = token
            fcmObject.data = data

            val gson = Gson()
            val json = gson.toJson(fcmObject)
            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .header(
                    "Authorization",
                    "key=AAAAw0zVm_U:APA91bEP2ISoe6EwgN3FO5CNuqgYwiYNWH_XF9GbJUM3ijWbsnlfiLsiX3eK5W9ODkcCBtce5iVV9kUBfT3wI2__WbhCDEuoiwgCt6cV-m2OV5kfgXc5ROiqHLGv7VA5rXGEgUIQCE1P"
                )
                .header("Content-Type", "application/json")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build()

            val thread = Thread {
                Log.e(TAG, "sending notif")
                try {
                    val response = httpClient.newCall(request).execute()
                    Log.e(TAG, response.toString())
                    Log.e(TAG, "FCM response $response")
                } catch (e: Exception) {
                    Log.e("err", e.toString())
                    throw e
                }
            }
            thread.start()
        }

    }
}