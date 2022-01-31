package com.app.app

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.telecom.PhoneAccountHandle
import java.lang.Exception
import java.util.*
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

import android.media.MediaRecorder
import androidx.annotation.RequiresApi
import com.app.app.databinding.ActivityMainBinding
import com.app.app.dialog.AudioRecordingDialog
import java.io.File
import java.io.IOException

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import androidx.databinding.DataBindingUtil
import com.app.app.service.FCMMessage


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    val number = "0766006439"
    val SENDING = false

    val BUILT_IN_CALL = true
    var db = FirebaseFirestore.getInstance()
    val TAG = "manu"
    var i = 0
    var configListener: ListenerRegistration? = null
    var notifListener: ListenerRegistration? = null

    // audio recording
    var isRecording = false
    var recorder: MediaRecorder? = null
    var filename : String = ""
    val pop = AudioRecordingDialog()

    var tts: TextToSpeech? = null

    // services
    val smsService = SmsService(this)
    val callService = CallService(this)
    //val ttsService = TTS(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "On create triggered")
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)
        Log.e(TAG, "default engine ${tts!!.defaultEngine}")
        val voice = Voice("en-us-x-sfg#male_2-local", Locale.US, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_NORMAL, false, null)
        tts!!.setVoice(voice)
        Log.e(TAG, "tts voice ${tts!!.voice}")

        // audio recording
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE), 0);
        }
        filename = "${externalCacheDir?.absolutePath}/vocal.3gp"

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(message: String) {
        Log.e(TAG, "received event $message")
        tts!!.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "On start triggered")
        EventBus.getDefault().register(this);
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
        recorder?.release()
        recorder = null
        EventBus.getDefault().unregister(this);
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
            Log.e(TAG, "custom call")
            callService.call(number)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun record(view: View) {
        if (!isRecording) {
            isRecording = true
            startRecording()
        } else {
            isRecording = false
            stopRecording()
        }
    }

    fun startRecording() {
        Log.e(TAG, "start recording")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(filename)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed")
            }
            start()
        }
    }

    fun stopRecording() {
        Log.e(TAG, "stop recording")
        recorder?.apply {
            stop()
            reset()
            release()
        }
        recorder = null

        try {
            smsService.sendMms(number)
        } catch(e: Exception) {
            Log.e(TAG, "$e")
        }
    }
    // check audio permission


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