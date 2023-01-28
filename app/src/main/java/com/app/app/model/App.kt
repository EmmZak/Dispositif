package com.app.app.model

data class App(
    var uid: String,
    var token: String,
    var number: String,
    var address: String,
    var emergencyContact: EmergencyContact,
    var lastLocation: Location,
    var alarms: List<Alarm>,
    var clients: List<Client>
) {
    constructor() : this("", "", "", "", EmergencyContact(), Location(), arrayListOf<Alarm>(), arrayListOf<Client>())
}