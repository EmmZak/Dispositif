package com.app.app.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.lang.Exception
import java.util.*

class TTS: TextToSpeech.OnInitListener {

    val TAG = "TTS manu"
    var tts: TextToSpeech? = null

    constructor(context: Context) {
        Log.e(TAG, "tts init")
        try {
            tts = TextToSpeech(context, this)
            Log.e(TAG, "done init")
        } catch(e: Exception) {
            Log.e(TAG, "tts error $e")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val res = tts!!.setLanguage(Locale.FRANCE)

            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG,"The Language specified is not supported!")
            }
        } else {
            Log.e(TAG, "Initilization Failed!")
        }
    }

}