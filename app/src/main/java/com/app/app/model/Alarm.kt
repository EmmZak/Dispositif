package com.app.app.model

import com.app.app.enums.AlarmFrequency
import java.util.*

data class Alarm(
    var id: Int, // String,
    var active: Boolean,
    var text: String,
    var datetime: String,
    var frequencyType: AlarmFrequency,
    var weekdays: List<Int>)
{
    constructor(): this(0, false, "", "", AlarmFrequency.ONCE, emptyList())
}