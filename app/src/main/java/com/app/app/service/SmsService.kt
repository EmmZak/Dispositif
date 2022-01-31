package com.app.app.service

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.app.utils.Utils
import java.io.File
import java.lang.Exception

class SmsService(val context: Context) {

    val number = "0766006439"
    val SENDING = false
    val TAG = "SmsService manu"

    fun sendSms(number: String, message: String) {
        if (!isSmsPermissionGranted()) {
            try {
                requestSmsPermission()
            } catch(e: Exception) {
                Log.e(TAG, "PERMISSION.exception ${e.toString()}")
                throw e
            }
        }
        Log.e(TAG, "sms permission OK")
        try {
            val finalMessage = "[${Utils.getFormattedDateTime()}] $message"

            Log.e(TAG, "$finalMessage")
            SmsManager.getDefault().sendTextMessage(number, null, finalMessage, null, null)
        } catch(e: Exception) {
            Log.e(TAG, "SEND.exception ${e.toString()}")
            throw e
        }
    }

    fun sendMms(number: String) {
        if (!isSmsPermissionGranted()) {
            try {
                requestSmsPermission()
            } catch(e: Exception) {
                Log.e(TAG, "PERMISSION.exception ${e.toString()}")
                throw e
            }
        }
        Log.e(TAG, "sms permission OK")

        File("${context.externalCacheDir?.absolutePath}").walk().forEach {
            Log.e(TAG, "$it")
        }

        try {
            val audioFile: File? = File("${context.externalCacheDir?.absolutePath}", "vocal.3gp")

            val finalMessage = "[${Utils.getFormattedDateTime()}]"

            Log.e(TAG, "message $finalMessage")

            val uri : Uri? = Uri.parse("file://"+"${context.externalCacheDir?.absolutePath}"+"/vocal.3gp")
            Log.e(TAG, "uri ${uri.toString()}")
            SmsManager.getDefault().sendMultimediaMessage(context, uri, null, null, null)
        } catch(e: Exception) {
            Log.e(TAG, "SEND.exception ${e.toString()}")
            throw e
        }
    }

    // helper funcions
    fun buildPdu(context: Context, number: String, subject: String, text: String) {

    }

    private fun isSmsPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermission() {
        val requestSendSms: Int = 2
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.SEND_SMS), requestSendSms)
    }
}