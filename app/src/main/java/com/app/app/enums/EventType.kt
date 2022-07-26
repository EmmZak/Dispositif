package com.app.app.enums

/**
 * Incoming FCM notification types are with FCM_ prefix
 *
 * FCM_TTS       -> speak message
 * FCM_TTS_ERROR -> tts not working
 * FCM_LOCATION  -> send location notif
 * FCM_SOS       -> send SOS alert
 * FCM_CONFIG    ->
 * FCM_SUBSCRIBE ->
 *
 * CALL          -> Start new call intent
 * NOTIFICATION_SUCCESS -> send back notif
 */
enum class EventType(value: String) {
    FCM_TTS("FCM_TTS"),
    FCM_TTS_ERROR("FCM_TTS_ERROR"),
    FCM_LOCATION("FCM_LOCATION"),
    FCM_SOS("FCM_SOS"),
    FCM_CONFIG("FCM_CONFIG"),
    FCM_SUBSCRIBE("FCM_SUBSCRIBE"),
    CALL("CALL"),
    NOTIFICATION_SUCCESS("NOTIFICATION_SUCCESS")
}