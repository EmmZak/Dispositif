package com.app.app.service.call

import android.content.ContextWrapper
import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import android.util.Log
import androidx.core.net.toUri
import com.app.app.dto.EventObject
import com.app.app.enums.EventType
import org.greenrobot.eventbus.EventBus

class CallService : InCallService() {

    val TAG = "manu CallService"

    /**
     * This callback handles
     *      Incoming Call -> Call.STATE_RINGING -> 2
     *      Outgoing Call -> Call.CONNECTING -> 9
     */
    override fun onCallAdded(call: Call) {
        Log.e(TAG, "call details handle ${call.details.handle}")
        Log.e(TAG,"onCallAdded state ${call.state}  $call ")
        CallService.call = call

        emit(call)
    }

    /**
     * This callback handles
     *      Call Ended -> Call.DISCONNECTED ->
     */
    override fun onCallRemoved(call: Call) {
        Log.e(TAG,"onCallRemoved state ${call.state} $call")
        CallService.call = null

        emit(call)
    }

    private fun emit(call: Call) {
        val handle = call.details.handle
        val number = handle.schemeSpecificPart
        Log.e(TAG, "call number $number")
        val data = hashMapOf(
            "state" to call.state,
            "number" to number
        )

        val o = EventObject(EventType.CALL, data as Map<String, Any>)
        EventBus.getDefault().post(o)
    }

    companion object {
        val TAG = "CallService.static"
        var state: Int = -1

        var call: Call? = null
            set(value) {
                field?.unregisterCallback(callback)
                value?.let {
                    it.registerCallback(callback)
                    state = it.state
                }
                field = value
            }

        /**
         * This callback handles call state
         * when communication is established
         */
        private val callback = object : Call.Callback() {
            override fun onStateChanged(call: Call, newState: Int) {
                Log.e(TAG, "callback state changed $newState")
                emit(call)
            }
        }

        fun emit(call: Call?) {
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

        fun call(number: String, context: ContextWrapper) {
            val uri = "tel:$number".toUri()
            context.startActivity(Intent(Intent.ACTION_CALL, uri))
        }

        fun answer() {
            try {
                call!!.answer(VideoProfile.STATE_AUDIO_ONLY)
            } catch (e: Exception) {
                Log.e(TAG, "error while answering $e")
            }
        }

        fun hangup() {
            try {
                call!!.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "error while disconnecting $e")
            }
        }
    }
}