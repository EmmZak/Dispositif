package com.app.app

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import android.Manifest
import android.R.attr
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telephony.SmsManager
import java.lang.Exception
import java.time.format.DateTimeFormatter
import java.util.*
import java.time.LocalDate
import java.time.LocalDateTime
import android.telecom.TelecomManager
import android.content.ComponentName
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import com.app.app.service.CallService
import com.app.app.service.SmsService
import com.app.app.utils.TTS
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import android.view.Gravity

import android.widget.TextView
import android.R.attr.duration

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    val number = "0766006439"
    val SENDING = false

    val BUILT_IN_CALL = true
    var db = FirebaseFirestore.getInstance()
    val TAG = "manu"
    var i = 0
    var configListener: ListenerRegistration? = null
    var notifListener: ListenerRegistration? = null

    var tts: TextToSpeech? = null

    // services
    val smsService = SmsService(this)
    val callService = CallService(this)
    val ttsService = TTS(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "On create triggered")
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)
        Log.e(TAG, "default engine ${tts!!.defaultEngine}")
        val voice = Voice("en-us-x-sfg#male_2-local", Locale.US, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_NORMAL, false, null)
        tts!!.setVoice(voice)
        Log.e(TAG, "tts voice ${tts!!.voice}")

        //Log.e(TAG, "ON CREATE configLisntener $configListener")
        // setup real time listener
        Log.e(TAG, "CREATING a listener for config")
        configListener = db.collection("apps").document("config1").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            i += 1
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "config1: listener: ${snapshot.data}")
            } else {
                Log.d(TAG, "Current data: null")
            }
        }

        notifListener = db.collection("apps").document("app1").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            i += 1
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "app1: listener: ${snapshot.data}")
                val message = snapshot.data?.get("message")
                tts!!.speak(message as CharSequence?, TextToSpeech.QUEUE_FLUSH, null, "")
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "On start triggered")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "on destroy Removing snapshot")
        configListener?.remove()
        notifListener?.remove()
    }

    override fun onStop() {
        super.onStop()
        notifListener?.remove()
        configListener?.remove()
        Log.e(TAG, "On stop triggered")
    }

    override fun isFinishing(): Boolean {
        return super.isFinishing()
    }

    fun sendOk(view: View) {
        Log.e("manu", "send OK")
        sendSms(number, "Salut, tout va bien")
    }

    fun sendKo(view: View) {
        Log.e("manu", "send KO")
        sendSms(number, "Salut, j'ai un petit souci")
    }

    fun sendSOS(view: View) {
        Log.e("manu", "send SOS")
        sendSms(number, "Alerte SOS \n Mme Dupont, localisation suivante : 12 rue de l'Yser, Raismes 59590")
    }

    fun sendSms(number: String, text: String) {
        var message = ""
        try {
            smsService.sendSms(number, text)
            message = "Notification envoyée"
        } catch(e: Exception) {
            message = "Erreur lors de l'envoi"
        }
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    fun speak(view: View) {
        val bundle = Bundle()
        bundle.putInt("quality", Voice.QUALITY_VERY_HIGH)
        //bundle.putBoolean("", "")
        tts!!.speak("manu N'oubliez pas, de prendre vos médicaments, à 15 heures", TextToSpeech.QUEUE_FLUSH, bundle, "")
    }

    @SuppressLint("MissingPermission")
    fun call(view: View) {
        if (BUILT_IN_CALL) {
            callService.nativeCall(number)
        } else {
            // call
        }
    }

    private fun getAccountHandle(): PhoneAccountHandle? {
        val phoneAccountLabel = BuildConfig.APPLICATION_ID
        val componentName = ComponentName(this, MyConnectionService::class.java)
        return PhoneAccountHandle(componentName, phoneAccountLabel)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.FRENCH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG,"The Language specified is not supported!")
            }
        } else {
            Log.e(TAG, "Initilization Failed!")
        }
    }

}