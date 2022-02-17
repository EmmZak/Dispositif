package com.app.app

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import android.Manifest
import android.annotation.SuppressLint
import android.telecom.PhoneAccountHandle
import java.lang.Exception
import java.util.*
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import com.app.app.service.call.CallService
import com.app.app.service.SmsService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

import android.media.MediaRecorder
import androidx.annotation.RequiresApi
import com.app.app.dialog.AudioRecordingDialog
import java.io.IOException

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe

import android.os.*
import android.widget.ImageView
import android.content.Intent
import android.telecom.Call

import android.telecom.TelecomManager
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.app.app.dialog.call.InCallDialog
import com.app.app.dialog.call.IncomingCallDialog
import com.app.app.dialog.call.OutgoingCallDialog
import com.app.app.dto.EventObject
import com.app.app.dto.EventType
import com.app.app.service.call.OngoingCall


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    var number1 = "0766006439"
    var number2 = "0621585966"
    val numbers = arrayOf(number2, number1)

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
    var FILE_NAME : String = ""
    var OUTPUT_DIR : String = ""
    val pop = AudioRecordingDialog()

    var tts: TextToSpeech? = null

    // services
    val smsService = SmsService(this)
    val callService = CallService()
    //val ttsService = TTS(this)

    // call
    var callDialog : DialogFragment? = null

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
        //filename = "${externalCacheDir?.absolutePath}/vocal.3gp"
        FILE_NAME = "audio.3gp"
        // Android/data/com.app.app/cache/audio.3gp
        OUTPUT_DIR = externalCacheDir?.absolutePath.toString()
        OUTPUT_DIR = getExternalFilesDir(null)?.absolutePath.toString()
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "On start before replaceDefaultDialer")
        offerReplacingDefaultDialer()
        Log.e(TAG, "On start after replaceDefaultDialer")

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

    @RequiresApi(Build.VERSION_CODES.S)
    fun sendOk(view: View) {
        Log.e("manu", "send OK")
        sendSms(numbers, "Salut, tout va bien")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun sendKo(view: View) {
        Log.e("manu", "send KO")
        sendSms(numbers, "Salut, j'ai un petit souci")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun sendSOS(view: View) {
        Log.e("manu", "send SOS")
        sendSms(numbers, "Alerte SOS \n Mme Dupont, localisation suivante : 12 rue de l'Yser, Raismes 59590")
    }

    fun sendSms(number: Array<String>, text: String) {
        var message = ""
        try {
            Log.e(TAG, "build version ${Build.VERSION.SDK_INT}")
            smsService.sendSms(number, text)
            message = "Notification envoyée"
            tts!!.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")

            var vibrator: Vibrator? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
            } else {
                vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(1000, 255))
        } catch(e: Exception) {
            message = "Erreur lors de l'envoi ou bien $e"
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
        Log.e(TAG, "call ")
        if (PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            val uri = "tel:0621585966".toUri()
            Log.e(TAG, "starting ACTION CALL activiry wioth uri ${uri.toString()}")
            startActivity(Intent(Intent.ACTION_CALL, uri))
            //openCallDialog(null)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_PERMISSION
            )
        }
    }

    fun openCallDialog(view: View?) {
        Log.e(TAG, "open call dialog")
        //val dialog = CallDialog.newInstance("Emmanuel", "Appel en cours avec")
        callDialog = InCallDialog.newInstance("Emmanuel", "En appel avec")
        if (callDialog == null) {
            Log.e(TAG, "dialog is null")
            return
        }
        (callDialog as InCallDialog).isCancelable = false
        (callDialog as InCallDialog).show(supportFragmentManager, "call dialog")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCallEvent(eventObject: EventObject) {

        callDialog?.dismiss()

        if (eventObject.type == EventType.CALL) {
            val state = Integer.parseInt(eventObject.data["state"].toString())
            Log.e(TAG, "received call event $state")

            // 1
            if (state == Call.STATE_DIALING) {
                Log.e(TAG, "Dialing ...")
                callDialog = OutgoingCallDialog.newInstance("Emmanuel", "Appel en cours")
                if (callDialog == null) {
                    Log.e(TAG, "dialog is null")
                    return
                }
                (callDialog as OutgoingCallDialog).isCancelable = false
                (callDialog as OutgoingCallDialog).show(supportFragmentManager, "call dialog")
            }
            /* 9
            if (state == Call.STATE_CONNECTING) {
                Log.e(TAG, "Dialing ...")
                callDialog = OutgoingCallDialog.newInstance("Emmanuel", "Appel en cours")
                if (callDialog == null) {
                    Log.e(TAG, "dialog is null")
                    return
                }
                (callDialog as OutgoingCallDialog).show(supportFragmentManager, "call dialog")
            } */
            // 7
            if (state == Call.STATE_DISCONNECTED) {
                Log.e(TAG, "Closing ...")
            }
            // 2
            if (state == Call.STATE_RINGING) {
                Log.e(TAG, "Incoming ...")
                callDialog = IncomingCallDialog.newInstance("Emmanuel", "Appel entrant")
                if (callDialog == null) {
                    Log.e(TAG, "dialog is null")
                    return
                }
                (callDialog as IncomingCallDialog).isCancelable = false
                (callDialog as IncomingCallDialog).show(supportFragmentManager, "call dialog")
            }
            // 4
            if (state == Call.STATE_ACTIVE) {
                Log.e(TAG, "In Call ...")
                callDialog = InCallDialog.newInstance("Emmanuel", "En appel avec")
                if (callDialog == null) {
                    Log.e(TAG, "dialog is null")
                    return
                }
                (callDialog as InCallDialog).isCancelable = false
                (callDialog as InCallDialog).show(supportFragmentManager, "call dialog")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventObject: EventObject) {
        if (eventObject.type == EventType.TextToSpeech) {
            val text = eventObject.data["message"].toString()
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    companion object {
        const val REQUEST_PERMISSION = 0
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun record(view: View) {
        Log.e(TAG, "into record")
        if (isRecording) {
            Log.e(TAG, "already recording !!!!")
            return
        }
        val micro = findViewById<ImageView>(R.id.microView)
        micro.setColorFilter(Color.BLUE)

        Log.e(TAG, "starting recording ...")
        startRecording()
        isRecording = true

        Handler().postDelayed({
            Log.e(TAG, "stopped recording")
            micro.setColorFilter(Color.DKGRAY)
            isRecording = false
            stopRecording()
        }, 5000)
    }

    fun startRecording() {
        Log.e(TAG, "startRecording()")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("$OUTPUT_DIR/$FILE_NAME")

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
            smsService.sendMms(number1, OUTPUT_DIR, FILE_NAME)
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

    private fun offerReplacingDefaultDialer() {
        try {
            Log.e(TAG, "before")
            val default = getSystemService(TelecomManager::class.java).defaultDialerPackage
            Log.e(TAG, "default vs package, $default vs $packageName")
            if (default != packageName) {
                Log.e(TAG, "inside")
                Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                    .let(::startActivity)
            }
            Log.e(TAG, "after")
        } catch(e: Exception) {
            Log.e(TAG, "exception while defaulting $e")
        }
    }

}