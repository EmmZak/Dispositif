package com.app.app.service.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.lang.Exception
import java.util.*

class TtsService(context: Context): TextToSpeech.OnInitListener {

    val TAG = "TtsService Manu"

    var tts: TextToSpeech = TextToSpeech(context, this)

    fun speak(message: String) {
        try {
            val res = tts.speak(message, TextToSpeech.QUEUE_ADD, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)

            if (res == TextToSpeech.ERROR) {
                Log.e(TAG, "tts speak QUEUE error res=$res")
            } else {
                Log.e(TAG, "tts worked")
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.FRENCH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG,"The Language specified is not supported!")
            }
            //tts.setOnUtteranceProgressListener(UtteranceManager())
            Log.e(TAG, "TTS setup done")

        } else {
            Log.e(TAG, "Initilization Failed!")
        }
    }
}