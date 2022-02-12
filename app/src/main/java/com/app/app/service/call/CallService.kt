package com.app.app.service.call

import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import androidx.constraintlayout.motion.widget.Debug.getState
import androidx.fragment.app.FragmentManager
import com.app.app.CallDialog
import com.app.app.dto.EventObject
import com.app.app.dto.EventType
import org.greenrobot.eventbus.EventBus

class CallService : InCallService() {

    val TAG = "manu CallService"

    override fun onCallAdded(call: Call) {
        Log.e(TAG,"onCallAdded ici $call")
        OngoingCall.call = call

        val data = hashMapOf(
            "state" to call.state
        )
        val o = EventObject(EventType.CALL, data as Map<String, Object>)
        EventBus.getDefault().post(o)
    }

    override fun onCallRemoved(call: Call) {
        Log.e(TAG,"onCallRemoved $call")
        OngoingCall.call = null

        val data = hashMapOf(
            "state" to call.state
        )
        val o = EventObject(EventType.CALL, data as Map<String, Object>)
        EventBus.getDefault().post(o)
    }
}