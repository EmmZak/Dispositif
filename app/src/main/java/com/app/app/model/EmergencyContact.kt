package com.app.app.model

data class EmergencyContact(val name: String, val number: String)
{
    constructor(): this("", "")
}