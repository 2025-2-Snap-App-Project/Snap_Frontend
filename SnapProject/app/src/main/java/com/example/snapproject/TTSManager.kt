package com.example.snapproject

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

// utterance ID별로 TTS 완료 콜백을 관리
private val ttsCallbacks = mutableMapOf<String, () -> Unit>() // <Key, Value> -> <utterance ID, TTS 완료 후 호출할 콜백>

// TTS 초기화 함수
fun initTTS(
    context: Context,
    onReady: () -> Unit,
): TextToSpeech {
    var tts: TextToSpeech? = null
    tts =
        TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("SnapTextToSpeech", "해당 언어는 지원되지 않습니다.")
                    return@TextToSpeech
                }
                onReady() // TTS 초기화 끝난 뒤에, 입력으로 들어온 onReady() 실행
            }
        }
    return tts
}

fun TextToSpeech?.readText(
    text: String,
    context: Context,
    onDone: (() -> Unit)? = null,
) {
    this?.let { tts ->
        val utteranceId = System.currentTimeMillis().toString() // 발화 식별용 고유 ID
        onDone?.let { ttsCallbacks[utteranceId] = it } // ID별로 TTS 완료 콜백 저장

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        try {
            // TTS가 Audio 포커스를 가져옴
            val audioFocusRequest =
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    )
                    .setOnAudioFocusChangeListener { }
                    .build()

            audioManager.requestAudioFocus(audioFocusRequest)
        } catch (e: Exception) {
            Log.e("SnapTextToSpeech", "requestAudioFocus 실패: ${e.message}")
        }

        // 발화 진행 상태를 감지하는 리스너
        tts.setOnUtteranceProgressListener(
            object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                // 발화 완료 후 입력으로 들어온 OnDone 코드 실행
                override fun onDone(id: String?) {
                    id?.let { uid ->
                        ttsCallbacks.remove(uid)?.invoke() // 해당 ID에 맞는 콜백 실행하면서 동시에 map에서 제거
                        try {
                            audioManager.abandonAudioFocus(null)
                        } catch (e: Exception) {
                            Log.e("SnapTextToSpeech", "abandonAudioFocus 실패: ${e.message}")
                        }
                    }
                }

                override fun onError(id: String?) {
                    id?.let { uid ->
                        ttsCallbacks.remove(uid) // 해당 ID에 맞는 콜백을 map에서 제거 (실행 X)
                        try {
                            audioManager.abandonAudioFocus(null)
                        } catch (e: Exception) {
                        }
                    }
                }
            },
        )

        // 기존 발화 완료한 뒤, 입력으로 들어온 text에 대해 발화 시작
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
        Log.d("SnapTextToSpeech", "TTS가 읽음")
    }
}
