package com.app.app.dto

import com.app.app.enums.FcmEventType

data class EventObject(
    val type: FcmEventType,
    val data: Map<String, Any>) {
}