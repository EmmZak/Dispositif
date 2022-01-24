package com.app.app.service

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.app.utils.Utils
import java.lang.Exception

class SmsService(val context: Context) {

    val number = "0766006439"
    val SENDING = false
    val TAG = "SmsService manu"

    fun isSmsPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    fun requestSmsPermission() {
        val requestSendSms: Int = 2
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.SEND_SMS), requestSendSms)
    }

    fun sendSms(number: String, message: String) {
        if (!isSmsPermissionGranted()) {
            try {
                requestSmsPermission()
            } catch(e: Exception) {
                Log.e(TAG, "PERMISSION.exception ${e.toString()}")
            }
            return;
        }
        Log.e(TAG, "sms permission OK")
        try {
            val finalMessage = "[${Utils.getFormattedDateTime()}] $message"

            Log.e("manu", "$finalMessage")
            SmsManager.getDefault().sendTextMessage(number, null, finalMessage, null, null)
        } catch(e: Exception) {
            Log.e("manu", "SEND.exception ${e.toString()}")
        }
    }
}