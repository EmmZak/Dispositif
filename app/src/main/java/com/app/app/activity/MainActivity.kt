package com.app.app.activity

import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import android.Manifest
import android.annotation.SuppressLint
import java.lang.Exception
import java.util.*
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.app.app.service.call.CallService
import com.app.app.service.sms.SmsService

import androidx.annotation.RequiresApi

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe

import android.os.*
import android.widget.ImageView
import android.content.Intent
import android.location.Location
import android.speech.tts.UtteranceProgressListener
import android.telecom.Call

import android.telecom.TelecomManager
import android.view.WindowManager
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import com.app.app.R
import com.app.app.dialog.call.CallDialog
import com.app.app.dto.EventObject
import com.app.app.exception.SmsException
import com.app.app.service.call.Contact
import com.app.app.service.gps.GpsService
import com.app.app.service.permission.AppPermissionService
import com.app.app.service.vibrator.VibratorService
import android.view.animation.AnimationUtils
import androidx.core.app.NotificationCompat
import com.app.app.config.Config
import com.app.app.config.SMSTemplate
import com.app.app.dto.FcmObjectData
import com.app.app.enums.EventType
import com.app.app.service.EventService
import com.app.app.service.FCM
import com.app.app.utils.Utils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class UtteranceManager: UtteranceProgressListener() {

    var TAG = "UtteranceManager manu"

    override fun onStart(utteranceId: String?) {
        Log.e(TAG, "onStart $utteranceId")
    }

    override fun onDone(utteranceId: String?) {
        Log.e(TAG, "onDone $utteranceId")
    }

    override fun onError(utteranceId: String?) {
        Log.e(TAG, "onError $utteranceId")
    }

}

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    var SENDING = false

    val TAG = "MainActivity manu"

    // audio recording
    var isRecording = false
    //var recorder: MediaRecorder? = null
    //var FILE_NAME : String = ""
    //var OUTPUT_DIR : String = ""
    //val pop = AudioRecordingDialog()
    val httpClient = OkHttpClient()

    /**
     * TTS
     */
    private lateinit var tts: TextToSpeech

    /**
     * services
     */
    var smsService: SmsService? = null
    var callService: CallService? = null
    var vibratorService: VibratorService? = null
    var gpsService: GpsService? = null

    var eventService: EventService? = null

    // call
    var callDialog : CallDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(android.R.attr.windowFullscreen, android.R.attr.windowFullscreen )
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
        //Log.e(TAG, "default engine ${tts!!.defaultEngine}")

        //filename = "${externalCacheDir?.absolutePath}/vocal.3gp"
        //FILE_NAME = "audio.3gp"
        // Android/data/com.app.app/cache/audio.3gp
        //OUTPUT_DIR = externalCacheDir?.absolutePath.toString()
        //OUTPUT_DIR = getExternalFilesDir(null)?.absolutePath.toString()

        // call card on click TABLETT
        setupOnClickListeners()

        callService = CallService()
        smsService = SmsService(this)
        vibratorService = VibratorService(this)
        gpsService = GpsService(this)

        //eventService = EventService(this)

        val numbers = Contact.getAllNumbers()
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

    private fun setupOnClickListeners() {

//        findViewById<ImageView>(R.id.imageViewSos).setOnClickListener { view ->
//            val anim = AnimationUtils.loadAnimation(this, R.anim.zoom_out_notif)
//            view.startAnimation(anim)
//
//            sendNotif(view.tag as String)
//        }

        findViewById<ImageView>(R.id.callIcon1).setOnClickListener { view ->
            val anim = AnimationUtils.loadAnimation(this, R.anim.zoom_out_call)
            view.startAnimation(anim)
            call(Contact.Emmanuel.number)
        }

    }

    private fun sendNotif(tag: String) {
        Log.e(TAG, "tag $tag")
        when(tag) {
            "OK" -> sendSms(Contact.getAllNumbers(), SMSTemplate.OK.text)
            "KO" -> sendSms(Contact.getAllNumbers(), SMSTemplate.KO.text)
            "SOS" -> sendSos()
        }
    }

    fun sendSos() {
        gpsService?.getLocation()
            ?.addOnSuccessListener { loc : Location? ->
                Log.e(TAG, "location $loc")
                val mapUrl = "https://www.google.com/maps/search/?api=1&query=${loc?.latitude},${loc?.longitude}"
                Log.e(TAG, "mapUrl $mapUrl")
                val text1 = java.lang.String.format(SMSTemplate.ALERT.text, Config.NOM, Config.PRENOM)
                Log.e(TAG, "text alert $text1")
                val text2 = String.format(SMSTemplate.SOS.text,
                    Config.NUM_SECU,
                    Config.MEDECIN,
                    mapUrl
                )
                Log.e(TAG, "text $text2")
                sendSms(Contact.getAllNumbers(), text1)
                sendSms(Contact.getAllNumbers(), text2)
            }
            ?.addOnFailureListener {
                Log.e(TAG, "error")
            }
    }

    private fun sendSms(number: Array<String>, text: String) {
        var message: String
        try {
            smsService?.sendSms(number, text)
            message = "Notification envoyée"
            vibratorService?.vibrate(1000)
        } catch (e: SmsException) {
            Log.e(TAG, "$e")
            message = "Problème lors de l'envoi de la notification"
        }

        speak(message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun speak(message: String) {
        try {
            val res = tts.speak(message, TextToSpeech.QUEUE_ADD, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)

            if (res == TextToSpeech.ERROR) {
                Log.e(TAG, "tts speak QUEUE error res=$res")
            } else {
                Log.e(TAG, "tts worked")
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
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
                    //tts!!.speak("L'application n'est pas configurée pour l'appel", TextToSpeech.QUEUE_ADD, null, "")
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
        Log.e(TAG, "onEvent event object $eventObject")
        val token = eventObject.data["token"]

        if (token != null) {
            FCM.sendFCM(eventObject.data["token"] as String)
        }

        when(eventObject.event) {
            EventType.FCM_TTS -> onMessageEvent(eventObject)
            EventType.FCM_LOCATION -> onLocationEvent(eventObject, token as String)
            EventType.FCM_SOS -> onSosEvent(eventObject)
            EventType.FCM_CONFIG -> TODO()
            EventType.FCM_SUBSCRIBE -> TODO()

            EventType.CALL -> onCallEvent(eventObject)
            EventType.NOTIFICATION_SUCCESS -> TODO()
        }


    }

    private fun onTtsError() {

    }

    private fun onSosEvent(eventObject: EventObject) {
        if (eventObject.event == EventType.FCM_SOS) {
            Log.e(TAG, "$eventObject")
            val text = "Une alerte SOS va être envoyée"

            speak(text)

            sendSos()
        }
    }

    private fun onCallEvent(eventObject: EventObject) {
        if (eventObject.event == EventType.CALL) {
            val state = Integer.parseInt(eventObject.data["state"].toString())
            Log.e(TAG, "received call state $state")

            // Outgoing call
            if (state == Call.STATE_CONNECTING /* == 9 */) {
                Log.e(TAG, "Dialing ...")
                callDialog = CallDialog.newInstance("Emmanuel", "Appel en cours ...", 1)
                callDialog!!.isCancelable = false
                (callDialog as CallDialog).show(supportFragmentManager, "call dialog")
            }
            // Incoming call
            if (state == Call.STATE_RINGING /* == 2 */) {
//                Log.e(TAG, "Incoming ...")
//                val replySwitch = findViewById<SwitchCompat>(R.id.autoReplySwitch)
//                Log.e(TAG, "replySWitch $replySwitch")
//
//                if (replySwitch.isChecked) {
//                    try {
//                        CallService.answer()
//                    } catch(e: Exception) {
//                        Log.e(TAG, "$e")
//                    }
//                } else {
//                     callDialog?.dismiss()
//                     callDialog = CallDialog.newInstance("Emmanuel", "Appel entrant ...", 2)
//                     callDialog!!.isCancelable = false
//                     (callDialog as CallDialog).show(supportFragmentManager, "call dialog")
//                }
                callDialog?.dismiss()
                callDialog = CallDialog.newInstance("Emmanuel", "Appel entrant ...", 2)
                callDialog!!.isCancelable = false
                (callDialog as CallDialog).show(supportFragmentManager, "call dialog")
            }
            // Disconnect
            if (state == Call.STATE_DISCONNECTED /* == 7 */) {
                //Log.e(TAG, "Closing ...")
                callDialog?.dismiss()
                callDialog = null
            }
            // In Call
            if (state == Call.STATE_ACTIVE /* == 4 */) {
                Log.e(TAG, "In Call ...")
                //callDialog = CallDialog.newInstance("Emmanuel", "En appel avec")
                if (callDialog != null) {
                    Log.e(TAG, "Updating in call dialog")
                    callDialog!!.updateUI( "Emmanuel", "En appel avec ...", 1)
                } else {
                    Log.e(TAG, "callDialog is null creating one")
                    callDialog = CallDialog.newInstance("Emmanuel", "En appel avec ...", 1)
                    callDialog!!.isCancelable = false
                    (callDialog as CallDialog).show(supportFragmentManager, "call dialog")
                }
            }
        }
    }

    private fun onMessageEvent(eventObject: EventObject) {
        Log.e(TAG, "onMessageEvent $eventObject")
        if (eventObject.event == EventType.FCM_TTS) {
            val text = eventObject.data["message"].toString()
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    private fun onLocationEvent(eventObject: EventObject, token: String) {
        Log.e(TAG, "onLocationEvent $eventObject")
        if (eventObject.event == EventType.FCM_LOCATION) {
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
                    val message = SMSTemplate.LOCATION.text.format(mapUrl)

                    val number = eventObject.data["number"] as String
                    sendSms(arrayOf(number), message)

                    // send notif
                    val data = FcmObjectData()
                    data.message = "Notification reçue"
                    data.date = Date()
                    data.event = EventType.FCM_LOCATION
                    data.mapUrl = mapUrl

                    /*val data = JSONObject()
                    data.put("message", "Notification reçue")
                    data.put("date", Utils.getFormattedDateTime())
                    data.put("event", EventType.FCM_LOCATION)
                    data.put("mapUrl", mapUrl)*/

                    FCM.sendFCM(token, data)
                }
                ?.addOnFailureListener {
                    Log.e(TAG, "error")
                }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.FRENCH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG,"The Language specified is not supported!")
            }
            //tts.setOnUtteranceProgressListener(UtteranceManager())
            Log.e(TAG, "TTS setup done")

        } else {
            Log.e(TAG, "Initilization Failed!")
        }
    }

    companion object {
        const val REQUEST_PERMISSION = 0
        const val TABLET_MODE = true
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
        tts.stop()
        tts.shutdown()
        super.onDestroy()
        Log.d(TAG, "on destroy Removing snapshot")
    }

    override fun onStop() {
        tts.stop()
        tts.shutdown()
        super.onStop()
        //recorder?.release()
        //recorder = null
        EventBus.getDefault().unregister(this);
        Log.e(TAG, "On stop triggered")
    }

}