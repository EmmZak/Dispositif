package com.app.app.service.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.app.app.activity.MainActivity

/**
 *
 *
 */
object AppPermissionService {

    // permissions
    fun requestPermissions(context: Context) {
        ActivityCompat.requestPermissions(context as Activity,
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 0);
    }

    // call
    fun isPhoneCallGranted(context: Context): Boolean {
        return PermissionChecker.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    fun requestPhoneCallPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.CALL_PHONE),
            MainActivity.REQUEST_PERMISSION
        )
    }

    // sms
    fun isSmsPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    fun requestSmsPermission(context: Context) {
        val requestSendSms: Int = 2
        ActivityCompat.requestPermissions(context as Activity,
            arrayOf(Manifest.permission.SEND_SMS), requestSendSms)
    }

    // audio
    fun isRecordAudioPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
    }

    fun requestRecordAudioPermission(context: Context) {
        ActivityCompat.requestPermissions(context as Activity,
            arrayOf(Manifest.permission.RECORD_AUDIO), 0)
    }

    // storage read
    fun isStorageReadPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun requestStorageReadPermission(context: Context) {
        ActivityCompat.requestPermissions(context as Activity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
    }

    // storage write
    fun isStorageWritePermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun requestStorageWritePermission(context: Context) {
        ActivityCompat.requestPermissions(context as Activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
    }

}