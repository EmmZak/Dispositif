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
    /*
    //private val state: BehaviorSubject<Int> = BehaviorSubject.create()
    private var state: Int = -1

    /**
     * This callback handles changes such as
     *      Outgoing Call -> Call.STATE_DIALING      -> 1
     *      Answear       -> Call.STATE_ACTIVE       -> 4
     *      Hungup        -> Call.STATE_DISCONNECTED -> 7
     */
    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            //Log.e(TAG, "OngoingCall state changed newstate $newState")
            emit(call)
        }
    }

    var call: Call? = null
        set(value) {
            field?.unregisterCallback(callback)
            value?.let {
                it.registerCallback(callback)
                state = it.state
            }
            field = value
        }

    private fun emit(call: Call?) {
        var state = Call.STATE_DISCONNECTING

        if (call != null) {
            state = call.state
        }

        val data = hashMapOf(
            "state" to state
        )
        val o = EventObject(EventType.CALL, data as Map<String, Any>)
        EventBus.getDefault().post(o)
    }

    fun answer() {
        call!!.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        call!!.disconnect()
    }
    */

}