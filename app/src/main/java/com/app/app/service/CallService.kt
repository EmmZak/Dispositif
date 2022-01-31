package com.app.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.app.app.BuildConfig
import com.app.app.MyConnectionService
import java.lang.Exception


class CallService(val context: Context) {

    val TAG = "CallService manu"

    @SuppressLint("MissingPermission")
    fun call(number: String) {
        val tm = context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager

        val accountHandle = getAccountHandle()
        var phoneAccount: PhoneAccount

        val builder = PhoneAccount.builder(accountHandle, BuildConfig.APPLICATION_ID)
        builder.setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)

        phoneAccount = builder.build()
        tm.registerPhoneAccount(phoneAccount)

        Log.e(TAG, "tm call $number")
        val uri = Uri.fromParts("tel", "$number", null)
        val extras = Bundle()
        extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
        extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, getAccountHandle());
        tm.placeCall(uri, extras)
    }

    private fun getAccountHandle(): PhoneAccountHandle? {
        val phoneAccountLabel = BuildConfig.APPLICATION_ID
        val componentName = ComponentName(context, MyConnectionService::class.java)
        return PhoneAccountHandle(componentName, phoneAccountLabel)
    }

    fun nativeCall(number: String) {
        if (!isCallPermissionGranted()) {
            try {
                requestCallPermission()
            } catch(e: Exception) {
                Log.e(TAG, "$e")
            }
        } else {
            val dialIntent = Intent(Intent.ACTION_CALL)
            dialIntent.data = Uri.parse("tel:$number")
            context.startActivity(dialIntent)
        }
    }

    private fun isCallPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCallPermission() {
        val requestCall: Int = 1
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CALL_PHONE), requestCall)
    }
}