package com.app.app.model

data class Client(
    var uid: String,
    var token: String,
    var number: String,
    var isEmergencyConcat: Boolean,
    var name: String
    ) {
    constructor(): this("", "", "", false, "")
}