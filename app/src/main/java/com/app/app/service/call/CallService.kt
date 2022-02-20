package com.app.app.service.call

import android.telecom.Call
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
        Log.e(TAG,"onCallAdded ici $call")
        OngoingCall.call = call

        if (call.state != Call.STATE_RINGING) {
            return
        }
        Log.e(TAG, "incoming call detected, going to emit")
        val data = hashMapOf(
            "state" to call.state
        )
        val o = EventObject(EventType.CALL, data as Map<String, Any>)
        EventBus.getDefault().post(o)
    }

    override fun onCallRemoved(call: Call) {
        Log.e(TAG,"onCallRemoved $call")
        OngoingCall.call = null

        //val data = hashMapOf("state" to call.state)
        //val o = EventObject(EventType.CALL, data as Map<String, Any>)
        //EventBus.getDefault().post(o)
    }
}