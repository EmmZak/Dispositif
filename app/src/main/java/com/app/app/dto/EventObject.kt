package com.app.app.dto

import com.app.app.enums.EventType

data class EventObject(
    val event: EventType,
    val data: Map<String, Any>) {
}