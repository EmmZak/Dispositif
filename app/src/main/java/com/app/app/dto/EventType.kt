package com.app.app.dto

/**
 * Incoming FCM notification types
 *
 * TTS      -> speak message
 * CALL     ->
 * LOCATION -> send location to a number of FCM
 * SOS      -> send SOS alert
 */
enum class EventType {
    TTS,
    CALL,
    LOCATION,
    SOS
}