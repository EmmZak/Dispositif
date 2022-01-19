package com.app.app

import android.telecom.Connection
import android.util.Log

class MyConnection: Connection {

    constructor() {
        Log.e("manu", "creating MyConnetion")
        super.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED)
    }

    override fun onShowIncomingCallUi() {
        Log.e("manu", "onShowIncomingCallUi")
    }

    override fun onAnswer() {
        Log.e("manu", "onAnswer")
    }

    override fun onDisconnect() {
        Log.e("manu", "onDisconnect")
    }
}