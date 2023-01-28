package com.app.app.enums

/**
 * Incoming FCM notification types are with FCM_ prefix
 *
 * FCM_TTS       -> speak message
 * FCM_TTS_ERROR -> tts not working
 * FCM_LOCATION  -> send location notif
 * FCM_LOCATION_UPDATE -> client send notif to update lastLocation
 * FCM_SOS       -> send SOS alert
 * FCM_CONFIG    ->
 * FCM_SUBSCRIBE ->
 * FCM_ALARM_CHANGE -> When alarm CRUD occured
 * FCM_CLIENT_CHANGE -> When client CRUD occured
 * FCM_EMERGENCY_CONTACT_CHANGE -> When client CRUD occured
 *
 * CALL          -> Start new call intent
 * NOTIFICATION_SUCCESS -> send back notif
 */
enum class EventType(value: String) {
    FCM_TTS("FCM_TTS"),
    FCM_TTS_ERROR("FCM_TTS_ERROR"),
    FCM_LOCATION("FCM_LOCATION"),
    FCM_LOCATION_UPDATE("FCM_LOCATION_UPDATE"),
    FCM_SOS("FCM_SOS"),
    FCM_CONFIG("FCM_CONFIG"),
    FCM_SUBSCRIBE("FCM_SUBSCRIBE"),
    FCM_ALARM_CHANGE("FCM_ALARM_CHANGE"),
    FCM_CLIENT_CHANGE("FCM_CLIENT_CHANGE"),
    FCM_EMERGENCY_CONTACT_CHANGE("FCM_EMERGENCY_CONTACT_CHANGE"),
    CALL("CALL"),
    NOTIFICATION_SUCCESS("NOTIFICATION_SUCCESS")
}