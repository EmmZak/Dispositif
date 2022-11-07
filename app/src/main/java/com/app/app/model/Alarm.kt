package com.app.app.model

import com.app.app.enums.AlarmFrequency
import java.util.*

data class Alarm(
    var uid: String,
    var active: Boolean,
    var text: String,
    var date: Date?,
    var frequencyType: AlarmFrequency,
    var frequency: Long?)
{
    constructor(): this("", false, "", null, AlarmFrequency.ONCE, null)
}