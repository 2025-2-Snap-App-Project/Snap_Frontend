package com.example.snapproject

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import java.util.Locale

// TTS 초기화 함수
fun initTTS(context: Context): TextToSpeech {
    var tts: TextToSpeech? = null
    tts =
        TextToSpeech(context) {
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

// TalkBack이 켜져있을 때, 입력된 String을 읽어주는 함수
// 입력으로 들어오는 onDone은 발화가 끝난 뒤에 실행할 콜백 함수
fun TextToSpeech?.readText(
    msg: String,
    context: Context,
    onDone: (() -> Unit)? = null,
) {
    // 접근성 관리자 체크
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val isAccessibilityEnabled = accessibilityManager.isEnabled

    if (isAccessibilityEnabled) { // 접근성 서비스가 활성화되어 있으면, AccessibilityEvent TYPE_ANNOUNCEMENT 로 보냄
        try { // TextToSpeech 대신 TalkBack를 발화 수단으로 사용
            // TalkBack에 event 전달 (읽어야 하는 메시지 전달)
            val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT).apply {
                className = context.javaClass.name
                packageName = context.packageName
                text.add(msg)
            }
            accessibilityManager.sendAccessibilityEvent(event)
        } catch (e: Exception) { // TalkBack이 event를 전달받는 데 실패한 경우
            Log.e("TextToSpeech", "Accessibility announcement 실패, fallback to TTS: ${e.message}")
            this.readTextWithAudioFocus(msg, context, onDone) // TTS 사용
        }
    } else { // TalkBack이 꺼져 있는 경우 -> TTS 사용
        this.readTextWithAudioFocus(msg, context, onDone)
    }
}

// TalkBack이 꺼져있을 때, 입력된 String을 읽어주는 함수
fun TextToSpeech?.readTextWithAudioFocus(
    text: String,
    context: Context,
    onDone: (() -> Unit)? = null,
) {
    this?.let { tts ->
        val utteranceId = System.currentTimeMillis().toString() // 발화 식별용 고유 ID

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        try { // TTS가 Audio 포커스를 가져옴
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        } catch (e: Exception) {
            Log.e("TextToSpeech", "requestAudioFocus 실패: ${e.message}")
        }

        // 발화 진행 상태를 감지하는 리스너
        tts.setOnUtteranceProgressListener(
            object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                // 발화 완료 후 입력으로 들어온 OnDone 코드 실행
                override fun onDone(utteranceId: String?) {
                    try {
                        audioManager.abandonAudioFocus(null)
                    } catch (e: Exception) {
                        Log.e("TextToSpeech", "abandonAudioFocus 실패: ${e.message}")
                    }
                    onDone?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    try {
                        audioManager.abandonAudioFocus(null)
                    } catch (e: Exception) { }
                }
            },
        )

        // 기존 발화 완료한 뒤, 입력으로 들어온 text에 대해 발화 시작
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        Log.d("TextToSpeech", "TTS가 읽음")
    }
}
