package com.app.app.model

/*
data class Client(
    var uid: String,
    var token: String,
    var number: String,
    var isEmergencyConcat: Boolean,
    var name: String
    ) {
    constructor(): this("", "", "", false, "")
}*/
data class Client(
    var uid: String,
    var token: String,
    var number: String,
    var email: String,
    var name: String
) {
    constructor(): this("", "", "", "", "")
}