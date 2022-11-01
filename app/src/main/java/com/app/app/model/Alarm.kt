package com.app.app.model

import com.app.app.enums.AlarmFrequency
import java.util.*

data class Alarm(
    var uid: String,
    var text: String,
    var datetime: Date,
    var frequency: AlarmFrequency)