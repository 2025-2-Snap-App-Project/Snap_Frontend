package com.example.snapproject

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

// TTS 초기화 함수
fun initTTS(context: Context): TextToSpeech {
    var tts: TextToSpeech? = null
    tts = TextToSpeech(context) {
        if (it == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeech", "해당 언어는 지원되지 않습니다.")
                return@TextToSpeech
            }
        }
    }
    return tts
}

// 입력된 String을 읽어주는 함수
fun TextToSpeech?.readText(text: String) {
    this?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    this?.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null)
}
