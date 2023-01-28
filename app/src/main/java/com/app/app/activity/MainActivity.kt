package com.app.app.activity

import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import java.lang.Exception
import java.util.*
import android.speech.tts.TextToSpeech
import com.app.app.service.call.CallService
import com.app.app.service.sms.SmsService

import androidx.annotation.RequiresApi

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe

import android.os.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.speech.tts.UtteranceProgressListener
import android.telecom.Call

import android.telecom.TelecomManager
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
import android.widget.*
import androidx.core.view.isGone
import com.app.app.config.Config
import com.app.app.config.SMSTemplate
import com.app.app.db.AppRepository
import com.app.app.dto.FcmObjectData
import com.app.app.enums.AlarmFrequency
import com.app.app.enums.EventType
import com.app.app.model.Alarm
import com.app.app.model.App
import com.app.app.service.AlarmListener
import com.app.app.service.AlarmReceiver
import com.app.app.service.EventService
import com.app.app.service.FCM
import com.app.app.service.alarm.AlarmService
import com.app.app.service.tts.TtsService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.Timestamp
import com.google.gson.Gson
import okhttp3.OkHttpClient
import kotlin.system.exitProcess

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

    val TAG = "MainActivity manu"

    // audio recording
    var isRecording = false
    //var recorder: MediaRecorder? = null
    //var FILE_NAME : String = ""
    //var OUTPUT_DIR : String = ""
    //val pop = AudioRecordingDialog()
    val httpClient = OkHttpClient()

    // Call
    private var CALL_STATE: Int = -1
    private var EMERGENCY_CONTACT_NUMBER: String = ""
    private var EMERGENCY_CONTACT_NAME: String = ""

    // TTS
    private var tts: TextToSpeech = TextToSpeech(this, this)

    // Services
    private var ttsService: TtsService = TtsService(this)
    private var vibratorService: VibratorService = VibratorService(this)
    private var smsService: SmsService = SmsService(vibratorService)
    private var callService: CallService = CallService()
    private var gpsService: GpsService = GpsService(this)
    private val alarmService: AlarmService = AlarmService(this)

    private var eventService: EventService? = null

    // call
    private var callDialog : CallDialog? = null

    // App
    private var repo: AppRepository = AppRepository()
    private var app: App? = null

    // Alarms
    private var intentRequestCodes = IntArray(0)

    /**
     * Progress Bar
     */
    private var IS_SETUP_DONE = false

    /**
     * UI elements
     */
    lateinit var emergencyCallBtnView: ImageView
    lateinit var emergencyContactView: TextView
    lateinit var sosBtnView: ImageView
    lateinit var progressBarView: ProgressBar

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isOnline(this)) {
            Log.e(TAG, "network not available exiting")
            Toast.makeText(this, "Pas de connexion internet", Toast.LENGTH_LONG).show()
            exitProcess(-1)
        }
        //window.setFlags(android.R.attr.windowFullscreen, android.R.attr.windowFullscreen )
        setContentView(R.layout.activity_main)
        setProgressScreen(true)

        emergencyCallBtnView = findViewById(R.id.emergencyContactCall)
        emergencyContactView = findViewById(R.id.emergencyContactName)
        sosBtnView = findViewById(R.id.sos)
        progressBarView = findViewById(R.id.progressBar)

        //app = dbService.findApp()
        repo.findApp()
            .addOnSuccessListener { appDoc ->
                if (appDoc.data == null) {
                    Log.e(TAG, "appDoc.data is null")
                    return@addOnSuccessListener
                }
                app = appDoc.toObject(App::class.java)
                if (app == null) {
                    Log.e(TAG, "app model is null")
                    return@addOnSuccessListener
                }
                app!!.uid = appDoc.id
                //Log.e(TAG, "app $app")
                //Log.e(TAG, "app.alarms ${app.alarms}")
                //Log.e(TAG, "app.clients ${app.clients}")
                //Log.e(TAG, "app.lastLocation ${app.lastLocation}")
                Log.e(TAG, "app.emergencyContact ${app!!.emergencyContact}")
                setupEmergencyContact()
                alarmService.setAlarms(app!!.alarms)
            }
            .addOnFailureListener {
                Log.e(TAG, "error while loading app")
                println("found app $it")
            }
        //setupLocalStorage()
        //setupAlarms()
        //setupClients()

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

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "fine access location not granted")
            exitProcess(0)
        }

        //tts = TextToSpeech(this, this)
        //Log.e(TAG, "default engine ${tts!!.defaultEngine}")

        //filename = "${externalCacheDir?.absolutePath}/vocal.3gp"
        //FILE_NAME = "audio.3gp"
        // Android/data/com.app.app/cache/audio.3gp
        //OUTPUT_DIR = externalCacheDir?.absolutePath.toString()
        //OUTPUT_DIR = getExternalFilesDir(null)?.absolutePath.toString()

        // call card on click TABLETT
        setupOnClickListeners()

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

    private fun setupEmergencyContact() {
        if (app == null) {
            Log.e(TAG, "app is null")
            return
        }
        Log.e(TAG, "setting up emergency contact, app.emerg ${app?.emergencyContact}")

        EMERGENCY_CONTACT_NUMBER = app!!.emergencyContact.number
        EMERGENCY_CONTACT_NAME = app!!.emergencyContact.name

        val text = "$EMERGENCY_CONTACT_NAME $EMERGENCY_CONTACT_NUMBER"

        emergencyContactView.text = text as CharSequence?
        setProgressScreen(false)
        /*
        repo.findEmergencyContact()
            .addOnSuccessListener { contactDoc ->
                val contact = contactDoc.data
                Log.e(TAG, "app client doc $contact")
                if (contact == null) {
                    Log.e(TAG, "emergency contact doc is null")
                    return@addOnSuccessListener
                }
                Log.e(TAG, "setting emergency contact doc $contact")
                EMERGENCY_CONTACT_NUMBER = contact["number"] as String
                EMERGENCY_CONTACT_NAME = contact["name"] as String
                val text = "${contact["name"]} ${contact["number"]}"
                findViewById<TextView>(R.id.emergencyContactName).text = text as CharSequence?

                setProgressScreen(false)
            }
            .addOnFailureListener {
                Log.e(TAG, "error while loading app clients $it")
                println("found app $it")
            } */
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun removeAlarms() {

        //val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        //sharedPref.get

        (1..5).forEach { requestCode ->
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_NO_CREATE)

            try {
                Log.e(TAG, "removing alarms")
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "e $e")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupAlarms() {
        if (app == null) {
            Log.e(TAG, "app is null")
            return
        }
        val alarms = app!!.alarms
        alarmService.setAlarms(alarms)

        //Log.e(TAG, "store.all ${store.all}")

        //val cal = Calendar.getInstance()
        //cal.add(Calendar.MINUTE, 1)
        //Log.e(TAG, "alarm at ${cal.time}")
        //removeAlarms()

        repo.findAlarms()
            .addOnSuccessListener { app ->
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                app.documents.forEachIndexed { requestCode, alarmDoc ->
                    val alarm = alarmDoc.data
                    Log.e(TAG, "alarm $alarm")
                    if (alarm == null || !(alarm["active"] as Boolean)) {
                        return@forEachIndexed
                    }
                    val intent = Intent(this, AlarmReceiver::class.java)
                    intent.putExtra("text", alarm["text"] as String)
                    val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
                    intentRequestCodes = intentRequestCodes.plus(requestCode)

                    val date = alarm["datetime"] as Timestamp
                    val millis = date.seconds*1000

                    Log.e(TAG, "${alarm["frequencyType"]} alarm at ${date.toDate()}, millis $millis, ${alarm["text"]} ")

                    //AlarmManager.Alarm

                    when(alarm["frequencyType"]) {
                        AlarmFrequency.ONCE.name -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
                        AlarmFrequency.REPEATING.name -> alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, millis, alarm["frequency"] as Long, pendingIntent)
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "error while loading app alarms $it")
                println("found app $it")
            }
        /*
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarms = arrayOf(
            mapOf("tts" to "Toutes les 1 min", "frequencyType" to "ONCE", "frequency" to 1*60*1000L),
            mapOf("tts" to "Toutes les 2 min", "frequencyType" to "REPEATING", "frequency" to 1*60*1000L)
        )

        alarms.forEachIndexed { i, alarm ->
            val intent = Intent(this, AlarmReceiver::class.java)
            intent.putExtra("tts", alarm["tts"])
            val pendingIntent = PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_MUTABLE)

            when(alarm["frequencyType"]) {
                AlarmFrequency.ONCE -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent)
                AlarmFrequency.REPEATING -> alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0, alarm["freq"] as Long, pendingIntent)
            }
        } */
    }

    private fun setupClients() {
        repo.findClients()
            .addOnSuccessListener { app ->
                app.documents.map { clientDoc ->
                    val client = clientDoc.data
                    Log.e(TAG, "app client doc $client")
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "error while loading app clients $it")
                println("found app $it")
            }
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

    private fun checkPermissions() {
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupOnClickListeners() {
        emergencyCallBtnView.setOnClickListener { view ->
            val anim = AnimationUtils.loadAnimation(this, R.anim.zoom_out_call)
            view.startAnimation(anim)
            if (EMERGENCY_CONTACT_NUMBER == "") {
                Log.e(TAG, "emergency number is empty")
                return@setOnClickListener
            }
            call(EMERGENCY_CONTACT_NUMBER)
        }

        sosBtnView.setOnClickListener { view ->
            val anim = AnimationUtils.loadAnimation(this, R.anim.zoom_out_notif)
            view.startAnimation(anim)

            sendNotif(view.tag as String)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun sendNotif(tag: String) {
        Log.e(TAG, "tag $tag")
        when(tag) {
            "OK" -> sendSms(Contact.getAllNumbers(), SMSTemplate.OK.text)
            "KO" -> sendSms(Contact.getAllNumbers(), SMSTemplate.KO.text)
            "SOS" -> sendSos()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun sendSos() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "fine location not granted")
            return
        }
        //val cancel = CancellationTokenSource()
        gpsService.getLocation()
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
            smsService.sendSms(this, number, text)
            message = "Notification envoyée"
            //vibratorService.vibrate(1000)
        } catch (e: SmsException) {
            Log.e(TAG, "$e")
            message = "Problème lors de l'envoi de la notification"
        }

        speak(message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun speak(message: String) {
        ttsService.speak(message)
        /*try {
            val res = tts.speak(message, TextToSpeech.QUEUE_ADD, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)

            if (res == TextToSpeech.ERROR) {
                Log.e(TAG, "tts speak QUEUE error res=$res")
            } else {
                Log.e(TAG, "tts worked")
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }*/
    }

    @SuppressLint("MissingPermission")
    fun call(number: String) {
        Log.e(TAG, "call ")
        CallService.call(number, this)
        /*
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
        } */
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

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i(TAG, "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i(TAG, "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i(TAG, "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.S)
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
            EventType.FCM_ALARM_CHANGE -> setupAlarms()
            EventType.FCM_CLIENT_CHANGE -> setupClients()
            EventType.FCM_EMERGENCY_CONTACT_CHANGE -> setupEmergencyContact()
            EventType.FCM_LOCATION_UPDATE -> TODO()
            EventType.CALL -> onCallEvent(eventObject)
            EventType.NOTIFICATION_SUCCESS -> TODO()

            EventType.FCM_TTS_ERROR -> TODO()
        }
    }


    private fun onTtsError() {

    }

    @RequiresApi(Build.VERSION_CODES.S)
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
            CALL_STATE = Integer.parseInt(eventObject.data["state"].toString())
            Log.e(TAG, "received call state $CALL_STATE")

            // Outgoing call
            if (CALL_STATE == Call.STATE_CONNECTING /* == 9 */) {
                Log.e(TAG, "Dialing ...")
                callDialog = CallDialog.newInstance(EMERGENCY_CONTACT_NAME, "Appel en cours ...", 1)
                callDialog!!.isCancelable = false
                (callDialog as CallDialog).show(supportFragmentManager, "call dialog")

                // launch timer to stop call if not pick up in 20 sec
                object : CountDownTimer(60000, 10000) {
                    override fun onTick(millisUntilFinished: Long) {
                        Log.e(TAG, "waiting $millisUntilFinished before hanging up")
                        Log.e(TAG, "call service state $CALL_STATE, waiting: $millisUntilFinished")

                        // dismiss
                        if (CALL_STATE == 7) {
                            Log.i(TAG, "hanging up as dismissed")
                            cancel()
                        }
                    }
                    override fun onFinish() {
                        Log.e(TAG, "timer finished")
                        CallService.hangup()
                        /*
                        if (CALL_STATE == 1) {
                            Log.e(TAG, "hanging up automatically")
                            CallService.hangup()
                        }
                        // répondeur
                        if (CALL_STATE == 4) {
                            Log.e(TAG, "Dans le répondeur")
                        } */
                    }
                }.start()

            }
            // Incoming call
            if (CALL_STATE == Call.STATE_RINGING /* == 2 */) {
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
                val number = eventObject.data["number"] as String
                val name = Contact.getContactNameByNumber(number)
                callDialog = CallDialog.newInstance(name, "Appel entrant ...", 2)
                callDialog!!.isCancelable = false
                (callDialog as CallDialog).show(supportFragmentManager, "call dialog")
            }
            // Disconnect
            if (CALL_STATE == Call.STATE_DISCONNECTED /* == 7 */) {
                //Log.e(TAG, "Closing ...")
                callDialog?.dismiss()
                callDialog = null
            }
            // In Call
            if (CALL_STATE == Call.STATE_ACTIVE /* == 4 */) {
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
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "")
        }
    }

    private fun onLocationEvent(eventObject: EventObject, token: String) {
        Log.e(TAG, "onLocationEvent $eventObject")
        if (eventObject.event == EventType.FCM_LOCATION) {
            val text = eventObject.data["message"].toString()
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "")

            val res = gpsService.getLocation()
            Log.e(TAG, "res $res")
            gpsService.getLocation()
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

    private fun setProgressScreen(isProgress: Boolean) {
        emergencyCallBtnView.isGone = isProgress
        emergencyContactView.isGone = isProgress
        sosBtnView.isGone = isProgress
        progressBarView.isGone = !isProgress
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        EventBus.getDefault().unregister(this);
        super.onDestroy()
        Log.d(TAG, "on destroy Removing snapshot")
    }

    override fun onStop() {
        tts.stop()
        tts.shutdown()
        EventBus.getDefault().unregister(this);
        Log.e(TAG, "On stop triggered")
        super.onStop()
        //recorder?.release()
        //recorder = null
    }

    companion object {
        const val REQUEST_PERMISSION = 0
        const val TABLET_MODE = true
    }
}