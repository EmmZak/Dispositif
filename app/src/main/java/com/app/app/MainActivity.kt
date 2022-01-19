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


class MainActivity : AppCompatActivity() {

    val number = "0766006439"
    val SENDING = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("manu", "start")
    }

    fun sendOk(view: View) {
        Log.e("manu", "send OK")
        sendSMS(number, "Salut, tout va bien")
    }

    fun sendKo(view: View) {
        Log.e("manu", "send KO")
        sendSMS(number, "Salut, j'ai un petit souci")
    }

    fun sendSMS(number: String, message: String) {
        if (!isSmsPermissionGranted()) {
            Log.e("manu", "permission sms not granted")
            try {
                requestSmsPermission()
                Log.e("manu", "permission granted")
            } catch(e: Exception) {
                Log.e("manu", "PERMISSION.exception ${e.toString()}")
            }
        } else {
            Log.e("manu", "sms permission OK")
            try {
                val finalMessage = "[${getFormattedDateTime()}] $message"

                Log.e("manu", "$finalMessage")
                SmsManager.getDefault().sendTextMessage(number, null, finalMessage, null, null)
            } catch(e: Exception) {
                Log.e("manu", "SEND.exception ${e.toString()}")
            }
        }
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
                //val dialIntent = Intent(Intent.ACTION_CALL)
                //dialIntent.data = Uri.parse("tel:$number")
                //startActivity(dialIntent)

                val tm = this.getSystemService(TELECOM_SERVICE) as TelecomManager

                val accountHandle = getAccountHandle()
                var phoneAccount = tm.getPhoneAccount(accountHandle)

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
     Helper functions
     */
    fun getFormattedDateTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy - HH:mm")
        return current.format(formatter)
    }

    /*
     Request permissions
     */
    fun isSmsPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    fun isCallPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    }

    fun requestSmsPermission() {
        val requestSendSms: Int = 2
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), requestSendSms)
    }

    fun requestCallPermission() {
        val requestCall: Int = 1
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), requestCall)
    }
}