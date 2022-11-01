package com.app.app.service

import android.app.AlarmManager
import android.util.Log
import java.util.*

class AlarmListener: AlarmManager.OnAlarmListener  {
    val TAG = "AlarmListener manu"

    override fun onAlarm() {
        Log.e(TAG, "onAlarm triggered at ${Date()}")
    }
}