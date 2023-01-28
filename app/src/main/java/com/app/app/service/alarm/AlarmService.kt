package com.app.app.service.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import com.app.app.enums.AlarmFrequency
import com.app.app.model.Alarm
import com.app.app.service.AlarmReceiver
import com.google.gson.Gson
import java.time.LocalDateTime
import java.util.*

class AlarmService(val context: Context) {
    val TAG = "AlarmService manu"

    val SHARED_REFERENCE_NAME = "alarm"
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    fun setAlarms(alarms: List<Alarm>) {
        //Log.e(TAG, "setting alarms $alarms")
        // cancel existing alarms
        val existingAlarms = findAllFromSharedReferences(SHARED_REFERENCE_NAME)
        existingAlarms.forEach { alarm ->
            removeAlarm(alarm)
        }

        // create new alarms
        alarms.forEach { alarm ->
            setAlarm(alarm)
        }

        val newAlarms = findAllFromSharedReferences(SHARED_REFERENCE_NAME)
        newAlarms.forEach { a -> Log.e(TAG, "alarm after $a") }
    }

    /**
     * 1. Create alarm intent
     * 2. add to shared references
     */
    private fun setAlarm(alarm: Alarm) {
        // intent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val dateStr = alarm.datetime
        val date = Date(dateStr)
        val millis = date.seconds*1000L

        when(alarm.frequencyType) {
            //AlarmFrequency.ONCE -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
            //AlarmFrequency.REPEATING -> alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, millis, 0, pendingIntent)
        }

        // shared ref
        addToSharedReferencesByKey(alarm.id.toString(), Gson().toJson(alarm), SHARED_REFERENCE_NAME)
    }

    /**
     * 1. Cancel alarm intent
     * 2. Remove from shared references
     */
    private fun removeAlarm(alarm: Alarm) {
        // intent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            Intent(context, AlarmReceiver::class.java),
            0
        )
        //alarmManager.cancel(pendingIntent);

        // shared ref
        removeFromSharedReferencesByKey(alarm.id.toString(), SHARED_REFERENCE_NAME)
    }


    /**
     * SHARED PREFERENCES
     */
    fun addToSharedReferencesByKey(key: String, value: String, name: String) {
        val store = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        with (store.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun removeFromSharedReferencesByKey(key: String, name: String) {
        val store = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        with (store.edit()) {
            remove(key)
            apply()
        }
    }

    fun findAllFromSharedReferences(name: String): List<Alarm> {
        val store = context.getSharedPreferences(name, Context.MODE_PRIVATE)

        Log.e(TAG, "store.all ${store.all}")

        store.all.map { (id, alarmJson) ->
            Log.e(TAG, "id: $id, alarmJson: $alarmJson")
            //Gson().fromJson(alarmJson as String, Alarm::class.java)
        }

        return store.all.map { (_, alarmJson) ->
            Gson().fromJson(alarmJson as String, Alarm::class.java)
        }
    }
}