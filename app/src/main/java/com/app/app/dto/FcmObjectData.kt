package com.app.app.dto

import com.app.app.enums.EventType
import java.util.*

data class FcmObjectData(var event: EventType?, var date: Date?, var number: String?, var message: String?, var token: String?, var mapUrl: String?) {
    constructor() : this(null, null, null, null, null, null)
}
