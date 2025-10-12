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

// 입력된 String을 읽어주는 함수 (입력으로 들어오는 onDone은 발화가 끝난 뒤에 실행할 콜백 함수)
fun TextToSpeech?.readText(text: String, onDone: (() -> Unit)? = null) {
    this?.let { tts ->
        val utteranceId = System.currentTimeMillis().toString() // 발화 식별용 고유 ID

        // 발화 진행 상태를 감지하는 리스너
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            // 발화 완료 후 입력으로 들어온 OnDone 코드 실행
            override fun onDone(utteranceId: String?) {
                onDone?.let { it() }
            }
            override fun onError(utteranceId: String?) {}

        })

        // 기존 발화 중단 후 입력으로 들어온 text에 대해 발화 시작
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
}
