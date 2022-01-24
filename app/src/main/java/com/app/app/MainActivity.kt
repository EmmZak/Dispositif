package com.app.app

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import android.Manifest
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
import com.app.app.service.SmsService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class MainActivity : AppCompatActivity() {

    val number = "0766006439"
    val SENDING = false

    val BUILT_IN_CALL = false
    var db = FirebaseFirestore.getInstance()
    val TAG = "manu"
    var i = 0
    var configListener: ListenerRegistration? = null

    // services
    val smsService = SmsService(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "On create triggered")
        setContentView(R.layout.activity_main)

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
                Log.d(TAG, "app1: listener($i): ${snapshot.data}")
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
    }

    override fun onStop() {
        super.onStop()
        configListener?.remove()
        Log.e(TAG, "On stop triggered")
    }

    override fun isFinishing(): Boolean {
        return super.isFinishing()
    }

    fun sendOk(view: View) {
        Log.e("manu", "send OK")
        smsService.sendSms(number, "Salut, tout va bien")
    }

    fun sendKo(view: View) {
        Log.e("manu", "send KO")
        smsService.sendSms(number, "Salut, j'ai un petit souci")
    }

    @SuppressLint("MissingPermission")
    fun call(view: View) {
        Log.e("manu", "call")
        if (!isCallPermissionGranted()) {
            Log.e("manu", "call permission not granted")
            try {
                requestCallPermission()
            } catch(e: Exception) {
                Log.e("manu", "$e")
            }
        } else {
            Log.e("manu", "call permission OK")
            try {
                if (BUILT_IN_CALL) {
                    val dialIntent = Intent(Intent.ACTION_CALL)
                    dialIntent.data = Uri.parse("tel:$number")
                    startActivity(dialIntent)
                } else {
                    val tm = this.getSystemService(TELECOM_SERVICE) as TelecomManager

                    val accountHandle = getAccountHandle()
                    var phoneAccount: PhoneAccount

                    val builder = PhoneAccount.builder(accountHandle, BuildConfig.APPLICATION_ID)
                    builder.setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)

                    phoneAccount = builder.build()
                    tm.registerPhoneAccount(phoneAccount)

                    Log.e("manu", "tm call ")
                    val uri = Uri.fromParts("tel", "$number", null)
                    val extras = Bundle()
                    extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
                    extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, getAccountHandle());
                    tm.placeCall(uri, extras)
                }
            } catch(e: Exception) {
                Log.e("tm call catch", "${e.toString()}")
            }
        }
    }

    private fun getAccountHandle(): PhoneAccountHandle? {
        val phoneAccountLabel = BuildConfig.APPLICATION_ID
        val componentName = ComponentName(this, MyConnectionService::class.java)
        return PhoneAccountHandle(componentName, phoneAccountLabel)
    }

    /*
     Request permissions
     */
    fun isCallPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCallPermission() {
        val requestCall: Int = 1
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), requestCall)
    }
}