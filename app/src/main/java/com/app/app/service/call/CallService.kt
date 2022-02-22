package com.app.app.service.call

import android.os.Bundle
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import androidx.constraintlayout.motion.widget.Debug.getState
import androidx.fragment.app.FragmentManager
import com.app.app.dto.EventObject
import com.app.app.dto.EventType
import org.greenrobot.eventbus.EventBus

class CallService : InCallService() {

    val TAG = "manu CallService"

    /**
     * This service handles
     *      Incoming Call -> Call.STATE_RINGING -> 2
     */
    override fun onCallAdded(call: Call) {
        Log.e(TAG,"onCallAdded  $call ")
        Log.e(TAG,"onCallAdded code ${call.state}")
        Log.e(TAG, "emmanuel OngoingCall.call before ${OngoingCall.call}")
        OngoingCall.call = call
        Log.e(TAG, "emmanuel OngoingCall.call after ${OngoingCall.call}")

        Log.e(TAG, "incoming call detected, going to emit")

        Log.e(TAG, "${call.state} == ${Call.STATE_RINGING}")
/*        if (call.state == Call.STATE_RINGING){
            emit(call)
        }*/
    }

    override fun onCallRemoved(call: Call) {
        Log.e(TAG,"onCallRemoved $call")
        OngoingCall.call = null

        //emit(call)
    }

    private fun emit(call: Call) {
        val data = hashMapOf(
            "state" to call.state
        )
        val o = EventObject(EventType.CALL, data as Map<String, Any>)
        EventBus.getDefault().post(o)
    }
}