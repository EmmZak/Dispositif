package com.app.app

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.util.Log

class MyConnectionService() : ConnectionService() {

    val TAG = "MyConnectionService manu"

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.e(TAG, "onCreateOutgoingConnection")
        //val con = super.onCreateOutgoingConnection(connectionManagerPhoneAccount, request)
        val con = MyConnection()
        return con
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.e(TAG, "onCreateIncomingConnection")
        return super.onCreateIncomingConnection(connectionManagerPhoneAccount, request)
    }
}