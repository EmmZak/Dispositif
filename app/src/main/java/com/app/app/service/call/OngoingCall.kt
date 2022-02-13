package com.app.app.service.call

import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log
import com.app.app.dto.EventObject
import com.app.app.dto.EventType
import io.reactivex.subjects.BehaviorSubject
import org.greenrobot.eventbus.EventBus

object OngoingCall {
    val TAG = "manu"

    val state: BehaviorSubject<Int> = BehaviorSubject.create()

    /**
     * This callback handles changes such as
     *      Outgoing Call -> Call.STATE_DIALING      -> 1
     *      Answear       -> Call.STATE_ACTIVE       -> 4
     *      Hungup        -> Call.STATE_DISCONNECTED -> 7
     */
    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            //Log.e(TAG, "OngoingCall state changed newstate $newState")

            val data = hashMapOf(
                "state" to call.state
            )
            Log.e(TAG, "state changed ${call.state}")
            val o = EventObject(EventType.CALL, data as Map<String, Object>)
            EventBus.getDefault().post(o)
        }
    }

    var call: Call? = null
        set(value) {
            field?.unregisterCallback(callback)
            value?.let {
                it.registerCallback(callback)
                state.onNext(it.state)
            }
            field = value
        }

    fun answer() {
        call!!.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        call!!.disconnect()
    }
}