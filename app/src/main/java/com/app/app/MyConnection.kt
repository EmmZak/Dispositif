package com.app.app

import android.telecom.Connection
import android.util.Log

class MyConnection: Connection {

    val TAG = "MyConnection manu"

    constructor() {
        Log.e(TAG, "creating MyConnetion")
        super.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED)
    }

    override fun onShowIncomingCallUi() {
        Log.e(TAG, "onShowIncomingCallUi")
    }

    override fun onAnswer() {
        Log.e(TAG, "onAnswer")
    }

    override fun onDisconnect() {
        Log.e(TAG, "onDisconnect")
    }
}