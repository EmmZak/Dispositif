package com.app.app.model

data class App(
    var uid: String,
    var token: String,
    var number: String,
    var address: String,
    var alarms: List<Alarm>,
    var clients: List<Client>
) {
    constructor() : this("", "", "", "", arrayListOf<Alarm>(), arrayListOf<Client>())
}