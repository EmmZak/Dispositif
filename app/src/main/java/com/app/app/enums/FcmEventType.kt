package com.app.app.enums

/**
 * Incoming FCM notification types
 *
 * TTS       -> speak message
 * CALL      ->
 * LOCATION  -> send location to a number of FCM
 * SOS       -> send SOS alert
 * CONFIG    -> todo
 * SUBSCRIBE -> new client
 */
enum class FcmEventType {
    TTS,
    CALL,
    LOCATION,
    SOS,
    CONFIG,
    NOTIFICATION_SUCCESS,
    SUBSCRIBE
}