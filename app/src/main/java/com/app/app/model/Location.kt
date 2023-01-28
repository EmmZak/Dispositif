package com.app.app.model

import java.util.*

data class Location(var date: String, var lat: Float, var long: Float) {
    constructor(): this("", 0f, 0f)
}