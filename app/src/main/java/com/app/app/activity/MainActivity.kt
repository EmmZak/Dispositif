package com.app.app.activity

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import java.lang.Exception
import java.util.*
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import com.app.app.service.call.CallService
import com.app.app.service.sms.SmsService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

import android.media.MediaRecorder
import androidx.annotation.RequiresApi
import com.app.app.dialog.audio.AudioRecordingDialog
import java.io.IOException

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe

import android.os.*
import android.widget.ImageView
import android.content.Intent
import android.location.Location
import android.media.CamcorderProfile.getAll
import android.telecom.Call

import android.telecom.TelecomManager
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import com.app.app.R
import com.app.app.dialog.call.CallDialog
import com.app.app.dto.EventObject
import com.app.app.dto.EventType
import com.app.app.exception.SmsException
import com.app.app.service.call.Contact
import com.app.app.service.gps.GpsService
import com.app.app.service.permission.AppPermissionService
import com.app.app.service.vibrator.VibratorService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    val MESSAGE_OK = "Salut, tout va bien"
    val MESSAGE_KO = "Salut, j'ai un souci"
    var MESSAGE_SOS = "Alerte SOS \n Dispositif, localisation suivante : "

    val SENDING = false

    val TAG = "manu"

    // audio recording
    var isRecording = false
    //var recorder: MediaRecorder? = null
    //var FILE_NAME : String = ""
    //var OUTPUT_DIR : String = ""
    //val pop = AudioRecordingDialog()

    var tts: TextToSpeech? = null

    // services
    var smsService: SmsService? = null
    var callService: CallService? = null
    var vibratorService: VibratorService? = null
    var gpsService: GpsService? = null

    // call
    var callDialog : CallDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //window.setFlags(android.R.attr.windowFullscreen, android.R.attr.windowFullscreen )
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this,
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
                //Manifest.permission.RECORD_AUDIO,
                //Manifest.permission.READ_EXTERNAL_STORAGE,
                //Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 0);
        if (!isDefaultDialerService()) {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                .let(::startActivity)
        }

        tts = TextToSpeech(this, this)
        Log.e(TAG, "default engine ${tts!!.defaultEngine}")
        //val voice = Voice("en-us-x-sfg#male_2-local", Locale.US, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_NORMAL, false, null)
        //tts!!.setVoice(voice)

        //filename = "${externalCacheDir?.absolutePath}/vocal.3gp"
        //FILE_NAME = "audio.3gp"
        // Android/data/com.app.app/cache/audio.3gp
        //OUTPUT_DIR = externalCacheDir?.absolutePath.toString()
        //OUTPUT_DIR = getExternalFilesDir(null)?.absolutePath.toString()

        // call card on click TABLETT
        val TABLET_MODE = true
        if (TABLET_MODE) {
            findViewById<LinearLayout>(R.id.callCard1).setOnClickListener {
                call(Contact.Emmanuel.number)
            }
            findViewById<LinearLayout>(R.id.callCard2).setOnClickListener {
                call(Contact.Alexandre.number)
            }
        } else {
            findViewById<ImageView>(R.id.callCard1).setOnClickListener {
                call(Contact.Emmanuel.number)
            }
            findViewById<ImageView>(R.id.callCard2).setOnClickListener {
                call(Contact.Alexandre.number)
            }
        }

        callService = CallService()
        smsService = SmsService(this)
        vibratorService = VibratorService(this)
        gpsService = GpsService(this)

        val numbers = Contact.getAll()
        Log.e(TAG, "numbers ${numbers[0]}")
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "On start before replaceDefaultDialer")
        offerReplacingDefaultDialer()
        Log.e(TAG, "On start after replaceDefaultDialer")

        EventBus.getDefault().register(this);
    }

    private fun setupPermissions() {
        // sms
        Log.e(TAG, "check sms permission")
        if (AppPermissionService.isSmsPermissionGranted(this)) {
            Log.e(TAG, "sms permission not granted")
            AppPermissionService.requestSmsPermission(this)
        }
        // call
        if (AppPermissionService.isPhoneCallGranted(this)) {
            AppPermissionService.requestPhoneCallPermission(this)
        }
        /*
        // audio
        if (AppPermissionService.isRecordAudioPermissionGranted(this)) {
            AppPermissionService.requestRecordAudioPermission(this)
        }
        // storage read
        if (AppPermissionService.isStorageReadPermissionGranted(this)) {
            AppPermissionService.requestStorageReadPermission(this)
        }
        // storage write
        if (AppPermissionService.isStorageWritePermissionGranted(this)) {
            AppPermissionService.requestStorageWritePermission(this)
        }
         */
    }

    override fun isFinishing(): Boolean {
        return super.isFinishing()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun sendOk(@Suppress("UNUSED_PARAMETER") view: View) {
        Log.e("manu", "send OK")
        sendSms(Contact.getAll(), MESSAGE_OK)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun sendKo(@Suppress("UNUSED_PARAMETER") view: View) {
        Log.e("manu", "send KO")
        sendSms(Contact.getAll(), MESSAGE_KO)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun sendSOS(@Suppress("UNUSED_PARAMETER") view: View) {
        Log.e(TAG, "send SOS")
        // fetch gps localisation
        gpsService?.getLocation()
            ?.addOnSuccessListener { loc : Location? ->
                Log.e(TAG, "location $loc")
                //val mapUrl = "https://www.google.com/maps/@${loc?.latitude},${loc?.longitude},20z"
                val mapUrl = "https://www.google.com/maps/search/?api=1&query=${loc?.latitude},${loc?.longitude}"
                Log.e(TAG, "mapUrl $mapUrl")
                Log.e(TAG, "res mapUrl $mapUrl")
                sendSms(Contact.getAll(), "$MESSAGE_SOS $mapUrl")
            }
            ?.addOnFailureListener {
                Log.e(TAG, "error")
            }
    }

    fun sendSms(number: Array<String>, text: String) {
        var message = ""
        try {
            smsService?.sendSms(number, text)
            message = "Notification envoyée"
            vibratorService?.vibrate(1000)
        } catch (e: SmsException) {
            Log.e(TAG, "$e")
            message = "Problème lors de l'envoi de la notification"
        }
        tts!!.speak(message, TextToSpeech.QUEUE_ADD, null, "")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("MissingPermission")
    fun call(number: String) {
        Log.e(TAG, "call ")
        if (PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            val uri = "tel:$number".toUri()
            Log.e(TAG, "starting ACTION CALL activity with uri ${uri.toString()}")
            try {
                if (isDefaultDialerService()) {
                    tts!!.speak("L'application n'est pas configurée pour l'appel", TextToSpeech.QUEUE_ADD, null, "")
                } else {
                    startActivity(Intent(Intent.ACTION_CALL, uri))
                }
            } catch (e: Exception) {
                Log.e(TAG, "call e $e")
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_PERMISSION
            )
        }
    }

    fun openCallDialog(@Suppress("UNUSED_PARAMETER") view: View?) {
        Log.e(TAG, "open call dialog")
        //val dialog = CallDialog.newInstance("Emmanuel", "Appel en cours avec")
/*        callDialog = InCallDialog.newInstance("Emmanuel", "En appel avec")
        if (callDialog == null) {
            Log.e(TAG, "dialog is null")
            return
        }
        (callDialog as InCallDialog).isCancelable = false
        (callDialog as InCallDialog).show(supportFragmentManager, "call dialog")*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(eventObject: EventObject) {
        //Log.e(TAG, "OnEvent event object $eventObject")
        when(eventObject.type) {
            EventType.CALL -> onCallEvent(eventObject)
            EventType.TTS -> onMessageEvent(eventObject)
            EventType.LOCATION -> onLocationEvent(eventObject)
        }
    }

    private fun onCallEvent(eventObject: EventObject) {
        if (eventObject.type == EventType.CALL) {
            val state = Integer.parseInt(eventObject.data["state"].toString())
            Log.e(TAG, "received call state $state")

            // Outgoing call
            if (state == Call.STATE_CONNECTING /* == 9 */) {
                //Log.e(TAG, "Dialing ...")
                callDialog = CallDialog.newInstance("Emmanuel", "Appel en cours ...", 1)
                callDialog!!.isCancelable = false
                (callDialog as CallDialog).show(supportFragmentManager, "call dialog")
            }
            // Incoming call
            if (state == Call.STATE_RINGING /* == 2 */) {
                Log.e(TAG, "Incoming ...")
                callDialog?.dismiss()
                callDialog = CallDialog.newInstance("Emmanuel", "Appel entrant ...", 2)
                callDialog!!.isCancelable = false
                (callDialog as CallDialog).show(supportFragmentManager, "call dialog")
            }
            // Disconnect
            if (state == Call.STATE_DISCONNECTED /* == 7 */) {
                //Log.e(TAG, "Closing ...")
                callDialog?.dismiss()
            }
            // In Call
            if (state == Call.STATE_ACTIVE /* == 4 */) {
                Log.e(TAG, "In Call ...")
                //callDialog = CallDialog.newInstance("Emmanuel", "En appel avec")
                if (callDialog != null) {
                    callDialog!!.updateUI( "Emmanuel", "En appel avec ...", 1)
                } else {
                    //Log.e(TAG, "callDialog is null creating one")
                    callDialog = CallDialog.newInstance("Emmanuel", "En appel avec ...", 1)
                    callDialog!!.isCancelable = false
                    (callDialog as CallDialog).show(supportFragmentManager, "call dialog")
                }
            }
        }
    }

    private fun onMessageEvent(eventObject: EventObject) {
        Log.e(TAG, "onMessageEvent $eventObject")
        if (eventObject.type == EventType.TTS) {
            val text = eventObject.data["message"].toString()
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    private fun onLocationEvent(eventObject: EventObject) {
        Log.e(TAG, "onLocationEvent $eventObject")
        if (eventObject.type == EventType.LOCATION) {
            val text = eventObject.data["message"].toString()
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")

            val res = gpsService?.getLocation()
            Log.e(TAG, "res $res")
            gpsService?.getLocation()
                ?.addOnSuccessListener { loc : Location? ->
                    Log.e(TAG, "location $loc")
                    //val mapUrl = "https://www.google.com/maps/@${loc?.latitude},${loc?.longitude},20z"

                    val mapUrl = "https://www.google.com/maps/search/?api=1&query=${loc?.latitude},${loc?.longitude}"
                    Log.e(TAG, "mapUrl $mapUrl")
                    Log.e(TAG, "res mapUrl $mapUrl")
                    sendSms(Contact.getAll(), "$MESSAGE_SOS $mapUrl")
                }
                ?.addOnFailureListener {
                    Log.e(TAG, "error")
                }
        }
    }

    companion object {
        const val REQUEST_PERMISSION = 0
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun record(@Suppress("UNUSED_PARAMETER") view: View) {
/*        Log.e(TAG, "into record")
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
        }, 5000)*/
    }

/*    private fun startRecording() {
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
    }*/

    fun stopRecording() {
/*        Log.e(TAG, "stop recording")
        recorder?.apply {
            stop()
            reset()
            release()
        }
        recorder = null

        try {
            //smsService?.sendMms(number1, OUTPUT_DIR, FILE_NAME)
        } catch(e: Exception) {
            Log.e(TAG, "$e")
        }*/
    }
    // check audio permission

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

    private fun isDefaultDialerService(): Boolean {
        return getSystemService(TelecomManager::class.java).defaultDialerPackage != packageName
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "on destroy Removing snapshot")
    }

    override fun onStop() {
        super.onStop()
        //recorder?.release()
        //recorder = null
        EventBus.getDefault().unregister(this);
        Log.e(TAG, "On stop triggered")
    }
}