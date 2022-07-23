package com.app.app.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.app.app.dto.EventObject
import com.app.app.enums.FcmEventType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class EventService: TextToSpeech.OnInitListener {

    private var context : Context? = null

    constructor(context: Context) {
        this.context = context
        EventBus.getDefault().register(this);
    }

    val TAG = "EventService"

    var tts: TextToSpeech = TextToSpeech(context, this)

    //@Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(eventObject: EventObject) {
        Log.e(TAG, "onEvent event object $eventObject")
        when(eventObject.type) {
            //FcmEventType.CALL -> onCallEvent(eventObject)
            FcmEventType.TTS -> onMessageEvent(eventObject)
            //FcmEventType.LOCATION -> onLocationEvent(eventObject)
            //FcmEventType.SOS -> onSosEvent(eventObject)
            //FcmEventType.CONFIG -> TODO()
        }
    }

    private fun onMessageEvent(eventObject: EventObject) {
        Log.e(TAG, "onMessageEvent $eventObject")
        if (eventObject.type == FcmEventType.TTS) {
            val text = eventObject.data["message"].toString()
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.FRENCH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG,"The Language specified is not supported!")
            }
        } else {
            Log.e(TAG, "Initilization Failed!")
        }
    }
}