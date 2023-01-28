package com.app.app.service.sms

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.app.app.config.SMSTemplate
import com.app.app.exception.SmsException
import com.app.app.service.call.Contact
import com.app.app.service.vibrator.VibratorService
import com.app.app.utils.Utils
import java.io.File
import java.lang.Exception

class SmsService(val vibratorService: VibratorService) {

    val SENDING = false
    val TAG = "SmsService manu"

    fun sendOK(context: Context) {
        sendSms(context, Contact.getAllNumbers(), SMSTemplate.OK.text)
    }

    fun sendKO(context: Context) {
        sendSms(context, Contact.getAllNumbers(), SMSTemplate.KO.text)
    }

    fun sendSOS() {

    }

    fun sendLocation(number: String) {

    }

    fun sendSms(context: Context, number: String, message: String) {
        if (!isSmsPermissionGranted(context)) {
            try {
                requestSmsPermission(context)
            } catch(e: Exception) {
                Log.e(TAG, "PERMISSION.exception ${e.toString()}")
                throw e
            }
        }
        Log.e(TAG, "sms permission OK")
        try {
            val finalMessage = "[${Utils.getFormattedDateTime()}] $message"

            Log.e(TAG, "$finalMessage")

            //context.getSystemService(SmsManager.class)
            SmsManager.getDefault().sendTextMessage(number, null, finalMessage, null, null)
        } catch(e: Exception) {
            Log.e(TAG, "SEND.exception ${e.toString()}")
            throw SmsException(e.toString())
        }
    }

    fun sendSms(context: Context, numbers: Array<String>, message: String) {
        /*if (!isSmsPermissionGranted(context)) {
            try {
                requestSmsPermission(context)
            } catch(e: Exception) {
                Log.e(TAG, "PERMISSION.exception ${e.toString()}")
                throw e
            }
        }*/
        Log.e(TAG, "sms permission OK")
        try {
            val finalMessage = "[${Utils.getFormattedDateTime()}] $message"
            Log.e(TAG, finalMessage)
            for(number in numbers) {
                SmsManager.getDefault().sendTextMessage(number, null, finalMessage, null, null)
            }
            vibratorService.vibrate(1000)
        } catch(e: Exception) {
            Log.e(TAG, "SEND.exception ${e.toString()}")
            throw SmsException(e.toString())
        }
    }

    fun sendMms(context: Context, number: String, OUTPUT_DIR: String, FILE_NAME: String) {
        if (!isSmsPermissionGranted(context)) {
            try {
                requestSmsPermission(context)
            } catch(e: Exception) {
                Log.e(TAG, "PERMISSION.exception ${e.toString()}")
                throw SmsException(e.toString())
            }
        }
        Log.e(TAG, "sms permission OK")

        File("$OUTPUT_DIR").walk().forEach {
            Log.e(TAG, "$it")
        }

        try {
            val audioFile: File? = File("$OUTPUT_DIR", "$FILE_NAME")

            val finalMessage = "[${Utils.getFormattedDateTime()}]"

            Log.e(TAG, "message $finalMessage")

            val uri : Uri? = Uri.parse("file://$OUTPUT_DIR/$FILE_NAME")
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

    private fun isSmsPermissionGranted(context: Context): Boolean {
        val granted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        Log.e(TAG, "sms granted $granted")
        return granted
    }

    private fun requestSmsPermission(context: Context) {
        val requestSendSms: Int = 2
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.SEND_SMS), requestSendSms)
    }
}