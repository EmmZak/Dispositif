package com.app.app.service.call

import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log
import io.reactivex.subjects.BehaviorSubject

object OngoingCall {
    val TAG = "manu"

    val state: BehaviorSubject<Int> = BehaviorSubject.create()

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            Log.e(TAG, "OngoingCall state changed")
            state.onNext(newState)
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