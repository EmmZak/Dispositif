package com.app.app.service.call

import android.telecom.Call
import android.telecom.InCallService
import android.util.Log

class CallService : InCallService() {

    val TAG = "manu"
    override fun onCallAdded(call: Call) {
        Log.e(TAG,"onCallAdded $call")
        OngoingCall.call = call
        //CallDialog.start(..., call)
    }

    override fun onCallRemoved(call: Call) {
        Log.e(TAG,"onCallAdded $call")
        OngoingCall.call = null
    }
}