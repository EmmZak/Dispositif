
APP

* apps (collection)
  * FpvQ2leGd0mRnTfJe142 (App.uid) (document)
    * app: App
  * ...

* App
  * uid
  * token
  * number
  * address
  * alarms: List(Alarm)
  * clients: List(Client)

* Client
  * uid
  * token
  * number
  * name

* FcmObject
  * to: (FCM String)
  * data : FcmObjectData

* FcmObjectData
  * event: EventType
  * date
  * number: String?
  * message: String?
  * mapUrl: String?

* EventType
  * FCM_TTS
  * FCM_TTS_ERROR
  * FCM_LOCATION
  * FCM_LOCATION_ERROR
  * FCM_SOS
  * FCM_CONFIG
  * FCM_SUBSCRIBE
  * CALL
  * NOTIFICATION_SUCCESS


* Alarm
  * uid
  * text
  * datetime
  * frequency: AlarmFrequency

* AlarmFrequency
  * ONCE
  * DAILY
  * WEEKLY
  * MONTHLY