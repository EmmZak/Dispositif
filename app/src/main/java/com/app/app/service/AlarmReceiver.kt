package com.app.app.service

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class AlarmReceiver: BroadcastReceiver(){

    val TAG = "AlarmReceiver manu"

    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        Log.e(TAG, "alarm received at ${Date()} ${intent.getStringExtra("tts")}")
    }

}